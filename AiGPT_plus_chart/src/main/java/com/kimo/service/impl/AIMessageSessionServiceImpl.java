package com.kimo.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.AIMessageSessionMapper;

import com.kimo.model.dto.chart.AIMessageSessionQueryRequest;
import com.kimo.model.dto.chart.AIMessageSessionUpdateRequest;
import com.kimo.model.dto.chart.UserDto;
import com.kimo.model.dto.po.AIMessageSession;
import com.kimo.service.AIMessageSessionService;

import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.kimo.constant.CaffeineConstant.CAFFEINE_USER;
import static com.kimo.constant.CaffeineConstant.CAFFEINE_USER_LOCKED;

/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class AIMessageSessionServiceImpl extends ServiceImpl<AIMessageSessionMapper, AIMessageSession>
    implements AIMessageSessionService {

    @Autowired
    UserClient userClient;

    @Autowired
    AIMessageSessionMapper aiMessageSessionMapper;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private RedissonClient redissonClient;



    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    @Transactional
    /**
     * @Author: Mr.kimo
     * @Date: 17:15
     * @return: boolean
     * @Param: [long, jakarta.servlet.http.HttpServletRequest]
     * @Description: 分页获取会话，由于版本更新，已抛弃该功能
     */
    public boolean deleteAIMessageSession(long id, HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        QueryWrapper<AIMessageSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.CHART_ID, id);
        queryWrapper.eq(SqlConstants.USER_ID, userDto.getId());
        AIMessageSession aiMessageSession = aiMessageSessionMapper.selectOne(queryWrapper);
        if(aiMessageSession != null) {
            aiMessageSessionMapper.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * @Author: Mr.kimo
     * @Date: 17:15
     * @return: UserDto
     * @Param: [request, type]
     * @Description:
     * 该方法用于根据请求头获取用户信息。如果在缓存中找不到用户数据，
     * 则通过分布式锁防止缓存击穿，查询数据库获取用户信息，并将数据同步到Redis缓存。
     * 如果数据仍然不存在，则将空值写入Redis以防止缓存穿透。
     *
     * @throws BusinessException 当获取锁失败或系统异常时抛出
     * @param request HttpServletRequest对象，用于获取请求头信息
     * @param type 请求头类型，用于识别用户信息
     */
    @Override
    public UserDto getUserDtoForRedisOrLock(HttpServletRequest request, String type) {
        UserDto userDto = new UserDto();
        Object user = servletUtils.getHeaderRedisForUser(request, type);

        if (user == null) {
            String headerKey = servletUtils.getHeaderKey(request, type);
            String redisUserKey = CAFFEINE_USER + headerKey;

            if (StringUtils.isNotBlank(headerKey)) {
                // 使用分布式锁，解决缓存击穿问题
                RLock lock = redissonClient.getLock(CAFFEINE_USER_LOCKED + headerKey);
                int maxRetries = 3;  // 最大重试次数
                int retryCount = 0;
                boolean isLocked = false;

                while (retryCount < maxRetries) {
                    try {
                        // 尝试获取锁，等待10秒，锁超时设置为2的重试次数幂秒数，限制最大值
                        isLocked = lock.tryLock(10, Math.min(10, (long) Math.pow(2, retryCount)), TimeUnit.SECONDS); // 最大锁持有时间10秒
                        if (isLocked) {
                            // 获取用户名并查询用户数据
                            String username = servletUtils.getHeader(request, type);
                            userDto = userClient.GobalGetLoginUser(username);

                            if (userDto == null) {
                                // 用户不存在，将空值写入Redis，防止缓存穿透
                                redisTemplate.opsForValue().set(redisUserKey, "", 1L, TimeUnit.HOURS);
                                throw new BusinessException(ErrorCode.USER_IS_NOT);
                            }

                            // 同步数据到Redis缓存
                            syncToRedis(redisUserKey, userDto);
                            break;  // 成功获取并处理数据后，退出重试循环
                        } else {
                            retryCount++;
                            log.warn("获取锁失败，正在重试，第 {} 次，headerKey: {}", retryCount, headerKey);
                            Thread.sleep(1000L * Math.min(5, (long) Math.pow(2, retryCount))); // 最大延迟5秒
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试");
                    } catch (Exception e) {
                        log.error("锁操作异常", e);
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
                    } finally {
                        // 确保只有在成功获取锁后才释放
                        if (isLocked) {
                            lock.unlock();
                        }
                    }
                }
            }
        } else {
            // 安全复制属性
            BeanUtils.copyProperties(user, userDto);
        }

        return userDto;
    }


    //缓存到redis
    private void syncToRedis(String cacheKey, Object data) {
        if (data != null) {
            String serializedData = serialize(data);
            redisTemplate.opsForValue().set(cacheKey, serializedData, 1, TimeUnit.HOURS); // 同步到 Redis 缓存
        }
    }

    // 序列化方法
    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);

        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
//        return data.toString();
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:16
     * @return: com.baomidou.mybatisplus.core.conditions.Wrapper<com.kimo.model.dto.po.AIMessageSession>
     * @Param: [com.kimo.model.dto.chart.AIMessageSessionQueryRequest, com.kimo.model.dto.chart.UserDto]
     * @Description: 分页获取会话，由于版本更新，已抛弃该功能
     */
    public Wrapper<AIMessageSession> getQueryWrapper(AIMessageSessionQueryRequest aiMessageSessionQueryRequest, UserDto user) {
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_LOGIN_ERROR);
        QueryWrapper<AIMessageSession> queryWrapper = new QueryWrapper<>();
        String sortField = aiMessageSessionQueryRequest.getSortField();
        String sortOrder = aiMessageSessionQueryRequest.getSortOrder();
        Long userId = user.getId();
        ThrowUtils.throwIf(userId <= 0,ErrorCode.NOT_LOGIN_ERROR);
        queryWrapper.eq(SqlConstants.USER_ID, userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    @Transactional
    /**
     * @Author: Mr.kimo
     * @Date: 17:18
     * @return: boolean
     * @Param: [com.kimo.model.dto.chart.AIMessageSessionUpdateRequest, jakarta.servlet.http.HttpServletRequest]
     * @Description: 由于版本更新，已抛弃该功能
     */
    public boolean validAIMessageSession(AIMessageSessionUpdateRequest aiRole, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        if(!Objects.equals(userDtoForRedisOrLock.getId(), aiRole.getUserId())){
            return false;
        }
        QueryWrapper<AIMessageSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.CHART_ID, aiRole.getId());
        queryWrapper.eq(SqlConstants.USER_ID, aiRole.getUserId());
        AIMessageSession aiMessageSession = aiMessageSessionMapper.selectOne(queryWrapper);
        if(aiMessageSession != null) {
            aiMessageSession.setTitle(aiRole.getTitle());
            aiMessageSessionMapper.updateById(aiMessageSession);
            return true;
        }
        return false;
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:19
     * @return: void
     * @Param: [jakarta.servlet.http.HttpServletRequest, com.kimo.model.dto.po.AIMessageSession]
     * @Description: 由于版本更新，已抛弃该功能
     */
    public void addAIMessageSession(HttpServletRequest request, AIMessageSession aiRole) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        aiRole.setUserId(userDtoForRedisOrLock.getId());
        int insert = aiMessageSessionMapper.insert(aiRole);
        if(insert <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:21
     * @return: com.kimo.model.dto.chart.UserDto
     * @Param: [java.lang.String]
     * @Description: 获取会话信息
     */
    public UserDto getAIMessageSessionByUser(String request) {
        UserDto user = userClient.GobalGetLoginUser(request);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    @Override
    public void fetchUpdatePoint(Long pointNumber, Long userId) {
        try {
            // 直接在条件判断中使用方法调用
            if (!Boolean.TRUE.equals(userClient.updatePoint(userId, pointNumber))) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
        } catch (Exception e) {
            // 捕获特定异常类型（假设你知道可能抛出的具体异常类型）
            log.error("更新用户积分时发生特定异常，用户ID: {}, 积分数量: {}", userId, pointNumber, e);
            throw new BusinessException(ErrorCode.POINT_NOT_FOUNT);
        }
    }

}





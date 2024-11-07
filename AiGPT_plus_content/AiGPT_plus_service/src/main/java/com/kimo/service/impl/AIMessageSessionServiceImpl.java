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
import com.kimo.model.dto.aiMessageSession.AIMessageSessionQueryRequest;
import com.kimo.model.dto.aiMessageSession.AIMessageSessionUpdateRequest;
import com.kimo.model.dto.user.Point;
import com.kimo.model.dto.user.UserDto;
import com.kimo.model.entity.AIMessageSession;
import com.kimo.model.entity.AIRole;
import com.kimo.service.AIMessageSessionService;
import com.kimo.utils.RedisUtils;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
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
import static com.kimo.constant.RedisConstant.USER_POINT_KEY;

/**
 * @author Mr.kimo
 */
@Service
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
    private RedisUtils redisUtils;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    @Transactional
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

    @Override
    public UserDto getUserDtoForRedisOrLock(HttpServletRequest request,String type){
        UserDto userDto = new UserDto();
        Object user = servletUtils.getHeaderRedisForUser(request, type);

        if (user == null) {
            String headerKey = servletUtils.getHeaderKey(request, type);
            String redis_user_key =  CAFFEINE_USER + headerKey;
            if (StringUtils.isNotBlank(headerKey)) {
                RLock lock = redissonClient.getLock(CAFFEINE_USER_LOCKED + headerKey);
                boolean isLocked = false;
                try {
                    // 尝试获取锁，等待10秒，锁超时1秒自动释放
                    isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);
                    if (isLocked) {
                        String username = servletUtils.getHeader(request, type);
                        userDto = userClient.GobalGetLoginUser(username);
                        // 如果成功获取锁，进行数据库查询和缓存更新
                        if (userDto == null) {
                            // 将空值写入redis 防止缓存穿透
                            redisTemplate.opsForValue().set(redis_user_key, "", 1L, TimeUnit.HOURS);
                            throw new BusinessException(ErrorCode.USER_IS_NOT); // 返回 null 表示没有找到课程
                        }
                        syncToRedis(redis_user_key,userDto);
                    }
                } catch (InterruptedException e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }finally {
                    if (isLocked){
                        lock.unlock();
                    }
                }
            }

        }else {

            BeanUtils.copyProperties(user,userDto);

        }
        return userDto;
    }

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
    public Wrapper<AIMessageSession> getQueryWrapper(AIMessageSessionQueryRequest aiMessageSessionQueryRequest,UserDto user) {
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
    public boolean validAIMessageSession(AIMessageSessionUpdateRequest aiRole, HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        if(!Objects.equals(userDto.getId(), aiRole.getUserId())){
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
    public void addAIMessageSession(HttpServletRequest request, AIMessageSession aiRole) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        aiRole.setUserId(userDto.getId());
        int insert = aiMessageSessionMapper.insert(aiRole);
        if(insert <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    public UserDto getAIMessageSessionByUser(String request) {
        UserDto user = userClient.GobalGetLoginUser(request);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    @Override
    public synchronized void fetchUpdatePoint(Long pointNumber, Long userId) {
        Boolean b = userClient.updatePoint(userId, pointNumber);
        ThrowUtils.throwIf(!b,ErrorCode.NOT_FOUND_ERROR);

    }
}





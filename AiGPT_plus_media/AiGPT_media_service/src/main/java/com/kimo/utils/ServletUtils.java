package com.kimo.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.api.client.PermissionsClient;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.Permissions;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;


import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.kimo.constant.CaffeineConstant.CAFFEINE_USER;
import static com.kimo.constant.CaffeineConstant.CAFFEINE_USER_LOCKED;
import static com.kimo.constant.SecurityConstants.LOGIN_TYPE;


/**
 * 客户端工具类
 *
 * @author ruoyi
 */
@Component
public class ServletUtils
{


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserClient userClient;


    @Autowired
    private PermissionsClient permissionsClient;




    public UserDto getHeaderRedisForUser(HttpServletRequest request, String name)
    {
        final String header = request.getHeader(name);

        final String jwt;
        final String userEmail;
        if(header == null || !header.startsWith(LOGIN_TYPE)){
            return null;
        }
        jwt = header.substring(7);
        String jwtKey = CAFFEINE_USER + jwt;
        String userJson = redisTemplate.opsForValue().get(jwtKey);
        if(userJson == null){
            return null;
        }
        UserDto deserialize = deserialize(userJson, UserDto.class);
        if(deserialize == null){
            return null;
        }
        return deserialize;
    }

    public String getHeader(HttpServletRequest request, String name)
    {
        final String header = request.getHeader(name);

        final String jwt;
        if(header == null || !header.startsWith(LOGIN_TYPE)){
            return null;
        }
        jwt = header.substring(7);
        return jwtService.extractUsername(jwt);
    }

    public String getHeaderKey(HttpServletRequest request, String name)
    {
        final String header = request.getHeader(name);

        final String jwt;
        if(header == null || !header.startsWith(LOGIN_TYPE)){
            return null;
        }
        jwt = header.substring(7);
        return jwt;
    }




    public <T> T deserialize(String redisData, Class<T> clazz) {
        try {
            return objectMapper.readValue(redisData, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
    }


    public UserDto getUserDtoForRedisOrLock(HttpServletRequest request, String type) {
        UserDto userDto = new UserDto();
        // 尝试从Redis中获取用户数据
        Object user = this.getHeaderRedisForUser(request, type);

        if (user == null) {
            String headerKey = this.getHeaderKey(request, type);
            String redisUserKey = CAFFEINE_USER + headerKey;

            if (StringUtils.isNotBlank(headerKey)) {
                // 尝试获取分布式锁，避免缓存穿透
                if (tryLockAndFetchData(headerKey, redisUserKey, request, type, userDto)) {
                    return userDto;
                }
            }
        } else {
            // 从Redis中成功获取，复制属性
            BeanUtils.copyProperties(user, userDto);
        }

        return userDto;
    }

    /**
     * 尝试获取分布式锁并查询数据库更新Redis缓存
     */
    private boolean tryLockAndFetchData(String headerKey, String redisUserKey, HttpServletRequest request, String type, UserDto userDto) {
        RLock lock = redissonClient.getLock(CAFFEINE_USER_LOCKED + headerKey);
        boolean isLocked = false;
        try {
            // 尝试获取锁，等待10秒，锁超时1秒自动释放
            isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if (isLocked) {
                String username = this.getHeader(request, type);
                userDto = userClient.GobalGetLoginUser(username);

                // 用户数据为空，防止缓存穿透
                if (userDto == null) {
                    redisTemplate.opsForValue().set(redisUserKey, "", 1L, TimeUnit.HOURS);
                    throw new BusinessException(ErrorCode.USER_IS_NOT); // 返回 null 表示没有找到用户
                }
                syncToRedis(redisUserKey, userDto); // 更新到 Redis
                return true;
            }
        } catch (InterruptedException e) {
            // 异常处理，添加日志
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "锁定中断");
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
        return false;
    }

    // 序列化方法
    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);

        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
    }

    private void syncToRedis(String cacheKey, Object data) {
        if (data != null) {
            String serializedData = serialize(data);
            redisTemplate.opsForValue().set(cacheKey, serializedData, 1, TimeUnit.HOURS); // 同步到 Redis 缓存
        }
    }


    public String getRoleForPermission(UserDto userDtoForRedisOrLock) {
        Long roleId = userDtoForRedisOrLock.getRoleId();
        Permissions userPermissions = permissionsClient.getUserPermissions(String.valueOf(roleId));
        String code = userPermissions.getCode();
        return code;
    }

    public void ensuperAdminOrAdmin(String code,String expected) {
        ArrayList<String> permissionList = new ArrayList<>();
        try {
            permissionList = objectMapper.readValue(code,new TypeReference<ArrayList<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        }
        boolean admin = permissionList.contains(expected);
        boolean superAdmin = permissionList.contains("114514");
        if(!admin && !superAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"不是管理员或超级管理员");
        }
    }
}

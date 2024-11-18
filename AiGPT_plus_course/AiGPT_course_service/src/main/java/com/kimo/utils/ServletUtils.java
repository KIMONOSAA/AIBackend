package com.kimo.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import com.kimo.model.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.kimo.constant.CaffeineConstant.CAFFEINE_USER;
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
    private JwtService jwtService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedissonClient redissonClient;


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



}

package com.kimo.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;

import com.kimo.model.po.Permissions;
import com.kimo.service.PermissionsService;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
    @Qualifier("redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    @Lazy
    private PermissionsService permissionsService;



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

    public String getRoleForPermission(com.kimo.api.dto.UserDto userDtoForRedisOrLock) {
        Long roleId = userDtoForRedisOrLock.getId();
        Permissions userPermissions = permissionsService.getPermissionByRoleId(String.valueOf(roleId));
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

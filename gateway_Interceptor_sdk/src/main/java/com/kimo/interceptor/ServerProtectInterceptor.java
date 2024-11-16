package com.kimo.interceptor;

import com.kimo.config.CloudSecurityProperties;
import com.kimo.constant.CloudConstant;
import com.kimo.constant.RedisDataConstant;
import com.kimo.dto.ResultData;
import com.kimo.utils.JwtUtils;
import com.kimo.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class ServerProtectInterceptor implements HandlerInterceptor {

    private CloudSecurityProperties properties;


    @Autowired
    private RedisTemplate<String,String> redisTemplate;



    @Override 
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler){
        ResultData<String> resultData = new ResultData<>();
        resultData.setSuccess(false);
        resultData.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resultData.setMessage("请通过网关访问资源");
        final String header = request.getHeader("X-GatewayTokenHeader");

        if(header == null){
            WebUtils.writeJson(response,resultData);
            return false;
        }

        if (StringUtils.isEmpty(header)){
            WebUtils.writeJson(response,resultData);
            return false;
        }

        JwtUtils jwtUtils = new JwtUtils();
        if (!properties.getOnlyFetchByGateway()) { 
            return true; 
        }
        String key = RedisDataConstant.GATEWAY + header;
        String cachedToken = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(cachedToken)){
            WebUtils.writeJson(response,resultData);
            return false;
        }
        String token = request.getHeader(CloudConstant.GATEWAY_TOKEN_HEADER);

        return true;
    } 
 
    public void setProperties(CloudSecurityProperties properties) { 
        this.properties = properties; 
    } 
} 
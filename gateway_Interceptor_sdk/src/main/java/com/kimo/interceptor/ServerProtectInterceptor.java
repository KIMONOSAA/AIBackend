package com.kimo.interceptor;

import com.kimo.config.CloudSecurityProperties;
import com.kimo.constant.CloudConstant;
import com.kimo.constant.RedisConstant;
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
        final String header = request.getHeader("Authorization");

        final String jwt;
        final String userEmail;
        if(header == null || !header.startsWith("Bearer ")){
            WebUtils.writeJson(response,resultData);
            return false;
        }

        jwt = header.substring(7);
        if (StringUtils.isEmpty(jwt)){
            WebUtils.writeJson(response,resultData);
            return false;
        }

        JwtUtils jwtUtils = new JwtUtils();
        userEmail = jwtUtils.extractUsername(jwt);
        if (!properties.getOnlyFetchByGateway()) { 
            return true; 
        }
        String key = RedisConstant.GATEWAY + userEmail;
        String cachedToken = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(cachedToken)){
            WebUtils.writeJson(response,resultData);
            return false;
        }
        String token = request.getHeader(CloudConstant.GATEWAY_TOKEN_HEADER);

        // 比较 Redis 中的 Token 和当前请求中的 Token 是否匹配
        if (!cachedToken.equals(token)) {
            WebUtils.writeJson(response,resultData);
            return false;  // 或者返回 403 等错误响应
        }
        return true;
    } 
 
    public void setProperties(CloudSecurityProperties properties) { 
        this.properties = properties; 
    } 
} 
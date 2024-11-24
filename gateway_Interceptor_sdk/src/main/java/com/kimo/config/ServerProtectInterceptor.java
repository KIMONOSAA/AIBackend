package com.kimo.config;

import com.kimo.constant.CloudConstant;
import com.kimo.constant.RedisDataConstant;
import com.kimo.dto.ResultData;
import com.kimo.utils.JwtUtils;
import com.kimo.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


//@Component
@AllArgsConstructor
public class ServerProtectInterceptor implements HandlerInterceptor {

    private CloudProperties properties;


    private RedisOrGatewayUtils redisOrGatewayUtils;


    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler){
        ResultData<String> resultData = new ResultData<>();
        resultData.setSuccess(false);
        resultData.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resultData.setMessage("请通过网关访问资源");
        final String header = request.getHeader("X-GatewayTokenHeader");

        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headerMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headerMap.put(key, value);
        }
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
        String cachedToken = redisOrGatewayUtils.get(key);
        if (StringUtils.isEmpty(cachedToken)){
            WebUtils.writeJson(response,resultData);
            return false;
        }
        return true;
    } 
 
    public void setProperties(CloudProperties properties) {
        this.properties = properties; 
    }




} 
package com.kimo.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


public class CloudInterceptorConfigure implements WebMvcConfigurer {

    @Autowired
    private CloudProperties properties;

    @Autowired
    private RedisOrGatewayUtils redisOrGatewayUtils;


//    @Autowired
//    public void setProperties(CloudProperties properties) {
//        this.properties = properties;
//    }

    @Bean
    public ServerProtectInterceptor serverProtectInterceptor() {
        ServerProtectInterceptor interceptor = new ServerProtectInterceptor(properties,redisOrGatewayUtils);
//        interceptor.setProperties(properties);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serverProtectInterceptor());
    }
}
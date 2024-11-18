package com.kimo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@ComponentScan("com.kimo.config")
@EnableConfigurationProperties(CloudSecurityProperties.class)
public class CloudSecurityAutoConfigure{



    @Bean
    public CloudSecurityInterceptorConfigure cloudSecurityInterceptorConfigure() {
        return new CloudSecurityInterceptorConfigure();
    }


//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory("192.168.101.132", 6379);
//        connectionFactory.setPassword("redis");  // 设置 Redis 密码
//        return connectionFactory;
//    }


    @Bean
    @DependsOn("redisConnectionFactory")
    @Qualifier("redisTemplateOrGateway")
    public RedisTemplate<String, String> redisTemplateOrGateway(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }





} 
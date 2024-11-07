package com.kimo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalUserRedisUtils {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;



}

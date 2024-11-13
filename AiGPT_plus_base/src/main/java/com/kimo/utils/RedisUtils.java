package com.kimo.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.kimo.common.ErrorCode;
import com.kimo.constant.RedisConstant;
import com.kimo.exception.BusinessException;
import com.kimo.model.TeachplanListDto;
import com.kimo.model.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static com.kimo.constant.CaffeineConstant.*;

@Component
public class RedisUtils {

    @Autowired
    Cache<String, Object> commonCaffeine;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, byte[]> redisByteTemplate;

    // 初始化 ObjectMapper 为静态成员，避免多次实例化
    @Autowired
    private ObjectMapper objectMapper;

    public  <T> Object getFromCaffeineOrRedis(String cacheKey, Class<T> clazz) {
        Object data = commonCaffeine.getIfPresent(cacheKey);
        if (data == null) {
            // 如果 Caffeine 没有，查 Redis
            String redisData = redisTemplate.opsForValue().get(cacheKey);
            if(StringUtils.isNotBlank(redisData)){
                data = deserialize(redisData, clazz);
                commonCaffeine.put(cacheKey, data); // 同步到 Caffeine 缓存
                return data;
            }
            // 判断命中的是否是空值
            if (redisData != null) {
                // 返回一个错误信息
                return null;
            }

        }
        return data;
    }

    public  <T> T getFromCaffeineOrRedis(String cacheKey, Class<T> clazz, String teachplanKey) {
        T data = null;

        // 从 Redis 获取数据
        byte[] redisData = redisByteTemplate.opsForValue().get(teachplanKey);
        if (redisData != null) {
            try {
                byte[] decompressedData = decompressForRedis(redisData);
                String jsonData = new String(decompressedData, StandardCharsets.UTF_8);
                data = redisSerialize(jsonData, clazz);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }

        return data;
    }

    private <T> T redisSerialize(String redisData, Class<T> clazz) {
        try {
            return objectMapper.readValue(redisData, clazz);
        } catch (Exception e) {
            return null; // 或者根据业务需求抛出自定义异常
        }
    }

    private byte[] decompressForRedis(byte[] redisData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(redisData);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(bais)){
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0){
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }

    // 泛型序列化方法
    public <T> T deserialize(String redisData, Class<T> clazz) {
        try {
            return objectMapper.readValue(redisData, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
    }

    // 同步数据到 Redis 和 Caffeine
    private void syncToCache(String cacheKey, Object data) {
        if (data != null) {
            String serializedData = serialize(data);
            redisTemplate.opsForValue().set(cacheKey, serializedData, 1, TimeUnit.HOURS); // 同步到 Redis 缓存
            commonCaffeine.put(cacheKey, data); // 同步到 Caffeine 缓存
        }
    }

    public void storeTokenInRedis(String token, String userEmail) {
        // 将 token 存入 Redis，key 为用户邮箱，value 为 token
        String key = RedisConstant.GATEWAY + userEmail;
        redisTemplate.opsForValue().set(key, token, Duration.ofMinutes(3));  // 设置过期时间为 1 小时
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


}

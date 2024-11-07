package com.kimo.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.time.Duration;
/**
 * @author Mr.kimo
 */
@Configuration
public class CaffeineConfig {
    @Bean
    public Cache<String, Object> commonCaffeine() {
        return Caffeine.newBuilder()
                //初始大小
                .initialCapacity(1000)
                //PS：expireAfterWrite和expireAfterAccess同时存在时，以expireAfterWrite为准。
                //最后一次写操作后经过指定时间过期
//                .expireAfterWrite(Duration.ofMinutes(30))
                //最后一次读或写操作后经过指定时间过期
                .expireAfterAccess(Duration.ofHours(16))
                // 最大数量，默认基于缓存内的元素个数进行驱逐
                .maximumSize(10000)
                //打开数据收集功能  hitRate(): 查询缓存的命中率 evictionCount(): 被驱逐的缓存数量 averageLoadPenalty(): 新值被载入的平均耗时
//                .recordStats()
                .build();
    }
}
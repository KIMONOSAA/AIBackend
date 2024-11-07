package com.kimo.manager;


import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import jakarta.annotation.Resource;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


/**
 * Redis 限流功能
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key){
        RRateLimiter limiter = redissonClient.getRateLimiter(key);
        limiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        boolean conOp = limiter.tryAcquire(1);
        if(!conOp){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,"你请求太多次啦，慢慢来吧");

        }
    }
}

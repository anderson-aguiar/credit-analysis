package com.anderson.mscredit.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RateLimitRepository {

    private final RedisTemplate<String, Long> redisTemplate;

    private static final String RATE_LIMIT = "rate-limit:";

    public RateLimitRepository(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long incrementRequestCount(String customerId) {
        String chave = RATE_LIMIT + customerId;
        return redisTemplate.opsForValue().increment(chave);
    }

    public void setExpiration(String customerId, long hours) {
        String chave = RATE_LIMIT + customerId;
        redisTemplate.expire(chave, Duration.ofHours(hours));
    }

    public Long getRequestCount(String customerId) {
        String chave = RATE_LIMIT + customerId;
        var value = redisTemplate.opsForValue().get(chave);

        return value == null ? 0L : value;
    }
}

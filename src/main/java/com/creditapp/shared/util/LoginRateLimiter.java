package com.creditapp.shared.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MINUTES = 1;
    private final RedisTemplate<String, Object> redisTemplate;

    public LoginRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean checkRateLimit(String email) {
        String key = "login_attempts:" + email;
        Object attempts = redisTemplate.opsForValue().get(key);
        return attempts == null || (Integer) attempts < MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String email) {
        String key = "login_attempts:" + email;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, WINDOW_MINUTES, TimeUnit.MINUTES);
    }

    public void clearFailedAttempts(String email) {
        String key = "login_attempts:" + email;
        redisTemplate.delete(key);
    }
}
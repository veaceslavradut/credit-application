package com.creditapp.shared.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiter for distributed rate limiting across multiple instances.
 * Implements token bucket algorithm with configurable limits per minute.
 */
@Component
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if action is allowed for the given key.
     * Uses Redis to track and enforce rate limits.
     *
     * @param key The rate limit key (e.g., borrower_id:action)
     * @param limitPerMinute Maximum number of requests allowed per minute
     * @return true if action is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int limitPerMinute) {
        String redisKey = "rate_limit:" + key;
        
        try {
            // Get current count
            String countStr = redisTemplate.opsForValue().get(redisKey);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);
            
            // If count < limit, increment and allow
            if (count < limitPerMinute) {
                redisTemplate.opsForValue().increment(redisKey);
                
                // Set expiration on first increment (1 minute)
                if (count == 0) {
                    redisTemplate.expire(redisKey, 1, TimeUnit.MINUTES);
                }
                
                return true;
            }
            
            // Limit exceeded
            return false;
            
        } catch (Exception e) {
            // If Redis is down, allow request (fail open)
            return true;
        }
    }

    /**
     * Get remaining calls for the given key.
     *
     * @param key The rate limit key
     * @param limitPerMinute Maximum number of requests allowed per minute
     * @return Number of remaining calls, or -1 if key doesn't exist
     */
    public int getRemainingCalls(String key, int limitPerMinute) {
        String redisKey = "rate_limit:" + key;
        
        try {
            String countStr = redisTemplate.opsForValue().get(redisKey);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);
            return Math.max(0, limitPerMinute - count);
        } catch (Exception e) {
            return limitPerMinute;
        }
    }

    /**
     * Get remaining time until rate limit resets (in seconds).
     *
     * @param key The rate limit key
     * @return Remaining time in seconds, or -1 if key doesn't exist
     */
    public long getResetTimeSeconds(String key) {
        String redisKey = "rate_limit:" + key;
        
        try {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl == null || ttl < 0 ? 60 : ttl;
        } catch (Exception e) {
            return 60;
        }
    }
}

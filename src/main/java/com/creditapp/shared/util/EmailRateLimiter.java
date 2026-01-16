package com.creditapp.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * EmailRateLimiter provides distributed rate limiting for email sending
 * Uses Redis to track email send counts across application instances
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRateLimiter {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${notification.rate-limit.emails-per-minute:100}")
    private int emailsPerMinute;
    
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
    
    /**
     * Check if rate limit allows sending another email
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean checkRateLimit() {
        String key = getRateLimitKey();
        
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            
            if (count == null) {
                log.warn("Redis increment returned null for key: {}", key);
                return true; // Allow on Redis failure
            }
            
            // Set expiry on first increment
            if (count == 1) {
                redisTemplate.expire(key, 60, TimeUnit.SECONDS);
            }
            
            if (count > emailsPerMinute) {
                log.warn("Email rate limit exceeded: {} emails sent this minute (limit: {})", 
                    count, emailsPerMinute);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error checking rate limit, allowing email send", e);
            return true; // Allow on error (fail open)
        }
    }
    
    /**
     * Get current count of emails sent this minute
     * @return email count
     */
    public long getCurrentCount() {
        String key = getRateLimitKey();
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0;
        } catch (Exception e) {
            log.error("Error getting rate limit count", e);
            return 0;
        }
    }
    
    /**
     * Reset rate limit counter (for testing)
     */
    public void reset() {
        String key = getRateLimitKey();
        redisTemplate.delete(key);
    }
    
    /**
     * Generate Redis key for current minute
     * Format: email:sent_count:YYYY-MM-DD-HH-mm
     */
    private String getRateLimitKey() {
        String minute = LocalDateTime.now().format(MINUTE_FORMATTER);
        return "email:sent_count:" + minute;
    }
}

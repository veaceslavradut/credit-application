package com.creditapp.common.interceptor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    public RateLimitingInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (request.getRequestURI().equals("/api/auth/register") && request.getMethod().equals("POST")) {
            String clientIp = getClientIp(request);
            String key = "rate_limit:registration:" + clientIp;
            String countStr = redisTemplate.opsForValue().get(key);
            
            if (countStr == null) {
                redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.HOURS);
                return true;
            } else {
                long count = Long.parseLong(countStr);
                if (count < 10) {
                    redisTemplate.opsForValue().increment(key);
                    return true;
                } else {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Rate Limit Exceeded\", \"message\": \"Maximum 10 requests per hour\"}");
                    return false;
                }
            }
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
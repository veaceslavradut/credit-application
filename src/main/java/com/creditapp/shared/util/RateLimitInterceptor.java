package com.creditapp.shared.util;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter scenarioCalculatorRateLimiter;

    @Override
    public boolean preHandle(@NonNull jakarta.servlet.http.HttpServletRequest request, 
                            @NonNull jakarta.servlet.http.HttpServletResponse response, 
                            @NonNull Object handler) throws Exception {
        String path = request.getRequestURI();
        
        if (path.contains("scenario-calculator")) {
            try {
                if (!scenarioCalculatorRateLimiter.executeSupplier(() -> true)) {
                    response.setStatus(429);
                    response.setHeader("Retry-After", "60");
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Rate Limit Exceeded\", \"message\": \"Maximum 100 requests per minute\", \"retryAfter\": 60}");
                    return false;
                }
            } catch (RequestNotPermitted e) {
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Rate Limit Exceeded\", \"message\": \"Maximum 100 requests per minute\", \"retryAfter\": 60}");
                return false;
            }
        }
        
        return true;
    }
}
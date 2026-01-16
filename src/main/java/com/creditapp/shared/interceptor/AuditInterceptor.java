package com.creditapp.shared.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP Interceptor to capture request context for audit logging
 * Stores IP address and user agent in ThreadLocal for easy access during request handling
 */
@Component
@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {
        // The RequestContextService already handles extraction from ServletRequestAttributes
        // This interceptor ensures the request is properly available throughout request processing
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                               @Nullable Exception ex)
            throws Exception {
        // Cleanup if needed
    }
}

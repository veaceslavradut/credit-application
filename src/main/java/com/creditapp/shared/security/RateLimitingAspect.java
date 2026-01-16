package com.creditapp.shared.security;

import com.creditapp.shared.exception.RateLimitExceededException;
import com.creditapp.shared.util.RateLimiter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * AOP aspect for enforcing rate limits on methods annotated with @RateLimited.
 * Extracts the authenticated user's ID from SecurityContext and checks rate limits.
 */
@Aspect
@Component
public class RateLimitingAspect {

    private final RateLimiter rateLimiter;

    public RateLimitingAspect(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * Check rate limits before executing @RateLimited methods.
     * Rate limit key: borrower_id:action (e.g., "550e8400-e29b-41d4-a716-446655440000:CREATE_APPLICATION")
     *
     * @param joinPoint The join point (method being intercepted)
     * @param rateLimited The @RateLimited annotation on the method
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    @Before("@annotation(rateLimited)")
    public void enforceRateLimit(JoinPoint joinPoint, RateLimited rateLimited) {
        // Extract authenticated user ID from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            // Skip rate limiting for unauthenticated requests (will be rejected by @PreAuthorize anyway)
            return;
        }

        // Get principal (could be UUID string or UserDetails)
        Object principal = authentication.getPrincipal();
        UUID userId = extractUserId(principal);

        if (userId == null) {
            // Skip rate limiting if we can't extract user ID
            return;
        }

        // Construct rate limit key: user_id:action
        String action = rateLimited.action();
        String rateLimitKey = userId + ":" + action;
        int limitPerMinute = rateLimited.limitPerMinute();

        // Check rate limit
        if (!rateLimiter.isAllowed(rateLimitKey, limitPerMinute)) {
            long resetTime = rateLimiter.getResetTimeSeconds(rateLimitKey);
            throw new RateLimitExceededException(
                    "Rate limit exceeded for action: " + action + 
                    ". Maximum " + limitPerMinute + " request(s) per minute.",
                    (int) resetTime
            );
        }
    }

    /**
     * Extract user ID (UUID) from principal object.
     * Handles both string UUIDs and UserDetails objects.
     *
     * @param principal The authentication principal
     * @return The user's UUID, or null if not found
     */
    private UUID extractUserId(Object principal) {
        if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        // If principal is a UserDetails-like object with getId() method
        try {
            if (principal.getClass().getMethod("getId").getReturnType() == UUID.class) {
                return (UUID) principal.getClass().getMethod("getId").invoke(principal);
            }
        } catch (Exception e) {
            // Method doesn't exist or invocation failed
        }

        return null;
    }
}

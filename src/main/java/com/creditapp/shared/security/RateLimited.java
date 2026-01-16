package com.creditapp.shared.security;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should be rate limited.
 * Rate limiting is enforced by RateLimitingAspect.
 *
 * Example:
 * @RateLimited(action = "CREATE_APPLICATION", limitPerMinute = 1)
 * public ResponseEntity<ApplicationDTO> createApplication(...) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {

    /**
     * The action being rate limited (e.g., "CREATE_APPLICATION").
     * Used to construct the rate limit key.
     */
    String action() default "";

    /**
     * Maximum number of requests allowed per minute.
     * Default is 60 (1 per second).
     */
    int limitPerMinute() default 60;
}

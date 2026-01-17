package com.creditapp.shared.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    /**
     * Rate limiter for scenario calculator endpoint.
     * Configuration: 100 requests per minute
     */
    @Bean(name = "scenarioCalculatorRateLimiter")
    public RateLimiter scenarioCalculatorRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ofMillis(25))
                .build();

        return RateLimiter.of("scenarioCalculator", config);
    }
}
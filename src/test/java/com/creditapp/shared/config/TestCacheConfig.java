package com.creditapp.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
@Profile("test")
public class TestCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        // Use simple in-memory cache for tests (no Redis)
        return new ConcurrentMapCacheManager(
            "bankMarketAnalysis", 
            "marketAverage", 
            "rateCards",
            "scenarioCalculations",
            "helpArticles",
            "applicationDetails"
        );
    }
}
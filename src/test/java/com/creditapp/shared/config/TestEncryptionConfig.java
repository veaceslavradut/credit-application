package com.creditapp.shared.config;

import com.creditapp.shared.security.EncryptionService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for encryption services.
 * Provides a pass-through encryption service for tests to avoid encryption overhead.
 */
@TestConfiguration
@Profile("test")
public class TestEncryptionConfig {

    @Bean
    @Primary
    public EncryptionService testEncryptionService() {
        // Return pass-through encryption service for tests
        return new EncryptionService("");
    }
}
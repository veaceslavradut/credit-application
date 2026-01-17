package com.creditapp.shared.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test Redis configuration using TestContainers.
 * Automatically starts a Redis container for integration tests.
 */
@TestConfiguration
@Profile("test")
public class TestRedisConfig {

    // Lazy-initialized Redis container
    private static GenericContainer<?> redisContainer;

    @SuppressWarnings("resource")
    private static synchronized GenericContainer<?> getRedisContainer() {
        if (redisContainer == null) {
            redisContainer = new GenericContainer<>(
                DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
            redisContainer.start();
        }
        return redisContainer;
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        GenericContainer<?> container = getRedisContainer();
        String host = container.getHost();
        int port = container.getMappedPort(6379);
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        if (host != null) {
            factory.getStandaloneConfiguration().setHostName(host);
        }
        factory.getStandaloneConfiguration().setPort(port);
        factory.afterPropertiesSet();
        
        return factory;
    }
}
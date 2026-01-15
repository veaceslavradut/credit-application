package com.creditapp.shared.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private Connection dbConnection;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHealthEndpoint_AllServicesHealthy() throws Exception {
        // Given: database and redis are both healthy
        when(dataSource.getConnection()).thenReturn(dbConnection);
        when(dbConnection.isValid(2)).thenReturn(true);
        
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        // When: health endpoint is called
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then: should return 200 OK with all services connected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("connected", response.getBody().get("database"));
        assertEquals("connected", response.getBody().get("redis"));
        assertEquals("1.0.0", response.getBody().get("version"));
    }

    @Test
    void testHealthEndpoint_DatabaseUnhealthy() throws Exception {
        // Given: database is unhealthy
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Database connection failed"));
        
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        // When: health endpoint is called
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then: should return 503 SERVICE_UNAVAILABLE
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("disconnected", response.getBody().get("database"));
        assertEquals("connected", response.getBody().get("redis"));
    }

    @Test
    void testHealthEndpoint_RedisUnhealthy() throws Exception {
        // Given: redis is unhealthy
        when(dataSource.getConnection()).thenReturn(dbConnection);
        when(dbConnection.isValid(2)).thenReturn(true);
        
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Redis connection failed"));

        // When: health endpoint is called
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then: should return 503 SERVICE_UNAVAILABLE
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("connected", response.getBody().get("database"));
        assertEquals("disconnected", response.getBody().get("redis"));
    }

    @Test
    void testHealthEndpoint_AllServicesUnhealthy() throws Exception {
        // Given: both services are unhealthy
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Database connection failed"));
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Redis connection failed"));

        // When: health endpoint is called
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then: should return 503 SERVICE_UNAVAILABLE
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("disconnected", response.getBody().get("database"));
        assertEquals("disconnected", response.getBody().get("redis"));
        assertEquals("1.0.0", response.getBody().get("version"));
    }
}
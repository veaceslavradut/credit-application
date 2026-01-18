package com.creditapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpringContextIntegrationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // with the test profile using H2 database
    }
}
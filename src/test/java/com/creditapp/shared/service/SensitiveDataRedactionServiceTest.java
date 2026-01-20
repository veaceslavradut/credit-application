package com.creditapp.shared.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SensitiveDataRedactionServiceTest {

    private final SensitiveDataRedactionService redactionService = new SensitiveDataRedactionService();

    @Test
    void testSSNRedaction() {
        Map<String, Object> data = new HashMap<>();
        data.put("ssn", "123-45-6789");
        data.put("socialSecurityNumber", "987654321");

        Map<String, Object> redacted = redactionService.redactSensitiveData(data);

        assertEquals("***REDACTED***", redacted.get("ssn"));
        assertEquals("***REDACTED***", redacted.get("socialSecurityNumber"));
    }

    @Test
    void testEmailRedaction() {
        Map<String, Object> data = new HashMap<>();
        data.put("email", "user@example.com");
        data.put("contactEmail", "john.doe@company.org");

        Map<String, Object> redacted = redactionService.redactSensitiveData(data);

        String email1 = (String) redacted.get("email");
        String email2 = (String) redacted.get("contactEmail");

        assertTrue(email1.matches(".*\\*{3,}.*@example\\.com"));
        assertTrue(email2.matches(".*\\*{3,}.*@company\\.org"));
    }

    @Test
    void testPhoneNumberRedaction() {
        Map<String, Object> data = new HashMap<>();
        data.put("phone", "1234567890");
        data.put("phoneNumber", "5551234567");

        Map<String, Object> redacted = redactionService.redactSensitiveData(data);

        assertEquals("***REDACTED***", redacted.get("phone"));
        assertEquals("***REDACTED***", redacted.get("phoneNumber"));
    }

    @Test
    void testCreditCardRedaction() {
        Map<String, Object> data = new HashMap<>();
        data.put("cardNumber", "4111111111111111");
        data.put("creditCard", "5555444433332222");

        Map<String, Object> redacted = redactionService.redactSensitiveData(data);

        assertEquals("***REDACTED***", redacted.get("cardNumber"));
        assertEquals("***REDACTED***", redacted.get("creditCard"));
    }

    @Test
    void testNoRedactionForSafeFields() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John Doe");
        data.put("age", 30);
        data.put("city", "New York");

        Map<String, Object> redacted = redactionService.redactSensitiveData(data);

        assertEquals("John Doe", redacted.get("name"));
        assertEquals(30, redacted.get("age"));
        assertEquals("New York", redacted.get("city"));
    }
}
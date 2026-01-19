package com.creditapp.shared.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAnonymizationServiceTest {

    private DataAnonymizationService dataAnonymizationService;

    @BeforeEach
    void setUp() {
        dataAnonymizationService = new DataAnonymizationService();
    }

    @Test
    void testHashForAnonymity() {
        String input1 = "test@example.com";
        String hash1 = dataAnonymizationService.hashForAnonymity(input1);

        assertNotNull(hash1);
        assertEquals(8, hash1.length());

        String hash1_again = dataAnonymizationService.hashForAnonymity(input1);
        assertEquals(hash1, hash1_again);
    }

    @Test
    void testAnonymizeName() {
        String name = "John Doe";
        String anonymized = dataAnonymizationService.anonymizeName(name);

        assertNotNull(anonymized);
        assertTrue(anonymized.startsWith("Deleted User"));
        assertTrue(anonymized.length() > "Deleted User".length());
    }

    @Test
    void testAnonymizeEmail() {
        String email = "john.doe@example.com";
        String anonymized = dataAnonymizationService.anonymizeEmail(email);

        assertNotNull(anonymized);
        assertTrue(anonymized.endsWith("@deleted.local"));
        String localPart = anonymized.split("@")[0];
        assertEquals(8, localPart.length());
    }

    @Test
    void testHashConsistency() {
        String input = "consistent-test";
        String hash1 = dataAnonymizationService.hashForAnonymity(input);
        String hash2 = dataAnonymizationService.hashForAnonymity(input);

        assertEquals(hash1, hash2);
    }

    @Test
    void testEmptyStringHandling() {
        String hash = dataAnonymizationService.hashForAnonymity("");

        assertNotNull(hash);
        assertEquals(8, hash.length());
    }

    @Test
    void testNullNameHandling() {
        String result = dataAnonymizationService.anonymizeName(null);
        assertNotNull(result);
        assertTrue(result.startsWith("Deleted User"));
    }
}
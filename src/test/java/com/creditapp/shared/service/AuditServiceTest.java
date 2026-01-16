package com.creditapp.shared.service;

import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private RequestContextService requestContextService;

    @InjectMocks
    private AuditService auditService;

    private UUID testEntityId;
    private UUID testActorId;
    private String testEntityType;

    @BeforeEach
    void setUp() {
        testEntityId = UUID.randomUUID();
        testActorId = UUID.randomUUID();
        testEntityType = "Application";
    }

    @Test
    void testSanitizeValues_RemovesSensitiveFields() {
        // Arrange
        Map<String, Object> values = new HashMap<>();
        values.put("id", testEntityId);
        values.put("email", "test@example.com");
        values.put("password", "secretPassword123");
        values.put("ssn", "123-45-6789");
        values.put("creditCardNumber", "4111111111111111");
        values.put("status", "ACTIVE");

        // Act
        Map<String, Object> result = auditService.sanitizeValues(values);

        // Assert
        assertEquals(testEntityId, result.get("id"));
        assertEquals("test@example.com", result.get("email"));
        assertEquals("***REDACTED***", result.get("password"));
        assertEquals("***REDACTED***", result.get("ssn"));
        assertEquals("***REDACTED***", result.get("creditCardNumber"));
        assertEquals("ACTIVE", result.get("status"));
    }

    @Test
    void testSanitizeValues_HandlesNullInput() {
        // Act
        Map<String, Object> result = auditService.sanitizeValues(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testSanitizeValues_HandlesEmptyMap() {
        // Arrange
        Map<String, Object> values = new HashMap<>();

        // Act
        Map<String, Object> result = auditService.sanitizeValues(values);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testSanitizeValues_MultipleSensitiveFields() {
        // Arrange
        Map<String, Object> values = new HashMap<>();
        values.put("password", "secret");
        values.put("token", "abc123");
        values.put("ssn", "123-45-6789");
        values.put("creditCardNumber", "4111111111111111");
        values.put("cvv", "123");
        values.put("pin", "1234");

        // Act
        Map<String, Object> result = auditService.sanitizeValues(values);

        // Assert
        result.forEach((key, value) -> assertEquals("***REDACTED***", value, "Field " + key + " should be redacted"));
    }

    @Test
    void testSanitizeValues_PartiallyRedacted() {
        // Arrange
        Map<String, Object> values = new HashMap<>();
        values.put("username", "john_doe");
        values.put("password", "secretPassword");
        values.put("email", "john@example.com");

        // Act
        Map<String, Object> result = auditService.sanitizeValues(values);

        // Assert
        assertEquals("john_doe", result.get("username"));
        assertEquals("***REDACTED***", result.get("password"));
        assertEquals("john@example.com", result.get("email"));
    }

    @Test
    void testGetAuditLogsForEntity() {
        // Arrange
        AuditLog log1 = new AuditLog();
        log1.setId(1L);
        log1.setEntityType(testEntityType);
        log1.setEntityId(testEntityId);
        log1.setAction(AuditAction.APPLICATION_CREATED);

        List<AuditLog> logs = List.of(log1);
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(testEntityType, testEntityId))
                .thenReturn(logs);

        // Act
        List<AuditLog> result = auditService.getAuditLogsForEntity(testEntityType, testEntityId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(AuditAction.APPLICATION_CREATED, result.get(0).getAction());
        verify(auditLogRepository, times(1))
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(testEntityType, testEntityId);
    }

    @Test
    void testGetAuditLogsByActor() {
        // Arrange
        AuditLog log1 = new AuditLog();
        log1.setId(1L);
        log1.setActorId(testActorId);

        List<AuditLog> logs = List.of(log1);
        when(auditLogRepository.findByActorIdOrderByCreatedAtDesc(testActorId)).thenReturn(logs);

        // Act
        List<AuditLog> result = auditService.getAuditLogsByActor(testActorId);

        // Assert
        assertEquals(1, result.size());
        verify(auditLogRepository, times(1)).findByActorIdOrderByCreatedAtDesc(testActorId);
    }
}

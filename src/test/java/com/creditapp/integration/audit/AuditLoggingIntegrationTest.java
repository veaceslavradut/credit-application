package com.creditapp.integration.audit;

import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import com.creditapp.shared.service.AuditLogArchivalService;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.RequestContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditLoggingIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RequestContextService requestContextService;

    @Autowired
    private AuditLogArchivalService archivalService;

    private UUID testUserId;
    private UUID testEntityId;
    private static final String TEST_USER_ROLE = "BORROWER";

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();
        auditLogRepository.deleteAll();
    }

    @Test
    void testBasicAuditLogging() {
        // Given: An audit event
        AuditAction action = AuditAction.USER_REGISTERED;

        // When: Logging the action
        auditService.logAction("User", testEntityId, action, testUserId, TEST_USER_ROLE);

        // Then: Audit log is created
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("User", testEntityId, null).getContent();
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals("User", log.getEntityType());
        assertEquals(testEntityId, log.getEntityId());
        assertEquals(action, log.getAction());
        assertEquals(testUserId, log.getActorId());
        assertEquals(TEST_USER_ROLE, log.getActorRole());
    }

    @Test
    void testAuditLoggingWithValues() {
        // Given: Audit event with old and new values
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("email", "old@example.com");
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("email", "new@example.com");

        // When: Logging with values
        auditService.logActionWithValues("User", testEntityId, AuditAction.PROFILE_UPDATED,
                testUserId, TEST_USER_ROLE, oldValues, newValues);

        // Then: Old and new values are captured
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("User", testEntityId, null).getContent();
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertNotNull(log.getOldValues());
        assertNotNull(log.getNewValues());
    }

    @Test
    void testSensitiveFieldSanitization() {
        // Given: Audit event with sensitive fields
        Map<String, Object> values = new HashMap<>();
        values.put("email", "user@example.com");
        values.put("password", "secretPassword123");
        values.put("ssn", "123-45-6789");

        // When: Logging with sensitive fields
        auditService.logActionWithValues("User", testEntityId, AuditAction.PASSWORD_CHANGED,
                testUserId, TEST_USER_ROLE, null, values);

        // Then: Sensitive fields are redacted
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("User", testEntityId, null).getContent();
        AuditLog log = logs.get(0);
        
        assertEquals("user@example.com", log.getNewValues().get("email"));
        assertEquals("[REDACTED]", log.getNewValues().get("password"));
        assertEquals("[REDACTED]", log.getNewValues().get("ssn"));
    }

    @Test
    void testMultipleActionsForSameEntity() {
        // Given: Multiple audit events for same entity
        UUID userId = UUID.randomUUID();

        // When: Logging multiple actions
        auditService.logAction("User", userId, AuditAction.USER_REGISTERED, userId, TEST_USER_ROLE);
        auditService.logAction("User", userId, AuditAction.USER_LOGGED_IN, userId, TEST_USER_ROLE);
        auditService.logAction("User", userId, AuditAction.PROFILE_UPDATED, userId, TEST_USER_ROLE);

        // Then: All actions are captured
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("User", userId, null).getContent();
        assertEquals(3, logs.size());
        assertEquals(AuditAction.USER_REGISTERED, logs.get(0).getAction());
        assertEquals(AuditAction.USER_LOGGED_IN, logs.get(1).getAction());
        assertEquals(AuditAction.PROFILE_UPDATED, logs.get(2).getAction());
    }

    @Test
    void testAuditLogsByActor() {
        // Given: Multiple users performing actions
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        // When: Logging actions from different users
        auditService.logAction("User", testEntityId, AuditAction.USER_REGISTERED, user1, TEST_USER_ROLE);
        auditService.logAction("User", testEntityId, AuditAction.PROFILE_UPDATED, user2, TEST_USER_ROLE);

        // Then: Can filter by actor
        List<AuditLog> user1Logs = auditLogRepository.findByActorId(user1, null).getContent();
        List<AuditLog> user2Logs = auditLogRepository.findByActorId(user2, null).getContent();
        
        assertEquals(1, user1Logs.size());
        assertEquals(1, user2Logs.size());
        assertEquals(user1, user1Logs.get(0).getActorId());
        assertEquals(user2, user2Logs.get(0).getActorId());
    }

    @Test
    void testAuditLogsByDateRange() {
        // Given: Audit events
        auditService.logAction("User", testEntityId, AuditAction.USER_REGISTERED, testUserId, TEST_USER_ROLE);

        // When: Querying date range
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        List<AuditLog> logs = auditLogRepository.findByCreatedAtBetween(startDate, endDate, null).getContent();

        // Then: Logs within range are returned
        assertEquals(1, logs.size());
    }

    @Test
    void testAllAuditActionTypes() {
        // Given: All audit action types
        AuditAction[] actions = AuditAction.values();

        // When: Logging each action
        for (AuditAction action : actions) {
            auditService.logAction("Entity", UUID.randomUUID(), action, testUserId, TEST_USER_ROLE);
        }

        // Then: All actions are logged
        long totalLogs = auditLogRepository.count();
        assertEquals(actions.length, totalLogs);
    }

    @Test
    void testAuditLogWithNullValues() {
        // Given: Audit event with null actor
        // When: Logging without user context
        auditService.logAction("Application", testEntityId, AuditAction.APPLICATION_CREATED, null, "SYSTEM");

        // Then: Audit log is created with null actor
        AuditLog log = auditLogRepository.findByEntityTypeAndEntityId("Application", testEntityId, null).getContent().get(0);
        assertNull(log.getActorId());
        assertEquals("SYSTEM", log.getActorRole());
    }

    @Test
    void testCreditCardSanitization() {
        // Given: Values with credit card
        Map<String, Object> values = new HashMap<>();
        values.put("creditCard", "4532-1234-5678-9010");
        values.put("amount", "1000.00");

        // When: Logging
        auditService.logActionWithValues("Transaction", testEntityId, AuditAction.APPLICATION_SUBMITTED,
                testUserId, TEST_USER_ROLE, null, values);

        // Then: Credit card is redacted
        AuditLog log = auditLogRepository.findByEntityTypeAndEntityId("Transaction", testEntityId, null).getContent().get(0);
        assertEquals("[REDACTED]", log.getNewValues().get("creditCard"));
        assertEquals("1000.00", log.getNewValues().get("amount"));
    }

    @Test
    void testBankAccountSanitization() {
        // Given: Values with bank account
        Map<String, Object> values = new HashMap<>();
        values.put("bankAccount", "DE89370400440532013000");
        values.put("bankName", "Example Bank");

        // When: Logging
        auditService.logActionWithValues("Organization", testEntityId, AuditAction.BANK_REGISTERED,
                testUserId, TEST_USER_ROLE, null, values);

        // Then: Bank account is redacted
        AuditLog log = auditLogRepository.findByEntityTypeAndEntityId("Organization", testEntityId, null).getContent().get(0);
        assertEquals("[REDACTED]", log.getNewValues().get("bankAccount"));
        assertEquals("Example Bank", log.getNewValues().get("bankName"));
    }

    @Test
    void testTokenSanitization() {
        // Given: Values with various tokens
        Map<String, Object> values = new HashMap<>();
        values.put("accessToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        values.put("refreshToken", "refresh_token_value_here");
        values.put("apiKey", "sk-1234567890abcdef");

        // When: Logging
        auditService.logActionWithValues("User", testEntityId, AuditAction.PASSWORD_CHANGED,
                testUserId, TEST_USER_ROLE, null, values);

        // Then: All tokens are redacted
        AuditLog log = auditLogRepository.findByEntityTypeAndEntityId("User", testEntityId, null).getContent().get(0);
        assertEquals("[REDACTED]", log.getNewValues().get("accessToken"));
        assertEquals("[REDACTED]", log.getNewValues().get("refreshToken"));
        assertEquals("[REDACTED]", log.getNewValues().get("apiKey"));
    }
}
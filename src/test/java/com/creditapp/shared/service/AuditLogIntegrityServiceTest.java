package com.creditapp.shared.service;

import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuditLogIntegrityServiceTest {

    private final AuditLogIntegrityService integrityService = new AuditLogIntegrityService();

    @Test
    void testCalculateHash() {
        AuditLog auditLog = new AuditLog(
                "CreditApplication",
                UUID.randomUUID(),
                AuditAction.APPLICATION_CREATED,
                UUID.randomUUID(),
                "User",
                null,
                null,
                "127.0.0.1",
                "TestAgent",
                "SUCCESS"
        );

        String hash = integrityService.calculateHash(auditLog);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    void testHashConsistency() {
        UUID actorId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        
        AuditLog auditLog1 = new AuditLog(
                "CreditApplication",
                entityId,
                AuditAction.APPLICATION_VIEWED,
                actorId,
                "User",
                null,
                null,
                "127.0.0.1",
                "TestAgent",
                "SUCCESS"
        );

        AuditLog auditLog2 = new AuditLog(
                "CreditApplication",
                entityId,
                AuditAction.APPLICATION_VIEWED,
                actorId,
                "User",
                null,
                null,
                "127.0.0.1",
                "TestAgent",
                "SUCCESS"
        );

        String hash1 = integrityService.calculateHash(auditLog1);
        String hash2 = integrityService.calculateHash(auditLog2);

        assertEquals(hash1, hash2);
    }

    @Test
    void testVerifyIntegrity() {
        AuditLog auditLog = new AuditLog(
                "Document",
                UUID.randomUUID(),
                AuditAction.DOCUMENT_DELETED,
                UUID.randomUUID(),
                "User",
                null,
                null,
                "127.0.0.1",
                "TestAgent",
                "SUCCESS"
        );

        String hash = integrityService.calculateHash(auditLog);
        boolean isValid = integrityService.verifyIntegrity(auditLog, hash);

        assertTrue(isValid);
    }

    @Test
    void testVerifyIntegrityWithTamperedData() {
        AuditLog auditLog = new AuditLog(
                "CreditApplication",
                UUID.randomUUID(),
                AuditAction.APPLICATION_UPDATED,
                UUID.randomUUID(),
                "User",
                null,
                null,
                "127.0.0.1",
                "TestAgent",
                "SUCCESS"
        );

        String hash = integrityService.calculateHash(auditLog);
        auditLog.setAction(AuditAction.APPLICATION_SUBMITTED);
        boolean isValid = integrityService.verifyIntegrity(auditLog, hash);

        assertFalse(isValid);
    }
}
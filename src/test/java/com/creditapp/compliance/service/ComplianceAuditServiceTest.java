package com.creditapp.compliance.service;

import com.creditapp.compliance.dto.AuditLogFilterDTO;
import com.creditapp.compliance.dto.AuditLogResponse;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import com.creditapp.shared.service.DataRedactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComplianceAuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private DataRedactionService dataRedactionService;

    @InjectMocks
    private ComplianceAuditService complianceAuditService;

    @Test
    void testGetAuditLogsWithUserIdFilter() {
        UUID userId = UUID.randomUUID();
        AuditLogFilterDTO filter = new AuditLogFilterDTO();
        filter.setUserId(userId);
        filter.setPage(0);
        filter.setSize(100);

        AuditLog auditLog = createSampleAuditLog(userId);
        Page<AuditLog> mockPage = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 100), 1);
        when(auditLogRepository.findByActorIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(mockPage);
        when(dataRedactionService.redactSensitiveData(any())).thenReturn(null);

        Page<AuditLogResponse> result = complianceAuditService.getAuditLogs(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals(userId, result.getContent().get(0).getUserId());
        verify(auditLogRepository, times(1)).findByActorIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class));
    }

    @Test
    void testExportAuditLogsAsCSV() {
        AuditLogFilterDTO filter = new AuditLogFilterDTO();
        filter.setPage(0);
        filter.setSize(100);

        AuditLog auditLog = createSampleAuditLog(UUID.randomUUID());
        Page<AuditLog> mockPage = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 100), 1);
        when(auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(mockPage);
        when(dataRedactionService.redactSensitiveData(any())).thenReturn(null);

        byte[] csv = complianceAuditService.exportAuditLogsAsCSV(filter);

        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Timestamp,User ID,Action,Result"));
        assertTrue(csvContent.contains("SUCCESS"));
    }

    private AuditLog createSampleAuditLog(UUID userId) {
        return new AuditLog(
                "CreditApplication",
                UUID.randomUUID(),
                AuditAction.APPLICATION_CREATED,
                userId,
                "User",
                null,
                null,
                "127.0.0.1",
                "TestAgent",
                "SUCCESS"
        );
    }
}
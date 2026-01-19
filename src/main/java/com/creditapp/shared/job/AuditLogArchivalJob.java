package com.creditapp.shared.job;

import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogArchivalJob {
    private final AuditLogRepository auditLogRepository;

    @Value("${app.audit.retention.days:1095}")
    private Integer retentionDays;

    @Scheduled(cron = "0 0 2 1 * ?") // Monthly on 1st at 2 AM
    public void archiveOldAuditLogs() {
        log.info("Starting audit log archival job. Retention: {} days", retentionDays);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        List<AuditLog> oldLogs = auditLogRepository.findByCreatedAtBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), cutoffDate
        );
        log.info("Found {} audit logs older than retention period for archival", oldLogs.size());
        // In production: export to S3 Glacier or data warehouse
        // For now: just log count (deletion prevented by DB trigger)
    }
}
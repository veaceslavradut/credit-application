package com.creditapp.shared.service;

import com.creditapp.shared.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class AuditLogArchivalService {
    private final EntityManager entityManager;
    private final AuditLogRepository auditLogRepository;

    @Value("${audit.retention.years:3}")
    private int retentionYears;

    public AuditLogArchivalService(EntityManager entityManager,
                                   AuditLogRepository auditLogRepository) {
        this.entityManager = entityManager;
        this.auditLogRepository = auditLogRepository;
    }

    public void archiveOldAuditLogs() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusYears(retentionYears);
            log.info("Starting audit log archival for logs older than {}", cutoffDate);

            long rowsAffected = archiveLogsOlderThan(cutoffDate);
            log.info("Archived {} audit log entries to audit_logs_archive table", rowsAffected);
        } catch (Exception e) {
            log.error("Failed to archive old audit logs", e);
        }
    }

    private long archiveLogsOlderThan(LocalDateTime cutoffDate) {
        String sql = "INSERT INTO audit_logs_archive (id, entity_type, entity_id, action, actor_id, actor_role, " +
                     "old_values, new_values, ip_address, user_agent, created_at) " +
                     "SELECT id, entity_type, entity_id, action, actor_id, actor_role, " +
                     "old_values, new_values, ip_address, user_agent, created_at " +
                     "FROM audit_logs WHERE created_at < :cutoffDate";

        int count = entityManager.createNativeQuery(sql)
                .setParameter("cutoffDate", cutoffDate)
                .executeUpdate();

        if (count > 0) {
            String deleteSql = "DELETE FROM audit_logs WHERE created_at < :cutoffDate";
            entityManager.createNativeQuery(deleteSql)
                    .setParameter("cutoffDate", cutoffDate)
                    .executeUpdate();
        }

        return count;
    }

    public long countArchivedLogs() {
        String sql = "SELECT COUNT(*) FROM audit_logs_archive";
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return ((Number) result).longValue();
    }

    public long countActiveLogs() {
        return auditLogRepository.count();
    }
}
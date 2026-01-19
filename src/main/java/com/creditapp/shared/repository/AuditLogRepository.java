package com.creditapp.shared.repository;

import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    /**
     * Find all audit logs for a specific entity
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);

    /**
     * Find all audit logs by actor
     */
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId);
    Page<AuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId, Pageable pageable);

    /**
     * Find audit logs within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by entity type and action
     */
    List<AuditLog> findByEntityTypeAndActionOrderByCreatedAtDesc(String entityType, AuditAction action);
    Page<AuditLog> findByEntityTypeAndActionOrderByCreatedAtDesc(String entityType, AuditAction action, Pageable pageable);

    /**
     * Find audit logs by actor and action
     */
    List<AuditLog> findByActorIdAndActionOrderByCreatedAtDesc(UUID actorId, AuditAction action);
    Page<AuditLog> findByActorIdAndActionOrderByCreatedAtDesc(UUID actorId, AuditAction action, Pageable pageable);

    Page<AuditLog> findByResultOrderByCreatedAtDesc(String result, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Count audit logs for specific entity
     */
    long countByEntityTypeAndEntityId(String entityType, UUID entityId);
}

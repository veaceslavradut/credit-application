package com.creditapp.compliance.controller;

import com.creditapp.shared.dto.AuditLogDTO;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for compliance officers to query audit logs
 * Access restricted to COMPLIANCE_OFFICER role
 */
@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final AuditService auditService;

    /**
     * Get audit logs for a specific entity
     * GET /api/compliance/audit-logs?entityType=Application&entityId=uuid
     */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam String entityType,
            @RequestParam UUID entityId) {
        List<AuditLog> auditLogs = auditService.getAuditLogsForEntity(entityType, entityId);
        List<AuditLogDTO> dtos = auditLogs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get audit logs by actor
     * GET /api/compliance/audit-logs/actor?actorId=uuid
     */
    @GetMapping("/audit-logs/actor")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByActor(
            @RequestParam UUID actorId) {
        List<AuditLog> auditLogs = auditService.getAuditLogsByActor(actorId);
        List<AuditLogDTO> dtos = auditLogs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Convert AuditLog entity to DTO (excludes sensitive fields)
     */
    private AuditLogDTO convertToDTO(AuditLog auditLog) {
        return new AuditLogDTO(
                auditLog.getId(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getAction(),
                auditLog.getActorId(),
                auditLog.getActorRole(),
                auditLog.getOldValues(),
                auditLog.getNewValues(),
                auditLog.getCreatedAt()
        );
    }
}

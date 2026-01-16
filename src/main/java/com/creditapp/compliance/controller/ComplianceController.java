package com.creditapp.compliance.controller;

import com.creditapp.shared.dto.AuditLogDTO;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.RequestContextService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final RequestContextService requestContextService;

    public ComplianceController(AuditLogRepository auditLogRepository,
                                AuditService auditService,
                                RequestContextService requestContextService) {
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
        this.requestContextService = requestContextService;
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('COMPLIANCE_OFFICER')")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {

        Page<AuditLog> auditLogs;

        if (entityType != null && entityId != null) {
            auditLogs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        } else if (actorId != null) {
            auditLogs = auditLogRepository.findByActorId(actorId, pageable);
        } else if (startDate != null && endDate != null) {
            auditLogs = auditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        } else {
            auditLogs = auditLogRepository.findAll(pageable);
        }

        List<AuditLogDTO> dtos = auditLogs.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<AuditLogDTO> result = new PageImpl<>(dtos, pageable, auditLogs.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/audit-logs/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE_OFFICER')")
    public ResponseEntity<AuditLogDTO> getAuditLog(@PathVariable Long id) {
        return auditLogRepository.findById(id)
                .map(auditLog -> ResponseEntity.ok(convertToDTO(auditLog)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/audit-logs/user/{userId}")
    @PreAuthorize("hasAuthority('COMPLIANCE_OFFICER')")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsByUser(
            @PathVariable UUID userId,
            Pageable pageable) {

        Page<AuditLog> auditLogs = auditLogRepository.findByActorId(userId, pageable);
        List<AuditLogDTO> dtos = auditLogs.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<AuditLogDTO> result = new PageImpl<>(dtos, pageable, auditLogs.getTotalElements());
        return ResponseEntity.ok(result);
    }

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
                auditLog.getIpAddress(),
                auditLog.getCreatedAt()
        );
    }
}
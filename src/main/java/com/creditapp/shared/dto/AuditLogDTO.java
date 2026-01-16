package com.creditapp.shared.dto;

import com.creditapp.shared.model.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for returning audit log information via API
 * Note: Excludes sensitive fields (ipAddress, userAgent) for security
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String entityType;
    private UUID entityId;
    private AuditAction action;
    private UUID actorId;
    private String actorRole;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private LocalDateTime createdAt;
}

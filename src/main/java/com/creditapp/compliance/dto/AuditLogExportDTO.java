package com.creditapp.compliance.dto;

import com.creditapp.shared.model.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AuditLogExportDTO {
    private LocalDateTime timestamp;
    private UUID userId;
    private AuditAction action;
    private String result;
    private String ipAddress;
    private String userAgent;
    private String resourceType;
    private UUID resourceId;
}

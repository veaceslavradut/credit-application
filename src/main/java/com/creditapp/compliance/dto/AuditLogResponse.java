package com.creditapp.compliance.dto;

import com.creditapp.shared.model.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private UUID userId;
    private AuditAction action;
    private String result;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private UUID resourceId;
    private String resourceType;
    private Map<String, Object> details;
}

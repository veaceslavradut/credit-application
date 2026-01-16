package com.creditapp.shared.service;

import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final RequestContextService requestContextService;
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>();

    static {
        SENSITIVE_FIELDS.add("password");
        SENSITIVE_FIELDS.add("passwordhash");
        SENSITIVE_FIELDS.add("refreshtoken");
        SENSITIVE_FIELDS.add("accesstoken");
        SENSITIVE_FIELDS.add("apikey");
        SENSITIVE_FIELDS.add("secret");
        SENSITIVE_FIELDS.add("ssn");
        SENSITIVE_FIELDS.add("creditcard");
        SENSITIVE_FIELDS.add("bankaccount");
        SENSITIVE_FIELDS.add("token");
    }

    public AuditService(AuditLogRepository auditLogRepository,
                       RequestContextService requestContextService) {
        this.auditLogRepository = auditLogRepository;
        this.requestContextService = requestContextService;
    }

    public void logAction(String entityType, UUID entityId, AuditAction action, UUID actorId, String actorRole) {
        try {
            String ipAddress = requestContextService.getCurrentIpAddress();
            String userAgent = requestContextService.getCurrentUserAgent();

            AuditLog auditLog = new AuditLog(
                    entityType,
                    entityId,
                    action,
                    actorId,
                    actorRole,
                    null,
                    null,
                    ipAddress,
                    userAgent
            );
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for action: {}", action);
        } catch (Exception e) {
            log.error("Failed to log audit action: {}", action, e);
        }
    }

    public void logActionWithValues(String entityType, UUID entityId, AuditAction action,
                                   UUID actorId, String actorRole,
                                   Map<String, Object> oldValues, Map<String, Object> newValues) {
        try {
            String ipAddress = requestContextService.getCurrentIpAddress();
            String userAgent = requestContextService.getCurrentUserAgent();

            Map<String, Object> sanitizedOldValues = sanitizeValues(oldValues);
            Map<String, Object> sanitizedNewValues = sanitizeValues(newValues);

            AuditLog auditLog = new AuditLog(
                    entityType,
                    entityId,
                    action,
                    actorId,
                    actorRole,
                    sanitizedOldValues,
                    sanitizedNewValues,
                    ipAddress,
                    userAgent
            );
            auditLogRepository.save(auditLog);
            log.debug("Audit log with values created for action: {}", action);
        } catch (Exception e) {
            log.error("Failed to log audit action with values: {}", action, e);
        }
    }

    private Map<String, Object> sanitizeValues(Map<String, Object> values) {
        if (values == null) return null;
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            if (isSensitiveField(key)) {
                sanitized.put(key, "[REDACTED]");
            } else {
                sanitized.put(key, entry.getValue());
            }
        }
        return sanitized;
    }

    private boolean isSensitiveField(String fieldName) {
        return fieldName != null && SENSITIVE_FIELDS.contains(fieldName.toLowerCase());
    }
}
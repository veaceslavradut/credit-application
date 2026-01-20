package com.creditapp.shared.service;

import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import com.creditapp.shared.security.DataRedactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Core service for audit logging
 * Provides methods to log actions with full context
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final RequestContextService requestContextService;
    private final DataRedactionService dataRedactionService;

    // PII fields that should be sanitized from audit logs
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "passwordHash", "token", "refreshToken", "accessToken",
            "ssn", "socialSecurityNumber", "creditCardNumber", "cvv", "pin"
    );

    /**
     * Log a simple action without old/new values
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityType, UUID entityId, AuditAction action) {
        logActionWithValues(entityType, entityId, action, null, null);
    }

    /**
     * Log an action with explicit actor context (for non-HTTP contexts)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityType, UUID entityId, AuditAction action,
                         UUID actorId, String actorRole) {
        logActionWithValues(entityType, entityId, action, actorId, actorRole, null, null, null, null);
    }

    /**
     * Log an action with old and new values for field-level tracking
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActionWithValues(String entityType, UUID entityId, AuditAction action,
                                   Map<String, Object> oldValues, Map<String, Object> newValues) {
        // Extract current user context from Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID actorId = null;
        String actorRole = null;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                // Assuming principal has getId() method (UserDetailsImpl or similar)
                Object principal = auth.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    // Extract user ID from username (assuming UUID format)
                    String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                    actorId = UUID.fromString(username);
                }
                actorRole = auth.getAuthorities().stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("UNKNOWN");
            } catch (Exception e) {
                log.warn("Failed to extract actor context from authentication: {}", e.getMessage());
            }
        }

        // Extract request context (IP, user agent)
        RequestContextService.RequestContext requestContext = requestContextService.getRequestContext();

        logActionWithValues(entityType, entityId, action, actorId, actorRole,
                oldValues, newValues, requestContext.getIpAddress(), requestContext.getUserAgent());
    }

    /**
     * Log an action with full context (all parameters)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActionWithValues(String entityType, UUID entityId, AuditAction action,
                                   UUID actorId, String actorRole,
                                   Map<String, Object> oldValues, Map<String, Object> newValues,
                                   String ipAddress, String userAgent) {
        try {
            // Sanitize sensitive fields
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
                userAgent,
                "SUCCESS"
            );

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: entityType={}, entityId={}, action={}, actor={}, role={}",
                    entityType, entityId, action, actorId, actorRole);
        } catch (Exception e) {
            // Never fail business operations due to audit logging failures
            log.error("Failed to create audit log: entityType={}, entityId={}, action={} - {}",
                    entityType, entityId, action, e.getMessage(), e);
        }
    }

    /**
     * Sanitize sensitive fields from values map
     * Removes or masks PII and security-sensitive data
     */
    public Map<String, Object> sanitizeValues(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return values;
        }

        // First apply data redaction service for PII fields
        Map<String, Object> sanitized = dataRedactionService.redactAuditDetails(values);
        
        // Then apply additional security-sensitive field redaction
        for (String sensitiveField : SENSITIVE_FIELDS) {
            if (sanitized.containsKey(sensitiveField)) {
                sanitized.put(sensitiveField, "***REDACTED***");
            }
        }
        return sanitized;
    }

    /**
     * Get audit logs for a specific entity
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForEntity(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Get audit logs by actor
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByActor(UUID actorId) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId);
    }
}

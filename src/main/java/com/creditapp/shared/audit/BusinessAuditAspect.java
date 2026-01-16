package com.creditapp.shared.audit;

import com.creditapp.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * AOP Aspect that intercepts methods annotated with @BusinessAudit
 * and logs audit events via AuditService
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class BusinessAuditAspect {

    private final AuditService auditService;

    /**
     * Log successful business operations
     */
    @AfterReturning(pointcut = "@annotation(businessAudit)", returning = "result")
    public void logBusinessAction(JoinPoint joinPoint, BusinessAudit businessAudit, Object result) {
        try {
            UUID entityId = extractEntityId(result);
            String entityType = businessAudit.entityType();
            
            if (entityId != null) {
                auditService.logAction(entityType, entityId, businessAudit.action());
            } else {
                log.warn("Could not extract entity ID from result of method: {}", joinPoint.getSignature().getName());
            }
        } catch (Exception e) {
            log.error("Error in BusinessAuditAspect: {}", e.getMessage(), e);
            // Don't fail business operation due to audit logging error
        }
    }

    /**
     * Log failed business operations
     */
    @AfterThrowing(pointcut = "@annotation(businessAudit)", throwing = "ex")
    public void logFailedAction(JoinPoint joinPoint, BusinessAudit businessAudit, Exception ex) {
        try {
            // For failed operations, try to extract entity ID from method parameters
            UUID entityId = extractEntityIdFromParams(joinPoint);
            String entityType = businessAudit.entityType();
            
            if (entityId != null) {
                log.warn("Business action failed - entityType: {}, action: {}, error: {}",
                        entityType, businessAudit.action(), ex.getMessage());
                // Optionally log the failure
            }
        } catch (Exception e) {
            log.error("Error logging failed action: {}", e.getMessage(), e);
        }
    }

    /**
     * Try to extract entity ID from method return value
     * Looks for getId() or id field
     */
    private UUID extractEntityId(Object result) {
        if (result == null) {
            return null;
        }

        try {
            // Try getId() method first
            var getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            if (id instanceof UUID) {
                return (UUID) id;
            }
        } catch (Exception e) {
            // Method doesn't exist, try next approach
        }

        try {
            // Try id field
            var idField = result.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(result);
            if (id instanceof UUID) {
                return (UUID) id;
            }
        } catch (Exception e) {
            // Field doesn't exist, log and return null
        }

        return null;
    }

    /**
     * Extract entity ID from method parameters
     * Looks for first parameter with getId() method
     */
    private UUID extractEntityIdFromParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            UUID id = extractEntityId(arg);
            if (id != null) {
                return id;
            }
        }
        return null;
    }
}

package com.creditapp.shared.audit;

import com.creditapp.shared.model.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark business methods for automatic audit logging via AOP
 * 
 * Example:
 * @BusinessAudit(action = AuditAction.APPLICATION_CREATED, entityType = "Application")
 * public Application createApplication(CreateApplicationRequest request) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessAudit {
    
    /**
     * The audit action to log
     */
    AuditAction action();
    
    /**
     * The entity type being audited
     */
    String entityType();
    
    /**
     * Whether to capture return value as newValues (default: false)
     * Only works if return type has accessible fields
     */
    boolean captureReturnValue() default false;
}

package com.creditapp.shared.listener;

import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.RequestContextService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class EntityAuditListener {
    private static AuditService auditService;
    private static RequestContextService requestContextService;

    @Autowired
    public void setAuditService(AuditService auditService) {
        EntityAuditListener.auditService = auditService;
    }

    @Autowired
    public void setRequestContextService(RequestContextService requestContextService) {
        EntityAuditListener.requestContextService = requestContextService;
    }

    @PostPersist
    public void onUserCreate(User user) {
        try {
            UUID actorId = requestContextService.getCurrentUserId();
            String actorRole = requestContextService.getCurrentUserRole();
            
            Map<String, Object> newValues = new HashMap<>();
            newValues.put("id", user.getId());
            newValues.put("email", user.getEmail());
            newValues.put("firstName", user.getFirstName());
            newValues.put("lastName", user.getLastName());
            newValues.put("phone", user.getPhone());

            auditService.logActionWithValues("User", user.getId(), AuditAction.USER_REGISTERED,
                    actorId, actorRole, null, newValues);
            log.debug("User creation audited: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to audit user creation", e);
        }
    }

    @PostUpdate
    public void onUserUpdate(User user) {
        try {
            UUID actorId = requestContextService.getCurrentUserId();
            String actorRole = requestContextService.getCurrentUserRole();

            Map<String, Object> newValues = new HashMap<>();
            newValues.put("firstName", user.getFirstName());
            newValues.put("lastName", user.getLastName());
            newValues.put("phone", user.getPhone());

            auditService.logActionWithValues("User", user.getId(), AuditAction.PROFILE_UPDATED,
                    actorId, actorRole, new HashMap<>(), newValues);
            log.debug("User update audited: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to audit user update", e);
        }
    }

    @PostPersist
    public void onOrganizationCreate(Organization org) {
        try {
            UUID actorId = requestContextService.getCurrentUserId();
            String actorRole = requestContextService.getCurrentUserRole();

            Map<String, Object> newValues = new HashMap<>();
            newValues.put("id", org.getId());
            newValues.put("name", org.getName());
            newValues.put("registrationNumber", org.getRegistrationNumber());
            newValues.put("taxId", org.getTaxId());

            auditService.logActionWithValues("Organization", org.getId(), AuditAction.BANK_REGISTERED,
                    actorId, actorRole, null, newValues);
            log.debug("Organization creation audited: {}", org.getId());
        } catch (Exception e) {
            log.error("Failed to audit organization creation", e);
        }
    }

    @PostUpdate
    public void onOrganizationUpdate(Organization org) {
        try {
            UUID actorId = requestContextService.getCurrentUserId();
            String actorRole = requestContextService.getCurrentUserRole();

            Map<String, Object> newValues = new HashMap<>();
            newValues.put("name", org.getName());
            newValues.put("status", org.getStatus());
            newValues.put("activatedAt", org.getActivatedAt());

            auditService.logActionWithValues("Organization", org.getId(), AuditAction.BANK_ACTIVATED,
                    actorId, actorRole, new HashMap<>(), newValues);
            log.debug("Organization update audited: {}", org.getId());
        } catch (Exception e) {
            log.error("Failed to audit organization update", e);
        }
    }
}
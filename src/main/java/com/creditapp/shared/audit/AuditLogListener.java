package com.creditapp.shared.audit;

import com.creditapp.auth.model.User;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple JPA entity listener to demonstrate audit capture hooks.
 * In later stories, wire this to an AuditService for persistence.
 */
public class AuditLogListener {

    private static final Logger log = LoggerFactory.getLogger(AuditLogListener.class);

    @PrePersist
    public void prePersist(User user) {
        log.info("AUDIT CREATE user={} email={}", user.getId(), user.getEmail());
    }

    @PreUpdate
    public void preUpdate(User user) {
        log.info("AUDIT UPDATE user={} email={}", user.getId(), user.getEmail());
    }

    @PreRemove
    public void preRemove(User user) {
        log.info("AUDIT DELETE user={} email={}", user.getId(), user.getEmail());
    }
}

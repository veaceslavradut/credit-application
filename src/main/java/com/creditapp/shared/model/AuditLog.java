package com.creditapp.shared.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_entity_type_id", columnList = "entity_type,entity_id"),
        @Index(name = "idx_actor_id", columnList = "actor_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_role")
    private String actorRole;

    @Type(JsonBinaryType.class)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private Map<String, Object> oldValues;

    @Type(JsonBinaryType.class)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private Map<String, Object> newValues;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "result", nullable = false)
    private String result;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog(String entityType, UUID entityId, AuditAction action,
                   UUID actorId, String actorRole,
                   Map<String, Object> oldValues, Map<String, Object> newValues,
                   String ipAddress, String userAgent, String result) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.result = result;
    }
}
package com.creditapp.borrower.model;

import com.creditapp.shared.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ApplicationHistory entity for tracking application status transitions.
 * Immutable audit trail of all status changes.
 */
@Entity
@Table(name = "application_history", indexes = {
        @Index(name = "idx_application_history_application_id", columnList = "application_id"),
        @Index(name = "idx_application_history_application_changed", columnList = "application_id, changed_at DESC"),
        @Index(name = "idx_application_history_changed_at", columnList = "changed_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "old_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus oldStatus;

    @Column(name = "new_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus newStatus;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by_user_id")
    private UUID changedByUserId;

    @Column(name = "change_reason")
    private String changeReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", insertable = false, updatable = false)
    private User changedByUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationHistory that = (ApplicationHistory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

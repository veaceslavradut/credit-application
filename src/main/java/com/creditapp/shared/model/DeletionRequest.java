package com.creditapp.shared.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deletion_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID borrowerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeletionStatus status;

    @Column(length = 255, unique = true)
    private String confirmationToken;

    private LocalDateTime confirmationTokenExpiresAt;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime completedAt;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
package com.creditapp.shared.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing in-portal notifications for banks
 * Story 4.6: Offer Expiration Notification - Task 4
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_bank_created", columnList = "bank_id, created_at DESC"),
    @Index(name = "idx_notifications_bank_read", columnList = "bank_id, read_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "bank_id", nullable = false)
    private UUID bankId;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", nullable = false, length = 1000)
    private String message;
    
    @Column(name = "link", length = 500)
    private String link;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    public boolean isRead() {
        return readAt != null;
    }
    
    public void markAsRead() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}
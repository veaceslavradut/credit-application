package com.creditapp.shared.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compliance_checklist_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = "item_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceChecklistItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplianceStatus status;
    
    @Column(name = "gdpr_articles", nullable = false, length = 255)
    private String gdprArticles;
    
    @Column(name = "evidence", length = 1000)
    private String evidence;
    
    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
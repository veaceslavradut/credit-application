package com.creditapp.shared.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for legal documents (Privacy Policy, Terms of Service)
 * with version tracking and audit history
 */
@Entity
@Table(name = "legal_documents", indexes = {
    @Index(name = "idx_legal_documents_type_status_language", columnList = "document_type,status,language"),
    @Index(name = "idx_legal_documents_version", columnList = "document_type,version")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalDocument {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String contentHash;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String language = "en";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LegalStatus status = LegalStatus.DRAFT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (version == null) {
            version = 1;
        }
        if (status == null) {
            status = LegalStatus.DRAFT;
        }
        if (language == null) {
            language = "en";
        }
    }
}
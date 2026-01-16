package com.creditapp.borrower.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a document uploaded by a borrower to support their loan application.
 * Supports soft delete via deleted_at timestamp.
 */
@Entity
@Table(name = "application_documents", indexes = {
        @Index(name = "idx_documents_application_id", columnList = "application_id"),
        @Index(name = "idx_documents_upload_date", columnList = "upload_date"),
        @Index(name = "idx_documents_application_deleted", columnList = "application_id, deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDocument {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "document_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, unique = true)
    private String storedFilename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "uploaded_by_user_id")
    private UUID uploadedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;

    /**
     * Check if this document has been soft-deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Mark this document as deleted (soft delete).
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}

package com.creditapp.bank.model;

import com.creditapp.shared.model.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing offer-related documents.
 * Stores document metadata and S3 storage references.
 */
@Entity
@Table(name = "offer_documents", indexes = {
    @Index(name = "idx_offer_id", columnList = "offer_id"),
    @Index(name = "idx_offer_id_document_type", columnList = "offer_id,document_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"offer"})
@ToString(exclude = {"offer"})
public class OfferDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "offer_id", nullable = false)
    private UUID offerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", insertable = false, updatable = false)
    private Offer offer;
    
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // TERMS_CONDITIONS, FEE_SCHEDULE, DISCLOSURE, TRUTH_IN_LENDING, CUSTOM
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // in bytes
    
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType; // application/pdf, etc.
    
    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key; // AWS S3 object key
    
    @Column(name = "s3_url", nullable = false, length = 1000)
    private String s3Url; // Pre-signed download URL (expires in 24 hours)
    
    @Column(name = "uploaded_by_officer_id", nullable = false)
    private UUID uploadedByOfficerId;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "virus_scan_status", nullable = false, length = 20)
    private String virusScanStatus; // PENDING, CLEAN, INFECTED
    
    @Column(name = "virus_scan_result", length = 1000)
    private String virusScanResult; // Details if infected
}
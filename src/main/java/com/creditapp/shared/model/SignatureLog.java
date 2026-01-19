package com.creditapp.shared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signature_logs", indexes = {
    @Index(name = "idx_signature_logs_document_id", columnList = "document_id"),
    @Index(name = "idx_signature_logs_status", columnList = "document_id,signature_status"),
    @Index(name = "idx_signature_logs_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatureLog {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    
    @Column(name = "signer_id", nullable = false)
    private UUID signerId;
    
    @Column(name = "signed_at")
    private LocalDateTime signedAt;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "signature_certificate", columnDefinition = "TEXT")
    private String signatureCertificate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "signature_status", nullable = false)
    private SignatureStatus signatureStatus;
    
    @Column(name = "signature_id_external", length = 255)
    private String signatureIdExternal;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (signatureStatus == null) {
            signatureStatus = SignatureStatus.PENDING;
        }
    }
}
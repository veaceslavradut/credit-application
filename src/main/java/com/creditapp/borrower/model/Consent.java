package com.creditapp.borrower.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a borrower''s consent to specific terms.
 * Tracks whether consent has been signed/accepted and when.
 */
@Entity
@Table(name = "consents", indexes = {
        @Index(name = "idx_consents_application_id", columnList = "application_id"),
        @Index(name = "idx_consents_borrower_id", columnList = "borrower_id"),
        @Index(name = "idx_consents_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "borrower_id", nullable = false)
    private UUID borrowerId;

    @Column(name = "consent_type", nullable = false)
    private Integer consentNumber;

    @Column(name = "consent_text", columnDefinition = "TEXT", nullable = false)
    private String consentText;

    @Column(name = "is_signed", nullable = false)
    private Boolean isSigned;

    @Column(name = "borrower_signature", length = 2000)
    private String borrowerSignature;

    @CreationTimestamp
    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;
}

package com.creditapp.shared.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gdpr_consents", indexes = {
    @Index(name = "idx_gdpr_consents_borrower_type", columnList = "borrower_id,consent_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GDPRConsent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    private UUID borrowerId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConsentType consentType;
    
    @Column(nullable = false)
    private LocalDateTime consentedAt;
    
    @Column(nullable = true)
    private LocalDateTime withdrawnAt;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
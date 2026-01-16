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
 * EmailDeliveryLog entity for tracking email delivery status
 * Provides audit trail of all sent emails
 */
@Entity
@Table(name = "email_delivery_logs",
       indexes = {
           @Index(name = "idx_email_delivery_logs_recipient_sent", columnList = "recipient_email, sent_at"),
           @Index(name = "idx_email_delivery_logs_status_created", columnList = "status, created_at"),
           @Index(name = "idx_email_delivery_logs_template", columnList = "template_name")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDeliveryLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
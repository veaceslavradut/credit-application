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
 * EmailTemplate entity representing notification email templates
 * Stores reusable email templates with variable placeholders
 */
@Entity
@Table(name = "email_templates", 
       uniqueConstraints = @UniqueConstraint(columnNames = "template_name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "template_name", nullable = false, unique = true, length = 100)
    private String templateName;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(name = "html_body", nullable = false, columnDefinition = "TEXT")
    private String htmlBody;
    
    @Column(name = "text_body", nullable = false, columnDefinition = "TEXT")
    private String textBody;
    
    /**
     * JSON array of variable names used in the template
     * Example: ["firstName", "lastName", "email"]
     */
    @Column(columnDefinition = "TEXT")
    private String variables;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

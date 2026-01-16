package com.creditapp.shared.repository;

import com.creditapp.shared.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailTemplate entity
 * Provides query methods for email template management
 */
@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {
    
    /**
     * Find active email template by name
     * @param templateName the template name
     * @return Optional<EmailTemplate> if found and active
     */
    Optional<EmailTemplate> findByTemplateNameAndActiveTrue(String templateName);
    
    /**
     * Find all active email templates
     * @return List of active templates
     */
    List<EmailTemplate> findAllByActiveTrue();
}
package com.creditapp.shared.repository;

import com.creditapp.shared.model.DeliveryStatus;
import com.creditapp.shared.model.EmailDeliveryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for EmailDeliveryLog entity
 * Provides query methods for email delivery tracking and metrics
 */
@Repository
public interface EmailDeliveryLogRepository extends JpaRepository<EmailDeliveryLog, UUID> {
    
    /**
     * Find email delivery logs by recipient and status
     * @param email recipient email
     * @param statuses list of statuses to filter by
     * @param pageable pagination parameters
     * @return Page of EmailDeliveryLog
     */
    Page<EmailDeliveryLog> findByRecipientEmailAndStatusIn(
        String email, 
        List<DeliveryStatus> statuses, 
        Pageable pageable
    );
    
    /**
     * Count emails by status sent after a specific time
     * Used for metrics and rate limiting
     * @param status delivery status
     * @param startTime start time for filtering
     * @return count of emails
     */
    Long countByStatusAndSentAtAfter(DeliveryStatus status, LocalDateTime startTime);
}
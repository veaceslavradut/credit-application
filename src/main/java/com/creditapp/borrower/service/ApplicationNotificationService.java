package com.creditapp.borrower.service;

import com.creditapp.borrower.event.ApplicationSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service to notify banks when new applications are submitted.
 * Listens for ApplicationSubmittedEvent and sends notifications to all active banks.
 * 
 * NOTE: This is a simplified implementation for Story 2.5. 
 * Full bank notification logic will be implemented in Epic 4.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationNotificationService {

    /**
     * Handle application submitted events by notifying all active banks.
     * This method runs asynchronously to avoid blocking the submission response.
     */
    @Async
    @EventListener
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        log.info("Processing application submitted event for application: {}", event.getApplicationId());

        try {
            // TODO: In Epic 4, fetch all active banks and send real notifications
            // For now, just log the notification
            log.info("Would notify banks about new application: {} - Borrower: {}, Loan: {} {} for {} months, Loan Type: {}, Submitted: {}",
                    event.getApplicationId(),
                    event.getBorrowerId(),
                    event.getLoanAmount(),
                    event.getCurrency(),
                    event.getLoanTermMonths(),
                    event.getLoanType(),
                    event.getSubmittedAt());

            log.info("Application {} is now available in bank queue (placeholder)", event.getApplicationId());

        } catch (Exception e) {
            log.error("Error processing application submitted event for application {}: {}", 
                    event.getApplicationId(), e.getMessage(), e);
        }
    }
}
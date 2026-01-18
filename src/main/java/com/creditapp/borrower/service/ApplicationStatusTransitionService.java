package com.creditapp.borrower.service;

import com.creditapp.borrower.model.ApplicationHistory;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing application status transitions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatusTransitionService {

    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AuditService auditService;

    /**
     * Record a status transition in the application history.
     */
    public void recordStatusTransition(UUID applicationId, ApplicationStatus oldStatus,
                                      ApplicationStatus newStatus, UUID changedByUserId, String reason) {
        // Validate transition
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid transition from " + oldStatus + " to " + newStatus);
        }

        // Create history entry
        ApplicationHistory history = ApplicationHistory.builder()
                .applicationId(applicationId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedByUserId(changedByUserId)
                .changeReason(reason)
                .build();

        applicationHistoryRepository.save(history);
        
        // Log audit event
        auditService.logAction("Application", applicationId, AuditAction.APPLICATION_STATUS_CHANGED);
        
        log.info("Status transition recorded: {} -> {} for application {}", oldStatus, newStatus, applicationId);
    }

    /**
     * Check if a status transition is valid.
     */
    public boolean isValidTransition(ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        // Define valid transitions
        switch (oldStatus) {
            case DRAFT:
                return newStatus == ApplicationStatus.SUBMITTED;
            case SUBMITTED:
                return newStatus == ApplicationStatus.UNDER_REVIEW;
            case UNDER_REVIEW:
                return newStatus == ApplicationStatus.OFFERS_AVAILABLE || 
                       newStatus == ApplicationStatus.REJECTED;
            case OFFERS_AVAILABLE:
                return newStatus == ApplicationStatus.ACCEPTED || 
                       newStatus == ApplicationStatus.REJECTED || 
                       newStatus == ApplicationStatus.EXPIRED;
            case ACCEPTED:
                return newStatus == ApplicationStatus.COMPLETED ||
                       newStatus == ApplicationStatus.SUBMITTED; // Revert when selected offer expires
            case REJECTED:
            case EXPIRED:
            case WITHDRAWN:
            case COMPLETED:
                return false; // Terminal states
            default:
                return false;
        }
    }
}

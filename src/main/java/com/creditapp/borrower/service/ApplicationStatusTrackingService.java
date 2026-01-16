package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.ApplicationStatusDTO;
import com.creditapp.borrower.dto.StatusTransitionDTO;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationHistory;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for tracking and retrieving application status history.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatusTrackingService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;

    // Define the expected workflow progression for percentage calculation
    private static final List<ApplicationStatus> WORKFLOW_PROGRESSION = List.of(
            ApplicationStatus.DRAFT,
            ApplicationStatus.SUBMITTED,
            ApplicationStatus.UNDER_REVIEW,
            ApplicationStatus.OFFERS_AVAILABLE,
            ApplicationStatus.ACCEPTED,
            ApplicationStatus.COMPLETED
    );

    /**
     * Get application status and full history timeline.
     *
     * @param applicationId the application ID
     * @param borrowerId the borrower ID (for access control)
     * @return ApplicationStatusDTO with current status and history
     * @throws ApplicationNotFoundException if application not found or access denied
     */
    public ApplicationStatusDTO getApplicationStatus(UUID applicationId, UUID borrowerId) {
        // Fetch application with access control
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found: " + applicationId
                ));

        // Verify borrower owns this application
        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new ApplicationNotFoundException(
                    "Application not found: " + applicationId
            );
        }

        // Fetch status history (ordered by changedAt DESC)
        List<ApplicationHistory> histories = applicationHistoryRepository
                .findByApplicationIdOrderByChangedAtDesc(applicationId);

        // Build status transitions
        List<StatusTransitionDTO> statusHistory = new ArrayList<>();
        for (ApplicationHistory history : histories) {
            StatusTransitionDTO dto = StatusTransitionDTO.builder()
                    .oldStatus(history.getOldStatus())
                    .newStatus(history.getNewStatus())
                    .changedAt(history.getChangedAt())
                    .changedByUserId(history.getChangedByUserId())
                    .changedByName(extractUserName(history))
                    .reason(history.getChangeReason())
                    .build();
            statusHistory.add(dto);
        }

        // Calculate progression percentage based on workflow
        int progressionPercentage = calculateProgressionPercentage(application.getStatus());

        log.info("Retrieved status for application {}: {} (progression: {}%)",
                applicationId, application.getStatus(), progressionPercentage);

        return ApplicationStatusDTO.builder()
                .applicationId(applicationId)
                .currentStatus(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .createdAt(application.getCreatedAt())
                .statusHistory(statusHistory)
                .progressionPercentage(progressionPercentage)
                .build();
    }

    /**
     * Calculate progression percentage based on workflow state machine.
     * DRAFT=0%, SUBMITTED=20%, UNDER_REVIEW=40%, OFFERS_AVAILABLE=60%, ACCEPTED=80%, COMPLETED=100%
     *
     * @param status the current application status
     * @return progression percentage (0-100)
     */
    private int calculateProgressionPercentage(ApplicationStatus status) {
        int index = WORKFLOW_PROGRESSION.indexOf(status);
        if (index < 0) {
            // Status not in main workflow (e.g., REJECTED, WITHDRAWN, EXPIRED)
            return 50; // Default to 50% for terminal states
        }
        return (int) ((index + 1) / (double) WORKFLOW_PROGRESSION.size() * 100);
    }

    /**
     * Extract user name from ApplicationHistory (for display in status transitions).
     *
     * @param history the application history entry
     * @return formatted user name or "System" if no user ID
     */
    private String extractUserName(ApplicationHistory history) {
        if (history.getChangedByUserId() == null) {
            return "System";
        }
        // In future, could fetch user details from database
        return "User " + history.getChangedByUserId().toString().substring(0, 8);
    }
}

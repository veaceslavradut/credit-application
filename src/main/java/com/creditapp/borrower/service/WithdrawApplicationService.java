package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.WithdrawApplicationRequest;
import com.creditapp.borrower.dto.WithdrawApplicationResponse;
import com.creditapp.borrower.exception.ApplicationNotWithdrawableException;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for handling application withdrawal operations.
 */
@Service
@RequiredArgsConstructor
public class WithdrawApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusTransitionService statusTransitionService;
    private final AuditService auditService;

    /**
     * Withdraw an application.
     *
     * @param applicationId The ID of the application to withdraw
     * @param borrowerId The ID of the borrower (for access control)
     * @param request The withdrawal request with optional reason
     * @return The withdrawn application response
     * @throws ApplicationNotFoundException if application not found
     * @throws ApplicationNotWithdrawableException if application is in terminal state
     */
    @Transactional
    public WithdrawApplicationResponse withdrawApplication(
            UUID applicationId,
            UUID borrowerId,
            WithdrawApplicationRequest request) {

        // Fetch application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + applicationId));

        // Verify borrower owns the application
        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new ApplicationNotFoundException("Application not found with ID: " + applicationId);
        }

        // Check if application is in withdrawable state
        ApplicationStatus currentStatus = application.getStatus();
        if (!isWithdrawable(currentStatus)) {
            throw new ApplicationNotWithdrawableException(
                    "Cannot withdraw application in status: " + currentStatus,
                    currentStatus.toString());
        }

        // Record status transition
        String withdrawalReason = request.getWithdrawalReason() != null 
                ? request.getWithdrawalReason() 
                : "User withdrew application";
        statusTransitionService.recordStatusTransition(
                applicationId,
                currentStatus,
                ApplicationStatus.WITHDRAWN,
                borrowerId,
                withdrawalReason);

        // Update application
        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawnAt(LocalDateTime.now());
        application.setWithdrawalReason(request.getWithdrawalReason());
        application.setUpdatedAt(LocalDateTime.now());

        applicationRepository.save(application);

        // Log audit event
        auditService.logAction("Application", applicationId, AuditAction.APPLICATION_WITHDRAWN, borrowerId, "BORROWER");

        // Return response
        return WithdrawApplicationResponse.builder()
                .id(application.getId())
                .status(ApplicationStatus.WITHDRAWN.toString())
                .withdrawnAt(application.getWithdrawnAt())
                .withdrawalReason(application.getWithdrawalReason())
                .message("Your application has been withdrawn successfully.")
                .build();
    }

    /**
     * Check if application can be withdrawn from its current status.
     *
     * @param status The current application status
     * @return true if withdrawable, false otherwise
     */
    private boolean isWithdrawable(ApplicationStatus status) {
        // Withdrawable states: DRAFT, SUBMITTED, UNDER_REVIEW, OFFERS_AVAILABLE
        // Terminal states: ACCEPTED, REJECTED, EXPIRED, COMPLETED, WITHDRAWN
        return status == ApplicationStatus.DRAFT
                || status == ApplicationStatus.SUBMITTED
                || status == ApplicationStatus.UNDER_REVIEW
                || status == ApplicationStatus.OFFERS_AVAILABLE;
    }
}

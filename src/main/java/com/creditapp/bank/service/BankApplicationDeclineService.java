package com.creditapp.bank.service;

import com.creditapp.bank.dto.DeclineApplicationRequest;
import com.creditapp.bank.dto.DeclineApplicationResponse;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BankApplicationDeclineService {

    private final ApplicationRepository applicationRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public DeclineApplicationResponse declineApplication(UUID bankId, UUID applicationId, DeclineApplicationRequest request) {
        log.info("Declining application {} for bank {}", applicationId, bankId);

        // Fetch Application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));

        // Verify application status < OFFERS_AVAILABLE (OFFER_SUBMITTED equivalent in enum)
        if (application.getStatus().ordinal() >= ApplicationStatus.OFFERS_AVAILABLE.ordinal()) {
            throw new IllegalStateException(
                    "Cannot decline application in status: " + application.getStatus() +
                    ". Application must be in DRAFT, SUBMITTED, or UNDER_REVIEW status.");
        }

        // Validate reason length
        if (request.getReason() != null && request.getReason().length() > 500) {
            throw new IllegalArgumentException("Decline reason must not exceed 500 characters");
        }

        // Update application status
        application.setStatus(ApplicationStatus.REJECTED);
        Application updatedApplication = applicationRepository.save(application);

        // Create audit log
        auditService.logAction(
                "APPLICATION",
                applicationId,
                com.creditapp.shared.model.AuditAction.APPLICATION_DECLINED
        );

        // Queue email notification
        try {
            String reason = request.getReason() != null ? request.getReason() : "Application declined";
            notificationService.createNotification(
                    application.getBorrowerId(),
                    applicationId,
                    com.creditapp.shared.model.NotificationType.APPLICATION_REJECTED,
                    "Application Status Update",
                    "Your application has been declined. Reason: " + reason
            );
        } catch (Exception e) {
            log.error("Failed to send decline notification", e);
        }

        LocalDateTime declinedAt = LocalDateTime.now();
        log.info("Application {} declined successfully at {}", applicationId, declinedAt);

        return DeclineApplicationResponse.builder()
                .applicationId(applicationId)
                .status(updatedApplication.getStatus())
                .declinedAt(declinedAt)
                .reason(request.getReason())
                .build();
    }
}
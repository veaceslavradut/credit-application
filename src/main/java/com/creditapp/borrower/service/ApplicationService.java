package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.dto.ApplicationHistoryDTO;
import com.creditapp.borrower.dto.CreateApplicationRequest;
import com.creditapp.borrower.dto.SubmitApplicationResponse;
import com.creditapp.borrower.dto.UpdateApplicationRequest;
import com.creditapp.borrower.dto.UpdateApplicationResponse;
import com.creditapp.borrower.exception.ApplicationAlreadySubmittedException;
import com.creditapp.borrower.exception.ApplicationCreationException;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.exception.ApplicationNotEditableException;
import com.creditapp.borrower.exception.ApplicationStaleException;
import com.creditapp.borrower.exception.InvalidApplicationException;
import com.creditapp.borrower.exception.SubmissionValidationException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationHistory;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.audit.BusinessAudit;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.NotificationService;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.bank.service.OfferCalculationService;
import com.creditapp.shared.service.GDPRConsentService;
import com.creditapp.shared.model.ConsentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Service for application operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final OfferCalculationService offerCalculationService;
    private final GDPRConsentService consentService;

    /**
     * Create a new application in DRAFT status.
     */
    @BusinessAudit(action = AuditAction.APPLICATION_CREATED, entityType = "Application")
    public ApplicationDTO createApplication(UUID borrowerId, CreateApplicationRequest request) {
        // Validate loan amount (100 - 1,000,000)
        if (request.getLoanAmount() == null) {
            throw new InvalidApplicationException("Loan amount is required");
        }
        if (request.getLoanAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new InvalidApplicationException("Loan amount must be at least 100");
        }
        if (request.getLoanAmount().compareTo(BigDecimal.valueOf(1000000)) > 0) {
            throw new InvalidApplicationException("Loan amount cannot exceed 1,000,000");
        }

        // Validate loan term (6 - 360 months)
        if (request.getLoanTermMonths() == null) {
            throw new InvalidApplicationException("Loan term is required");
        }
        if (request.getLoanTermMonths() < 6) {
            throw new InvalidApplicationException("Loan term must be at least 6 months");
        }
        if (request.getLoanTermMonths() > 360) {
            throw new InvalidApplicationException("Loan term cannot exceed 360 months");
        }

        try {
            // Create application
            Application application = Application.builder()
                    .borrowerId(borrowerId)
                    .loanType(request.getLoanType())
                    .loanAmount(request.getLoanAmount())
                    .loanTermMonths(request.getLoanTermMonths())
                    .currency(request.getCurrency())
                    .ratePreference(request.getRatePreference() != null ? request.getRatePreference() : "VARIABLE")
                    .status(ApplicationStatus.DRAFT)
                    .build();

            application = applicationRepository.save(application);
            // Flush and reload to ensure @CreationTimestamp is populated by database
            applicationRepository.flush();
            // Use Java Optional API to handle the reload safely
            UUID appId = application.getId();
            application = applicationRepository.findById(appId)
                    .orElseThrow(() -> new ApplicationCreationException("Failed to retrieve saved application"));

            // Audit event logged via @BusinessAudit annotation
            log.info("Application created: {} for borrower: {}", application.getId(), borrowerId);
            return mapToDTO(application);
            
        } catch (Exception e) {
            log.error("Failed to create application for borrower: {}", borrowerId, e);
            throw new ApplicationCreationException("Failed to create application", e);
        }
    }

    /**
     * Get an application by ID for a specific borrower.
     */
    public ApplicationDTO getApplication(UUID applicationId, UUID borrowerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found: " + applicationId));

        // Verify borrower owns the application
        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new ApplicationNotFoundException(
                    "Access denied to application: " + applicationId);
        }

        return mapToDTO(application);
    }

    /**
     * Get application status history.
     */
    public List<ApplicationHistoryDTO> getApplicationHistory(UUID applicationId, UUID borrowerId) {
        // Verify borrower owns the application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new ApplicationNotFoundException(
                    "Access denied to application: " + applicationId);
        }

        return applicationHistoryRepository
                .findByApplicationIdOrderByChangedAtDesc(applicationId)
                .stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get application status history with pagination.
     */
    public Page<ApplicationHistoryDTO> getApplicationHistory(UUID applicationId, UUID borrowerId, Pageable pageable) {
        // Verify borrower owns the application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new ApplicationNotFoundException(
                    "Access denied to application: " + applicationId);
        }

        Page<ApplicationHistory> historyPage = applicationHistoryRepository
                .findByApplicationIdOrderByChangedAtDesc(applicationId, pageable);
        
        return historyPage.map(this::mapHistoryToDTO);
    }

    /**
     * List applications for a borrower with pagination.
     */
    public Page<ApplicationDTO> listApplicationsByBorrower(UUID borrowerId, Pageable pageable) {
        return applicationRepository.findByBorrowerId(borrowerId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Submit an application for underwriting review (transition DRAFT -> SUBMITTED).
     */
    @BusinessAudit(action = AuditAction.APPLICATION_SUBMITTED, entityType = "Application")
    public SubmitApplicationResponse submitApplication(UUID applicationId, UUID borrowerId) {
        // Verify borrower owns the application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new ApplicationNotFoundException(
                    "Access denied to application: " + applicationId);
        }

        // Verify application is in DRAFT status
        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApplicationAlreadySubmittedException(
                    "Application is already in " + application.getStatus() + " status. Only DRAFT applications can be submitted.");
        }

        // Validate required consents before submission (GDPR compliance)
        validateRequiredConsents(borrowerId);

        // Validate required fields
        List<String> missingFields = new ArrayList<>();
        if (application.getLoanType() == null || application.getLoanType().isEmpty()) {
            missingFields.add("loanType");
        }
        if (application.getLoanAmount() == null) {
            missingFields.add("loanAmount");
        }
        if (application.getLoanTermMonths() == null) {
            missingFields.add("loanTermMonths");
        }
        if (application.getCurrency() == null || application.getCurrency().isEmpty()) {
            missingFields.add("currency");
        }

        if (!missingFields.isEmpty()) {
            throw new SubmissionValidationException(
                    "Required fields missing: " + String.join(", ", missingFields),
                    missingFields);
        }

        // Update status to SUBMITTED
        application.setStatus(ApplicationStatus.SUBMITTED);
        
        try {
            application = applicationRepository.save(application);

            log.info("Application submitted: {} by borrower: {}", applicationId, borrowerId);

            // Queue email notification for borrower (APPLICATION_SUBMITTED)
            try {
                final Application appFinal = application;
                java.util.Optional<com.creditapp.shared.model.User> borrowerOpt = userRepository.findById(borrowerId);
                borrowerOpt.ifPresent(borrower -> {
                    String subject = "Application Submitted";
                    String message = String.format(
                        "Dear %s, your loan application for %s %s has been submitted successfully. Application ID: %s",
                        borrower.getFirstName() != null ? borrower.getFirstName() : "",
                        appFinal.getLoanAmount() != null ? appFinal.getLoanAmount().toPlainString() : "",
                        appFinal.getCurrency() != null ? appFinal.getCurrency() : "",
                        appFinal.getId() != null ? appFinal.getId().toString() : ""
                    );
                    notificationService.createNotification(
                        borrowerId,
                        appFinal.getId(),
                        com.creditapp.shared.model.NotificationType.APPLICATION_SUBMITTED,
                        subject,
                        message
                    );
                });
            } catch (Exception notifyEx) {
                log.warn("Failed to create APPLICATION_SUBMITTED notification for application {}: {}",
                        application.getId(), notifyEx.getMessage());
            }

            // Trigger async offer calculation for all active banks
            try {
                log.info("Triggering async offer calculation for application: {}", application.getId());
                offerCalculationService.calculateOffers(application.getId());
            } catch (Exception calcEx) {
                log.error("Failed to trigger offer calculation for application {}: {}",
                        application.getId(), calcEx.getMessage(), calcEx);
                // Don't fail submission if calculation trigger fails - calculation can be retried
            }

            return SubmitApplicationResponse.builder()
                    .id(application.getId())
                    .status(application.getStatus().toString())
                    .submittedAt(application.getSubmittedAt())
                    .message("Application submitted successfully. Banks will review and contact you with offers.")
                    .application(mapToDTO(application))
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to submit application: {}", applicationId, e);
            throw new ApplicationCreationException("Failed to submit application", e);
        }
    }

    /**
     * Update an existing DRAFT application.
     */
    @BusinessAudit(action = AuditAction.APPLICATION_UPDATED, entityType = "Application")
    public UpdateApplicationResponse updateApplication(UUID applicationId, UUID borrowerId, UpdateApplicationRequest request) {
        // Verify borrower owns the application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new ApplicationNotFoundException(
                    "Access denied to application: " + applicationId);
        }

        // Verify application is in DRAFT status
        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApplicationNotEditableException(
                    "Cannot edit application in " + application.getStatus() + " status. Only DRAFT applications can be edited.");
        }

        // Track old values for audit
        List<String> editedFields = new ArrayList<>();
        
        // Validate and update loan amount if provided
        if (request.getLoanAmount() != null) {
            if (request.getLoanAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
                throw new InvalidApplicationException("Loan amount must be at least 100");
            }
            if (request.getLoanAmount().compareTo(BigDecimal.valueOf(1000000)) > 0) {
                throw new InvalidApplicationException("Loan amount cannot exceed 1,000,000");
            }
            if (!request.getLoanAmount().equals(application.getLoanAmount())) {
                application.setLoanAmount(request.getLoanAmount());
                editedFields.add("loanAmount");
            }
        }

        // Validate and update loan term if provided
        if (request.getLoanTermMonths() != null) {
            if (request.getLoanTermMonths() < 6) {
                throw new InvalidApplicationException("Loan term must be at least 6 months");
            }
            if (request.getLoanTermMonths() > 360) {
                throw new InvalidApplicationException("Loan term cannot exceed 360 months");
            }
            if (!request.getLoanTermMonths().equals(application.getLoanTermMonths())) {
                application.setLoanTermMonths(request.getLoanTermMonths());
                editedFields.add("loanTermMonths");
            }
        }

        // Update other fields if provided
        if (request.getLoanType() != null && !request.getLoanType().equals(application.getLoanType())) {
            application.setLoanType(request.getLoanType());
            editedFields.add("loanType");
        }

        if (request.getCurrency() != null && !request.getCurrency().equals(application.getCurrency())) {
            application.setCurrency(request.getCurrency());
            editedFields.add("currency");
        }

        if (request.getRatePreference() != null && !request.getRatePreference().equals(application.getRatePreference())) {
            application.setRatePreference(request.getRatePreference());
            editedFields.add("ratePreference");
        }

        // Update application details if provided
        if (request.getApplicationDetails() != null) {
            if (application.getDetails() == null) {
                application.setDetails(com.creditapp.borrower.model.ApplicationDetails.builder()
                        .applicationId(application.getId())
                        .build());
            }
            
            if (request.getApplicationDetails().getAnnualIncome() != null) {
                application.getDetails().setAnnualIncome(request.getApplicationDetails().getAnnualIncome());
                editedFields.add("annualIncome");
            }
            if (request.getApplicationDetails().getEmploymentStatus() != null) {
                application.getDetails().setEmploymentStatus(request.getApplicationDetails().getEmploymentStatus());
                editedFields.add("employmentStatus");
            }
            if (request.getApplicationDetails().getDownPaymentAmount() != null) {
                application.getDetails().setDownPaymentAmount(request.getApplicationDetails().getDownPaymentAmount());
                editedFields.add("downPaymentAmount");
            }
        }

        try {
            // Save application (JPA will increment version)
            application = applicationRepository.save(application);
            
            log.info("Application updated: {} by borrower: {}, fields changed: {}", 
                    applicationId, borrowerId, editedFields);

            return UpdateApplicationResponse.builder()
                    .id(application.getId())
                    .loanType(application.getLoanType())
                    .loanAmount(application.getLoanAmount())
                    .loanTermMonths(application.getLoanTermMonths())
                    .currency(application.getCurrency())
                    .ratePreference(application.getRatePreference())
                    .status(application.getStatus().toString())
                    .version(application.getVersion())
                    .updatedAt(application.getUpdatedAt())
                    .editedFields(editedFields)
                    .build();
                    
        } catch (ObjectOptimisticLockingFailureException e) {
            // Application was modified by another request
            Application current = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));
            throw new ApplicationStaleException(
                    "Application was modified by another request. Please refresh and try again.", 
                    current.getVersion());
        } catch (Exception e) {
            log.error("Failed to update application: {}", applicationId, e);
            throw new ApplicationCreationException("Failed to update application", e);
        }
    }

    private ApplicationDTO mapToDTO(Application application) {
        return ApplicationDTO.builder()
                .id(application.getId())
                .loanType(application.getLoanType())
                .loanAmount(application.getLoanAmount())
                .loanTermMonths(application.getLoanTermMonths())
                .currency(application.getCurrency())
                .ratePreference(application.getRatePreference())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .submittedAt(application.getSubmittedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private ApplicationHistoryDTO mapHistoryToDTO(com.creditapp.borrower.model.ApplicationHistory history) {
        return ApplicationHistoryDTO.builder()
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedAt(history.getChangedAt())
                .changedByUserId(history.getChangedByUserId())
                .changeReason(history.getChangeReason())
                .build();
    }

    /**
     * Validate that required GDPR consents are given before application submission.
     * Requires: DATA_COLLECTION and BANK_SHARING consents.
     */
    private void validateRequiredConsents(UUID borrowerId) {
        if (!consentService.isConsentGiven(borrowerId, ConsentType.DATA_COLLECTION)) {
            throw new SubmissionValidationException(
                    "Required consent missing: You must consent to data collection to submit an application.",
                    List.of("DATA_COLLECTION_CONSENT"));
        }
        
        if (!consentService.isConsentGiven(borrowerId, ConsentType.BANK_SHARING)) {
            throw new SubmissionValidationException(
                    "Required consent missing: You must consent to bank sharing to submit an application.",
                    List.of("BANK_SHARING_CONSENT"));
        }
        
        log.info("Consent validation passed for borrower: {}", borrowerId);
    }
}

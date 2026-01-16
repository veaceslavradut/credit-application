package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.dto.ApplicationHistoryDTO;
import com.creditapp.borrower.dto.CreateApplicationRequest;
import com.creditapp.borrower.exception.ApplicationCreationException;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.exception.InvalidApplicationException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.audit.BusinessAudit;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * List applications for a borrower with pagination.
     */
    public Page<ApplicationDTO> listApplicationsByBorrower(UUID borrowerId, Pageable pageable) {
        return applicationRepository.findByBorrowerId(borrowerId, pageable)
                .map(this::mapToDTO);
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
}

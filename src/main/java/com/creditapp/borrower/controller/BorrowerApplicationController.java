package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.dto.ApplicationHistoryDTO;
import com.creditapp.borrower.dto.ApplicationStatusDTO;
import com.creditapp.borrower.dto.CreateApplicationRequest;
import com.creditapp.borrower.dto.DocumentDTO;
import com.creditapp.borrower.dto.SubmitApplicationRequest;
import com.creditapp.borrower.dto.SubmitApplicationResponse;
import com.creditapp.borrower.dto.UpdateApplicationRequest;
import com.creditapp.borrower.dto.UpdateApplicationResponse;
import com.creditapp.borrower.dto.WithdrawApplicationRequest;
import com.creditapp.borrower.dto.WithdrawApplicationResponse;
import com.creditapp.borrower.model.DocumentType;
import com.creditapp.borrower.service.ApplicationDocumentService;
import com.creditapp.borrower.service.ApplicationService;
import com.creditapp.borrower.service.ApplicationStatusTrackingService;
import com.creditapp.borrower.service.WithdrawApplicationService;
import com.creditapp.shared.security.AuthorizationService;
import com.creditapp.shared.security.RateLimited;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for borrower application operations.
 */
@RestController
@RequestMapping("/api/borrower/applications")
@RequiredArgsConstructor
@Slf4j
public class BorrowerApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationDocumentService documentService;
    private final ApplicationStatusTrackingService statusTrackingService;
    private final WithdrawApplicationService withdrawApplicationService;
    private final AuthorizationService authorizationService;

    /**
     * Create a new loan application in DRAFT status.
     * Rate limited to 1 application per borrower per minute.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    @RateLimited(action = "CREATE_APPLICATION", limitPerMinute = 1)
    public ResponseEntity<ApplicationDTO> createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Creating application for borrower: {}, loanType: {}, loanAmount: {}", 
                borrowerId, request.getLoanType(), request.getLoanAmount());
        
        ApplicationDTO application = applicationService.createApplication(borrowerId, request);
        
        log.info("Application created successfully: {} for borrower: {}", 
                application.getId(), borrowerId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    /**
     * List all applications for the authenticated borrower.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<Page<ApplicationDTO>> listApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<ApplicationDTO> applications = applicationService.listApplicationsByBorrower(borrowerId, pageable);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get a specific application by ID.
     */
    @GetMapping("/{applicationId}")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApplicationDTO> getApplication(@PathVariable UUID applicationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        ApplicationDTO application = applicationService.getApplication(applicationId, borrowerId);
        return ResponseEntity.ok(application);
    }

    /**
     * Get the status change history of an application.
     */
    @GetMapping("/{applicationId}/history")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<List<ApplicationHistoryDTO>> getApplicationHistory(@PathVariable UUID applicationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        List<ApplicationHistoryDTO> history = applicationService.getApplicationHistory(applicationId, borrowerId);
        return ResponseEntity.ok(history);
    }

    /**
     * Update a DRAFT application. Only applications in DRAFT status can be edited.
     * Uses optimistic locking to prevent concurrent modifications.
     */
    @PutMapping("/{applicationId}")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<UpdateApplicationResponse> updateApplication(
            @PathVariable UUID applicationId,
            @Valid @RequestBody UpdateApplicationRequest request) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Updating application: {} for borrower: {}", applicationId, borrowerId);
        
        UpdateApplicationResponse response = applicationService.updateApplication(applicationId, borrowerId, request);
        
        log.info("Application updated successfully: {} by borrower: {}, fields: {}", 
                applicationId, borrowerId, response.getEditedFields());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Submit an application for underwriting review (transition DRAFT -> SUBMITTED).
     * No rate limiting for submissions (unlike creation).
     */
    @PostMapping("/{applicationId}/submit")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<SubmitApplicationResponse> submitApplication(
            @PathVariable UUID applicationId,
            @RequestBody(required = false) SubmitApplicationRequest request) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Submitting application: {} for borrower: {}", applicationId, borrowerId);
        
        SubmitApplicationResponse response = applicationService.submitApplication(applicationId, borrowerId);
        
        log.info("Application submitted successfully: {} by borrower: {}", applicationId, borrowerId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Upload a document to an application.
     * Accepts multipart/form-data with file and documentType.
     */
    @PostMapping(value = "/{applicationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @PathVariable UUID applicationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Uploading document to application: {} by borrower: {}, type: {}", 
                applicationId, borrowerId, documentType);
        
        DocumentDTO document = documentService.uploadDocument(applicationId, borrowerId, file, documentType);
        
        log.info("Document uploaded successfully: {} for application: {}", document.getId(), applicationId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    /**
     * List all active documents for an application.
     */
    @GetMapping("/{applicationId}/documents")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<List<DocumentDTO>> listDocuments(@PathVariable UUID applicationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Listing documents for application: {} by borrower: {}", applicationId, borrowerId);
        
        List<DocumentDTO> documents = documentService.listDocuments(applicationId, borrowerId);
        
        log.info("Found {} documents for application: {}", documents.size(), applicationId);
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Delete a document (soft delete).
     */
    @DeleteMapping("/{applicationId}/documents/{documentId}")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID applicationId,
            @PathVariable UUID documentId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Deleting document: {} from application: {} by borrower: {}", 
                documentId, applicationId, borrowerId);
        
        documentService.deleteDocument(applicationId, borrowerId, documentId);
        
        log.info("Document deleted successfully: {} from application: {}", documentId, applicationId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get application status and timeline history.
     */
    @GetMapping("/{applicationId}/status")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApplicationStatusDTO> getApplicationStatus(
            @PathVariable UUID applicationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Retrieving status for application: {} by borrower: {}", applicationId, borrowerId);
        
        ApplicationStatusDTO status = statusTrackingService.getApplicationStatus(applicationId, borrowerId);
        
        log.info("Status retrieved successfully for application: {} - current status: {}", 
                applicationId, status.getCurrentStatus());
        
        return ResponseEntity.ok(status);
    }

    /**
     * Withdraw an application.
     * Can only withdraw applications in DRAFT, SUBMITTED, UNDER_REVIEW, or OFFERS_AVAILABLE states.
     */
    @PostMapping("/{applicationId}/withdraw")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<WithdrawApplicationResponse> withdrawApplication(
            @PathVariable UUID applicationId,
            @Valid @RequestBody WithdrawApplicationRequest request) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        log.info("Withdrawing application: {} by borrower: {}", applicationId, borrowerId);
        
        WithdrawApplicationResponse response = withdrawApplicationService.withdrawApplication(
                applicationId, borrowerId, request);
        
        log.info("Application withdrawn successfully: {}", applicationId);
        
        return ResponseEntity.ok(response);
    }
}

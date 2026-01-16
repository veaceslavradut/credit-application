package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.dto.ApplicationHistoryDTO;
import com.creditapp.borrower.dto.CreateApplicationRequest;
import com.creditapp.borrower.service.ApplicationService;
import com.creditapp.shared.security.AuthorizationService;
import com.creditapp.shared.security.RateLimited;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}

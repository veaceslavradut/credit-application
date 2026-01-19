package com.creditapp.bank.controller;

import com.creditapp.bank.dto.ApplicationDetailsResponse;
import com.creditapp.bank.service.BankApplicationDetailsService;
import com.creditapp.shared.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for Story 4.3: Application Review Panel
 * Provides bank admins with complete application details for review before making offer
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank/applications")
@Slf4j
public class BankApplicationReviewController {

    private final BankApplicationDetailsService applicationDetailsService;
    private final AuthorizationService authorizationService;

    /**
     * Get full application details for review panel
     * Includes borrower info, loan details, employment, consents, and current offer
     * 
     * @param applicationId the application ID to retrieve
     * @return complete application details with borrower and loan information
     */
    @GetMapping("/{applicationId}/details")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<ApplicationDetailsResponse> getApplicationDetails(
            @PathVariable UUID applicationId
    ) {
        // Extract bankId from authenticated user
        UUID bankId = authorizationService.getBankIdFromContext();

        log.info("Bank {} retrieving application details for app {}", bankId, applicationId);

        // Fetch full application details
        ApplicationDetailsResponse details = applicationDetailsService.getApplicationDetails(bankId, applicationId);

        log.info("Successfully retrieved application details for app {} from bank {}", applicationId, bankId);

        return ResponseEntity.ok(details);
    }

    /**
     * Update internal notes for an application (bank staff only)
     * 
     * @param applicationId the application ID to update notes for
     * @param payload request body with notes content
     * @return success response with updated details
     */
    @PutMapping("/{applicationId}/notes")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateInternalNotes(
            @PathVariable UUID applicationId,
            @RequestBody Map<String, String> payload
    ) {
        // Extract bankId and userId from authenticated user
        UUID bankId = authorizationService.getBankIdFromContext();
        UUID userId = authorizationService.getCurrentUserId();

        String notes = payload.getOrDefault("notes", "");

        log.info("Bank {} updating internal notes for application {} by user {}", bankId, applicationId, userId);

        // Update notes
        applicationDetailsService.updateInternalNotes(bankId, applicationId, notes, userId);

        // Return success response
        Map<String, Object> response = new HashMap<>();
        response.put("applicationId", applicationId);
        response.put("notes", notes);
        response.put("message", "Internal notes updated successfully");

        return ResponseEntity.ok(response);
    }
}

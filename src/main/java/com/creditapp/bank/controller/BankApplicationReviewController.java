package com.creditapp.bank.controller;

import com.creditapp.bank.dto.ApplicationDetailsResponse;
import com.creditapp.bank.service.BankApplicationDetailsService;
import com.creditapp.shared.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

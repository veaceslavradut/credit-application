package com.creditapp.bank.controller;

import com.creditapp.bank.dto.OfferSubmissionRequest;
import com.creditapp.bank.dto.OfferSubmissionResponse;
import com.creditapp.bank.service.BankOfferSubmissionService;
import com.creditapp.shared.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller for Story 4.4: Offer Submission Form
 * Handles bank submission of preliminary offers with optional field overrides
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank/offers")
@Slf4j
public class BankOfferSubmissionController {

    private final BankOfferSubmissionService offerSubmissionService;
    private final AuthorizationService authorizationService;

    /**
     * Submit preliminary offer for an application
     * Supports accepting system-calculated offer AS-IS or with field overrides
     * Implements idempotency: same (bankId, applicationId) returns existing offer
     *
     * @param request submission request with optional overrides
     * @return OfferSubmissionResponse with 201 CREATED (new) or 200 OK (idempotent)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<OfferSubmissionResponse> submitOffer(
            @RequestBody OfferSubmissionRequest request
    ) {
        // Extract bankId from authenticated user
        UUID bankId = authorizationService.getBankIdFromContext();

        log.info("Bank {} submitting offer for application {}", bankId, request.getApplicationId());

        try {
            // Submit offer
            OfferSubmissionResponse response = offerSubmissionService.submitOffer(
                    bankId,
                    request.getApplicationId(),
                    request
            );

            // Return appropriate HTTP status
            HttpStatus status = response.getHttpStatus() == 201 ? HttpStatus.CREATED : HttpStatus.OK;
            log.info("Offer submitted successfully for app {}: {}", request.getApplicationId(), status);

            return ResponseEntity.status(status).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in offer submission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error submitting offer for app {}: {}", request.getApplicationId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package com.creditapp.bank.controller;

import com.creditapp.bank.dto.ResubmitOfferFormResponse;
import com.creditapp.bank.dto.ResubmitOfferRequest;
import com.creditapp.bank.dto.ResubmitOfferResponse;
import com.creditapp.bank.service.BankOfferResubmissionService;
import com.creditapp.shared.security.AuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for offer resubmission endpoints
 * Story 4.6: Offer Expiration Notification - Tasks 5 & 6
 */
@RestController
@RequestMapping("/api/bank/offers")
@RequiredArgsConstructor
@Slf4j
public class BankOfferResubmissionController {

    private final BankOfferResubmissionService resubmissionService;
    private final AuthorizationService authorizationService;

    /**
     * Get resubmit form with previous offer details
     * GET /api/bank/offers/{offerId}/resubmit
     */
    @GetMapping("/{offerId}/resubmit")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<ResubmitOfferFormResponse> getResubmitForm(@PathVariable UUID offerId) {
        log.info("GET /api/bank/offers/{}/resubmit", offerId);

        try {
            UUID bankId = authorizationService.getBankIdFromContext();
            
            ResubmitOfferFormResponse response = resubmissionService.getResubmitForm(offerId, bankId);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Offer not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Unauthorized resubmit access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error getting resubmit form for offer {}: {}", offerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resubmit offer with updated values
     * POST /api/bank/offers/{offerId}/resubmit
     */
    @PostMapping("/{offerId}/resubmit")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<ResubmitOfferResponse> resubmitOffer(
            @PathVariable UUID offerId,
            @Valid @RequestBody ResubmitOfferRequest request) {
        
        log.info("POST /api/bank/offers/{}/resubmit with APR {}", offerId, request.getApr());

        try {
            UUID bankId = authorizationService.getBankIdFromContext();
            
            ResubmitOfferResponse response = resubmissionService.resubmitOffer(offerId, bankId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error in resubmit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (SecurityException e) {
            log.warn("Unauthorized resubmit attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error resubmitting offer {}: {}", offerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
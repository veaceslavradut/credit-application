package com.creditapp.borrower.controller;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.creditapp.bank.service.OfferCalculationService;
import com.creditapp.borrower.dto.OfferComparisonResponse;
import com.creditapp.borrower.dto.SelectOfferRequest;
import com.creditapp.borrower.dto.SelectOfferResponse;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.OfferRetrievalService;
import com.creditapp.borrower.service.OfferSelectionService;
import com.creditapp.shared.security.AuthorizationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/borrower")
public class BorrowerOfferController {

    private final OfferRetrievalService offerRetrievalService;
    private final OfferSelectionService offerSelectionService;
    private final OfferCalculationService offerCalculationService;
    private final ApplicationRepository applicationRepository;
    private final AuthorizationService authorizationService;

    public BorrowerOfferController(OfferRetrievalService offerRetrievalService,
                                   OfferSelectionService offerSelectionService,
                                   OfferCalculationService offerCalculationService,
                                   ApplicationRepository applicationRepository,
                                   AuthorizationService authorizationService) {
        this.offerRetrievalService = offerRetrievalService;
        this.offerSelectionService = offerSelectionService;
        this.offerCalculationService = offerCalculationService;
        this.applicationRepository = applicationRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/applications/{applicationId}/offers")
    @PreAuthorize("hasAuthority(\"BORROWER\")")
    public OfferComparisonResponse getApplicationOffers(@PathVariable UUID applicationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        return new OfferComparisonResponse(
            offerRetrievalService.getOffersForApplication(applicationId, borrowerId),
            applicationId
        );
    }

    @PostMapping("/applications/{applicationId}/select-offer")
    @PreAuthorize("hasAuthority(\"BORROWER\")")
    public ResponseEntity<SelectOfferResponse> selectOffer(
            @PathVariable UUID applicationId,
            @Valid @RequestBody SelectOfferRequest request) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        SelectOfferResponse response = offerSelectionService.selectOffer(
            applicationId,
            borrowerId,
            request.getOfferId()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/applications/{applicationId}/recalculate-offers")
    @PreAuthorize("hasAuthority(\"BORROWER\")")
    public ResponseEntity<String> recalculateOffers(@PathVariable UUID applicationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        
        // Verify borrower owns this application
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        
        if (!application.getBorrowerId().equals(borrowerId)) {
            log.warn("Unauthorized recalculation attempt. ApplicationId: {}, BorrowerId: {}", 
                    applicationId, borrowerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access to application");
        }
        
        // Verify application status is SUBMITTED (can only recalculate when not in ACCEPTED state)
        if (application.getStatus() != ApplicationStatus.SUBMITTED &&
            application.getStatus() != ApplicationStatus.OFFERS_AVAILABLE &&
            application.getStatus() != ApplicationStatus.ACCEPTED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot recalculate offers in current application status: " + application.getStatus());
        }
        
        log.info("Recalculating offers for application: {}, BorrowerId: {}", applicationId, borrowerId);
        
        // Trigger offer recalculation (async)
        offerCalculationService.recalculateOffers(applicationId);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Offer recalculation started. You will receive new offers shortly.");
    }
}
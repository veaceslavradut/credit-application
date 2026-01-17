package com.creditapp.borrower.controller;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.creditapp.borrower.dto.OfferComparisonResponse;
import com.creditapp.borrower.dto.SelectOfferRequest;
import com.creditapp.borrower.dto.SelectOfferResponse;
import com.creditapp.borrower.service.OfferRetrievalService;
import com.creditapp.borrower.service.OfferSelectionService;
import com.creditapp.shared.security.AuthorizationService;

@RestController
@RequestMapping("/api/borrower")
public class BorrowerOfferController {

    private final OfferRetrievalService offerRetrievalService;
    private final OfferSelectionService offerSelectionService;
    private final AuthorizationService authorizationService;

    public BorrowerOfferController(OfferRetrievalService offerRetrievalService,
                                   OfferSelectionService offerSelectionService,
                                   AuthorizationService authorizationService) {
        this.offerRetrievalService = offerRetrievalService;
        this.offerSelectionService = offerSelectionService;
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
}
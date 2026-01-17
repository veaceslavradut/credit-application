package com.creditapp.borrower.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creditapp.borrower.dto.OfferComparisonResponse;
import com.creditapp.borrower.service.OfferRetrievalService;
import com.creditapp.shared.security.AuthorizationService;

@RestController
@RequestMapping("/api/borrower")
public class BorrowerOfferController {

    private final OfferRetrievalService offerRetrievalService;
    private final AuthorizationService authorizationService;

    public BorrowerOfferController(OfferRetrievalService offerRetrievalService,
                                   AuthorizationService authorizationService) {
        this.offerRetrievalService = offerRetrievalService;
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
}
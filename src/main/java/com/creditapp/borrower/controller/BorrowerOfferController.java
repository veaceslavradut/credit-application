package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.*;
import com.creditapp.borrower.service.OfferComparisonTableService;
import com.creditapp.borrower.service.OfferInsightsService;
import com.creditapp.borrower.service.OfferSelectionService;
import com.creditapp.shared.security.AuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/borrower/applications")
@RequiredArgsConstructor
@Slf4j
public class BorrowerOfferController {
    
    private final OfferComparisonTableService offerComparisonTableService;
    private final OfferSelectionService offerSelectionService;
    private final OfferInsightsService offerInsightsService;
    private final AuthorizationService authorizationService;
    
    @GetMapping("/{applicationId}/offers")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<OfferComparisonTableResponse> getOffers(
            @PathVariable UUID applicationId,
            @Valid @ModelAttribute OfferComparisonTableRequest request) {
        
        UUID borrowerId = authorizationService.getCurrentUserId();
        log.info("GET /api/borrower/applications/{}/offers by borrower {}", applicationId, borrowerId);
        
        OfferComparisonTableResponse response = offerComparisonTableService.getOffersTable(
                applicationId, 
                borrowerId, 
                request
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{applicationId}/offers-table")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<OfferComparisonTableResponse> getOffersTable(
            @PathVariable UUID applicationId,
            @Valid @ModelAttribute OfferComparisonTableRequest request) {
        
        UUID borrowerId = authorizationService.getCurrentUserId();
        log.info("GET /api/borrower/applications/{}/offers-table by borrower {}", applicationId, borrowerId);
        
        OfferComparisonTableResponse response = offerComparisonTableService.getOffersTable(
                applicationId, 
                borrowerId, 
                request
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{applicationId}/select-offer")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<SelectOfferResponse> selectOffer(
            @PathVariable UUID applicationId,
            @Valid @RequestBody SelectOfferRequest request) {
        
        UUID borrowerId = authorizationService.getCurrentUserId();
        log.info("POST /api/borrower/applications/{}/select-offer by borrower {}", applicationId, borrowerId);
        
        SelectOfferResponse response = offerSelectionService.selectOffer(
                applicationId,
                borrowerId,
                request.getOfferId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{applicationId}/offers/insights")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<OfferInsightsDTO> getOfferInsights(@PathVariable UUID applicationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        log.info("GET /api/borrower/applications/{}/offers/insights by borrower {}", applicationId, borrowerId);
        
        OfferInsightsDTO insights = offerInsightsService.calculateInsights(applicationId, borrowerId);
        
        // If < 2 offers, return 204 No Content
        if (insights == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(insights);
    }
}

package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.OfferComparisonTableRequest;
import com.creditapp.borrower.dto.OfferComparisonTableResponse;
import com.creditapp.borrower.service.OfferComparisonTableService;
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
    private final AuthorizationService authorizationService;
    
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
}

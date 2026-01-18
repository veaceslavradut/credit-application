package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.ApplicationHistoryRequest;
import com.creditapp.borrower.dto.ApplicationHistoryResponse;
import com.creditapp.borrower.dto.OfferHistoryResponse;
import com.creditapp.borrower.service.ApplicationHistoryService;
import com.creditapp.borrower.service.OfferHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for borrower's application and offer history endpoints.
 * Provides access to historical data with filtering, sorting, and pagination.
 */
@RestController
@RequestMapping("/api/borrower/history")
@RequiredArgsConstructor
@Slf4j
public class BorrowerHistoryController {
    private final OfferHistoryService offerHistoryService;
    private final ApplicationHistoryService applicationHistoryService;

    /**
     * GET /api/borrower/history/offers
     * Retrieve borrower's offer history with pagination.
     * 
     * @param limit Maximum number of records (default 20, max 100)
     * @param offset Pagination offset (default 0)
     * @param sortBy Sort field (default offerReceivedAt)
     * @return OfferHistoryResponse with paginated offers
     */
    @GetMapping("/offers")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<OfferHistoryResponse> getOfferHistory(
            @RequestParam(name = "limit", defaultValue = "20") Integer limit,
            @RequestParam(name = "offset", defaultValue = "0") Integer offset,
            @RequestParam(name = "sortBy", defaultValue = "offerReceivedAt") String sortBy,
            Authentication authentication) {
        
        UUID borrowerId = extractBorrowerId(authentication);
        log.info("Retrieving offer history for borrower: {}", borrowerId);
        
        OfferHistoryResponse response = offerHistoryService.getOfferHistory(borrowerId, limit, offset, sortBy);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/borrower/history/applications
     * Retrieve borrower's application history with filtering and pagination.
     * 
     * @param request Application history request with filters
     * @return ApplicationHistoryResponse with paginated applications
     */
    @GetMapping("/applications")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApplicationHistoryResponse> getApplicationHistory(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "offset", required = false) Integer offset,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "dateRangeStart", required = false) String dateRangeStart,
            @RequestParam(name = "dateRangeEnd", required = false) String dateRangeEnd,
            @RequestParam(name = "loanAmountMin", required = false) java.math.BigDecimal loanAmountMin,
            @RequestParam(name = "loanAmountMax", required = false) java.math.BigDecimal loanAmountMax,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            Authentication authentication) {
        
        UUID borrowerId = extractBorrowerId(authentication);
        log.info("Retrieving application history for borrower: {}", borrowerId);
        
        ApplicationHistoryRequest request = ApplicationHistoryRequest.builder()
            .limit(limit)
            .offset(offset)
            .status(status)
            .loanAmountMin(loanAmountMin)
            .loanAmountMax(loanAmountMax)
            .sortBy(sortBy)
            .build();
        
        ApplicationHistoryResponse response = applicationHistoryService.getApplicationHistory(borrowerId, request);
        return ResponseEntity.ok(response);
    }

    private UUID extractBorrowerId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}

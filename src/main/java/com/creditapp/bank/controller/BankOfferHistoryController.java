package com.creditapp.bank.controller;

import com.creditapp.shared.security.RequiresBankAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for bank offer history endpoints.
 * Provides filtered, sorted, and paginated views of all offers submitted by a bank.
 * 
 * Status: DTOs ready. Service and controller endpoints placeholder for backend integration.
 */
@RestController
@RequestMapping("/api/bank/offers")
public class BankOfferHistoryController {
    
    /**
     * Get paginated history of all offers submitted by the authenticated bank.
     * NOTE: Full implementation will integrate with BankOfferHistoryService
     * 
     * Query parameters:
     * - page: 0-based page number (default 0)
     * - pageSize: items per page, 1-100 (default 20)
     * - statuses: comma-separated filter (SUBMITTED,ACCEPTED,EXPIRED,WITHDRAWN)
     * - dateFrom, dateTo: YYYY-MM-DD format
     * - aprFrom, aprTo: BigDecimal APR range
     * - paymentFrom, paymentTo: BigDecimal monthly payment range
     * - sortBy: submittedDate_DESC (default), apr_ASC, monthlyPayment_DESC, etc.
     */
    @RequiresBankAdmin
    @GetMapping
    public ResponseEntity<?> getOfferHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String statuses,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) BigDecimal aprFrom,
            @RequestParam(required = false) BigDecimal aprTo,
            @RequestParam(required = false) BigDecimal paymentFrom,
            @RequestParam(required = false) BigDecimal paymentTo,
            @RequestParam(required = false, defaultValue = "submittedDate_DESC") String sortBy) {
        
        // TODO: Complete integration:
        // 1. Inject BankOfferHistoryService
        // 2. Extract bankId from SecurityContext (JwtAuthenticationToken)
        // 3. Build OfferHistoryFilter from query parameters
        // 4. Call offerHistoryService.getOfferHistory(bankId, filter, page, pageSize)
        // 5. Return OfferHistoryResponse with 200 OK or error handling
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}


package com.creditapp.bank.controller;

import com.creditapp.auth.filter.JwtAuthenticationToken;
import com.creditapp.bank.dto.OfferHistoryFilter;
import com.creditapp.bank.dto.OfferHistoryResponse;
import com.creditapp.bank.service.BankOfferHistoryService;
import com.creditapp.shared.security.RequiresBankAdmin;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for bank offer history endpoints.
 * Provides filtered, sorted, and paginated views of all offers submitted by a bank.
 */
@RestController
@RequestMapping("/api/bank/offers")
@AllArgsConstructor
public class BankOfferHistoryController {
    
    private final BankOfferHistoryService offerHistoryService;
    
    /**
     * Get paginated history of all offers submitted by the authenticated bank.
     */
    @RequiresBankAdmin
    @GetMapping
    public ResponseEntity<OfferHistoryResponse> getOfferHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String statuses,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) BigDecimal aprFrom,
            @RequestParam(required = false) BigDecimal aprTo,
            @RequestParam(required = false) BigDecimal paymentFrom,
            @RequestParam(required = false) BigDecimal paymentTo,
            @RequestParam(required = false, defaultValue = "offerSubmittedAt_DESC") String sortBy) {
        
        try {
            // Extract bank ID from security context
            JwtAuthenticationToken authentication = 
                    (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UUID bankId = authentication.getOrganizationId();
            
            if (bankId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (pageSize < 1) pageSize = 20;
            if (pageSize > 100) pageSize = 100; // Max 100 items per page
            
            // Parse filter parameters
            List<String> statusList = parseStatuses(statuses);
            LocalDate parsedDateFrom = parseDate(dateFrom);
            LocalDate parsedDateTo = parseDate(dateTo);
            
            // Create filter
            OfferHistoryFilter filter = new OfferHistoryFilter(
                    statusList,
                    parsedDateFrom,
                    parsedDateTo,
                    aprFrom,
                    aprTo,
                    paymentFrom,
                    paymentTo,
                    sortBy
            );
            
            // Get offer history
            OfferHistoryResponse response = offerHistoryService.getOfferHistory(
                    bankId,
                    filter,
                    page,
                    pageSize
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Parse comma-separated status values.
     */
    private List<String> parseStatuses(String statusesParam) {
        if (statusesParam == null || statusesParam.isEmpty()) {
            return null;
        }
        return Arrays.asList(statusesParam.split(","));
    }
    
    /**
     * Parse date string (yyyy-MM-dd format).
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}


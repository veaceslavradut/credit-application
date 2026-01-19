package com.creditapp.bank.controller;

import com.creditapp.bank.dto.*;
import com.creditapp.bank.service.BankAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank/analytics")
@RequiredArgsConstructor
@Slf4j
public class BankAnalyticsController {
    
    private final BankAnalyticsService analyticsService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<AnalyticsResponseDTO> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) AnalyticsRequest.DatePreset preset) {
        
        UUID bankId = extractBankId();
        log.info("GET /api/bank/analytics for bank: {} with preset: {}", bankId, preset);
        
        AnalyticsRequest request = AnalyticsRequest.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .preset(preset != null ? preset : AnalyticsRequest.DatePreset.LAST_30)
                .build();
        
        AnalyticsResponseDTO response = analyticsService.getAnalytics(bankId, request);
        
        log.info("Analytics generated successfully for bank: {}", bankId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/loan-types")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<Map<String, LoanTypeBreakdownDTO>> getLoanTypeBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        UUID bankId = extractBankId();
        
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusDays(29);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }
        
        Map<String, LoanTypeBreakdownDTO> breakdown = analyticsService.getAcceptanceByLoanType(bankId, dateFrom, dateTo);
        return ResponseEntity.ok(breakdown);
    }
    
    @GetMapping("/amount-ranges")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<Map<String, AmountRangeBreakdownDTO>> getAmountRangeBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        UUID bankId = extractBankId();
        
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusDays(29);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }
        
        Map<String, AmountRangeBreakdownDTO> breakdown = analyticsService.getAcceptanceByAmountRange(bankId, dateFrom, dateTo);
        return ResponseEntity.ok(breakdown);
    }
    
    @GetMapping("/trends")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<List<AnalyticsTrendDTO>> getTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "DAILY") String granularity) {
        
        UUID bankId = extractBankId();
        
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusDays(29);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }
        
        List<AnalyticsTrendDTO> trends = analyticsService.getTrendData(bankId, dateFrom, dateTo, granularity);
        return ResponseEntity.ok(trends);
    }
    
    private UUID extractBankId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
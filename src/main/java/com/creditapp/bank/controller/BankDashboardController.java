package com.creditapp.bank.controller;

import com.creditapp.bank.dto.BankDashboardMetrics;
import com.creditapp.bank.dto.BankDashboardResponse;
import com.creditapp.bank.dto.QuickLink;
import com.creditapp.bank.service.BankDashboardService;
import com.creditapp.shared.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank/dashboard")
@RequiredArgsConstructor
@Slf4j
public class BankDashboardController {

    private final BankDashboardService dashboardService;
    private final AuthorizationService authorizationService;

    /**
     * GET /api/bank/dashboard - Retrieve dashboard metrics for authenticated bank
     * 
     * @param timePeriod Time period filter: TODAY, LAST_7_DAYS, LAST_30_DAYS (default: TODAY)
     * @return Dashboard metrics with quick links
     */
    @GetMapping
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<BankDashboardResponse> getDashboard(
        @RequestParam(defaultValue = "TODAY") String timePeriod
    ) {
        UUID bankId = authorizationService.getBankIdFromContext();
        log.info("[DASHBOARD] Bank {} requested dashboard with period {}", bankId, timePeriod);

        BankDashboardMetrics metrics = dashboardService.getDashboardMetrics(bankId, timePeriod);
        
        List<QuickLink> quickLinks = List.of(
            new QuickLink("View Application Queue", "/api/bank/applications/queue", "inbox"),
            new QuickLink("View Rate Cards", "/api/bank/rate-cards", "credit_card"),
            new QuickLink("Submit Offer", "/api/bank/offers/submit", "send")
        );

        BankDashboardResponse response = new BankDashboardResponse(metrics, quickLinks, timePeriod);
        
        return ResponseEntity.ok(response);
    }
}
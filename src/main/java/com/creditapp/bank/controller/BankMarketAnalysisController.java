package com.creditapp.bank.controller;

import com.creditapp.bank.dto.MarketAnalysisDTO;
import com.creditapp.bank.service.BankMarketAnalysisService;
import com.creditapp.shared.security.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bank/rate-cards")
public class BankMarketAnalysisController {

    private final BankMarketAnalysisService analysisService;
    private final AuthorizationService authorizationService;

    public BankMarketAnalysisController(BankMarketAnalysisService analysisService,
                                        AuthorizationService authorizationService) {
        this.analysisService = analysisService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/market-analysis")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<MarketAnalysisDTO> getMarketAnalysis() {
        UUID bankId = authorizationService.getBankIdFromContext();
        MarketAnalysisDTO analysis = analysisService.analyzeMarket(bankId);
        return ResponseEntity.ok(analysis);
    }
}
package com.creditapp.borrower.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.creditapp.borrower.dto.CalculateScenarioRequest;
import com.creditapp.borrower.dto.CalculateScenarioResponse;
import com.creditapp.borrower.service.ScenarioCalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/borrower/scenario-calculator")
@RequiredArgsConstructor
@Slf4j
public class ScenarioCalculatorController {
    
    private final ScenarioCalculatorService scenarioCalculatorService;
    
    /**
     * Calculate loan scenario with different parameters.
     * POST /api/borrower/scenario-calculator
     * 
     * Request: CalculateScenarioRequest { loanAmount, termMonths, bankId (optional) }
     * Response: CalculateScenarioResponse { monthly payment, total cost, apr, fees, etc }
     * 
     * No authentication required. Rate limited to 100 requests/minute per IP.
     */
    @PostMapping
    public ResponseEntity<CalculateScenarioResponse> calculateScenario(
            @Valid @RequestBody CalculateScenarioRequest request) {
        
        log.info("Calculating scenario for loan amount: {}, term: {} months", 
            request.getLoanAmount(), request.getTermMonths());
        
        CalculateScenarioResponse response = scenarioCalculatorService.calculateScenario(request);
        return ResponseEntity.ok(response);
    }
}
package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.CalculateScenarioRequest;
import com.creditapp.borrower.dto.CalculateScenarioResponse;
import com.creditapp.borrower.controller.ScenarioCalculatorController;
import com.creditapp.borrower.service.ScenarioCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioCalculatorControllerTest {

    @Mock
    private ScenarioCalculatorService scenarioCalculatorService;

    @InjectMocks
    private ScenarioCalculatorController scenarioCalculatorController;

    private CalculateScenarioRequest request;
    private CalculateScenarioResponse response;

    @BeforeEach
    void setUp() {
        request = new CalculateScenarioRequest();
        request.setLoanAmount(new BigDecimal("200000"));
        request.setTermMonths(360);

        response = new CalculateScenarioResponse();
        response.setLoanAmount(new BigDecimal("200000"));
        response.setTermMonths(360);
        response.setApr(new BigDecimal("8.5"));
        response.setMonthlyPayment(new BigDecimal("1489.51"));
        response.setTotalCost(new BigDecimal("335624.36"));
        response.setOriginationFee(new BigDecimal("5000.00"));
        response.setInsuranceCost(new BigDecimal("5000.00"));
    }

    @Test
    void testCalculateScenarioReturns200() {
        when(scenarioCalculatorService.calculateScenario(any())).thenReturn(response);

        ResponseEntity<CalculateScenarioResponse> result = scenarioCalculatorController.calculateScenario(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(new BigDecimal("1489.51"), result.getBody().getMonthlyPayment());
    }

    @Test
    void testCalculateScenarioIncludesDisclaimer() {
        when(scenarioCalculatorService.calculateScenario(any())).thenReturn(response);

        ResponseEntity<CalculateScenarioResponse> result = scenarioCalculatorController.calculateScenario(request);

        assertNotNull(result.getBody().getDisclaimer());
        assertFalse(result.getBody().getDisclaimer().isEmpty());
    }

    @Test
    void testInvalidRequest_LoanAmountTooLow() {
        CalculateScenarioRequest invalidRequest = new CalculateScenarioRequest();
        invalidRequest.setLoanAmount(new BigDecimal("500"));
        invalidRequest.setTermMonths(120);

        assertThrows(Exception.class, () -> scenarioCalculatorController.calculateScenario(invalidRequest));
    }
}

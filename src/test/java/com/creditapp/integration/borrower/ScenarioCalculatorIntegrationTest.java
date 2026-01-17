package com.creditapp.integration.borrower;

import com.creditapp.borrower.dto.CalculateScenarioRequest;
import com.creditapp.borrower.dto.CalculateScenarioResponse;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.bank.model.BankRateCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ScenarioCalculatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankRateCardRepository bankRateCardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID bankId;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        BankRateCard rateCard = new BankRateCard();
        rateCard.setId(UUID.randomUUID());
        rateCard.setBankId(bankId);
        rateCard.setBaseApr(new BigDecimal("8.5"));
        rateCard.setOriginationFeePercent(new BigDecimal("2.5"));
        rateCard.setInsuranceCostPercent(new BigDecimal("0.5"));
        bankRateCardRepository.save(rateCard);
    }

    @Test
    void testCalculateScenarioPublicEndpoint() throws Exception {
        CalculateScenarioRequest request = new CalculateScenarioRequest();
        request.setLoanAmount(new BigDecimal("200000"));
        request.setTermMonths(360);

        mockMvc.perform(post("/api/borrower/scenario-calculator")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanAmount").value(200000))
                .andExpect(jsonPath("$.termMonths").value(360))
                .andExpect(jsonPath("$.monthlyPayment").exists())
                .andExpect(jsonPath("$.totalCost").exists());
    }

    @Test
    void testCalculateScenarioWithBankId() throws Exception {
        CalculateScenarioRequest request = new CalculateScenarioRequest();
        request.setLoanAmount(new BigDecimal("150000"));
        request.setTermMonths(240);
        request.setBankId(bankId);

        mockMvc.perform(post("/api/borrower/scenario-calculator")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankId").value(bankId.toString()))
                .andExpect(jsonPath("$.apr").value(8.5));
    }

    @Test
    void testCalculateScenarioInvalidAmount() throws Exception {
        CalculateScenarioRequest request = new CalculateScenarioRequest();
        request.setLoanAmount(new BigDecimal("500"));
        request.setTermMonths(120);

        mockMvc.perform(post("/api/borrower/scenario-calculator")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCalculateScenarioIncludesDisclaimer() throws Exception {
        CalculateScenarioRequest request = new CalculateScenarioRequest();
        request.setLoanAmount(new BigDecimal("200000"));
        request.setTermMonths(360);

        MvcResult result = mockMvc.perform(post("/api/borrower/scenario-calculator")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        CalculateScenarioResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CalculateScenarioResponse.class);

        assertNotNull(response.getDisclaimer());
        assertFalse(response.getDisclaimer().isEmpty());
    }
}

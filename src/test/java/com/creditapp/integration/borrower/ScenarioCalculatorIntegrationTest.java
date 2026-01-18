package com.creditapp.integration.borrower;

import com.creditapp.borrower.dto.CalculateScenarioRequest;
import com.creditapp.borrower.dto.CalculateScenarioResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ScenarioCalculatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCalculateScenarioPublicEndpoint() throws Exception {
        CalculateScenarioRequest request = CalculateScenarioRequest.builder()
                .loanAmount(new BigDecimal("200000"))
                .termMonths(360)
                .build();

        mockMvc.perform(post("/api/borrower/scenario-calculator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanAmount").value(200000))
                .andExpect(jsonPath("$.termMonths").value(360))
                .andExpect(jsonPath("$.apr").exists())
                .andExpect(jsonPath("$.monthlyPayment").exists())
                .andExpect(jsonPath("$.totalCost").exists())
                .andExpect(jsonPath("$.originationFee").exists())
                .andExpect(jsonPath("$.insuranceCost").exists());
    }

    @Test
    public void testCalculateScenarioInvalidAmount() throws Exception {
        CalculateScenarioRequest request = CalculateScenarioRequest.builder()
                .loanAmount(new BigDecimal("500"))
                .termMonths(360)
                .build();

        mockMvc.perform(post("/api/borrower/scenario-calculator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCalculateScenarioIncludesDisclaimer() throws Exception {
        CalculateScenarioRequest request = CalculateScenarioRequest.builder()
                .loanAmount(new BigDecimal("100000"))
                .termMonths(180)
                .build();

        MvcResult result = mockMvc.perform(post("/api/borrower/scenario-calculator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CalculateScenarioResponse response = objectMapper.readValue(responseBody, CalculateScenarioResponse.class);
        
        assertThat(response.getDisclaimer()).isNotNull();
        assertThat(response.getDisclaimer()).contains("preliminary");
    }

    @Test
    public void testCalculateScenarioDeterministic() throws Exception {
        CalculateScenarioRequest request = CalculateScenarioRequest.builder()
                .loanAmount(new BigDecimal("150000"))
                .termMonths(240)
                .build();

        // First calculation
        MvcResult result1 = mockMvc.perform(post("/api/borrower/scenario-calculator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody1 = result1.getResponse().getContentAsString();
        CalculateScenarioResponse response1 = objectMapper.readValue(responseBody1, CalculateScenarioResponse.class);

        // Second calculation with same inputs
        MvcResult result2 = mockMvc.perform(post("/api/borrower/scenario-calculator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody2 = result2.getResponse().getContentAsString();
        CalculateScenarioResponse response2 = objectMapper.readValue(responseBody2, CalculateScenarioResponse.class);

        // Verify deterministic behavior
        assertThat(response1.getMonthlyPayment()).isEqualTo(response2.getMonthlyPayment());
        assertThat(response1.getTotalCost()).isEqualTo(response2.getTotalCost());
        assertThat(response1.getOriginationFee()).isEqualTo(response2.getOriginationFee());
    }

    @Test
    public void testCalculateScenarioResponseTime() throws Exception {
        CalculateScenarioRequest request = CalculateScenarioRequest.builder()
                .loanAmount(new BigDecimal("250000"))
                .termMonths(300)
                .build();

        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/api/borrower/scenario-calculator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Response time SLA: <500ms
        assertThat(duration).isLessThan(500);
    }
}
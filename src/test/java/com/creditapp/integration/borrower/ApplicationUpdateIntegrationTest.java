package com.creditapp.integration.borrower;

import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class ApplicationUpdateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private String borrowerToken;
    private UUID borrowerId;
    private UUID applicationId;
    private Application draftApplication;
    private User testBorrower;

    @BeforeEach
    void setUp() {
        // Create borrower user
        testBorrower = new User();
        testBorrower.setEmail("borrower_update_" + UUID.randomUUID() + "@test.com");
        testBorrower.setPasswordHash("hashed");
        testBorrower.setFirstName("John");
        testBorrower.setLastName("Doe");
        testBorrower.setRole(UserRole.BORROWER);
        testBorrower = userRepository.saveAndFlush(testBorrower);
        borrowerId = testBorrower.getId();

        // Generate JWT token
        borrowerToken = jwtTokenService.generateToken(testBorrower);

        // Create a DRAFT application
        draftApplication = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(10000))
                .loanTermMonths(24)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApplication = applicationRepository.saveAndFlush(draftApplication);
        applicationId = draftApplication.getId();
    }

    @Test
    void testUpdateDraftApplication_AllFields_ShouldReturn200() throws Exception {
        String requestBody = """
                {
                    "loanType": "HOME",
                    "loanAmount": 50000,
                    "loanTermMonths": 120,
                    "currency": "USD",
                    "ratePreference": "FIXED"
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanAmount").value(50000))
                .andExpect(jsonPath("$.loanTermMonths").value(120))
                .andExpect(jsonPath("$.loanType").value("HOME"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.ratePreference").value("FIXED"));

        // Verify database was updated
        Application updated = applicationRepository.findById(applicationId).orElseThrow();
        assertThat(updated.getLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(updated.getLoanTermMonths()).isEqualTo(120);
        assertThat(updated.getLoanType()).isEqualTo("HOME");
        assertThat(updated.getCurrency()).isEqualTo("USD");
        assertThat(updated.getRatePreference()).isEqualTo("FIXED");
    }

    @Test
    void testUpdateDraftApplication_PartialFields_ShouldReturn200() throws Exception {
        String requestBody = """
                {
                    "loanAmount": 15000
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanAmount").value(15000))
                .andExpect(jsonPath("$.loanTermMonths").value(24))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"));

        // Verify only loanAmount was updated
        Application updated = applicationRepository.findById(applicationId).orElseThrow();
        assertThat(updated.getLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(updated.getLoanTermMonths()).isEqualTo(24);
        assertThat(updated.getLoanType()).isEqualTo("PERSONAL");
    }

    @Test
    void testUpdateApplication_LoanAmountTooLow_ShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "loanAmount": 50
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateApplication_LoanAmountTooHigh_ShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "loanAmount": 2000000
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateApplication_LoanTermTooLow_ShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "loanTermMonths": 3
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateApplication_LoanTermTooHigh_ShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "loanTermMonths": 400
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateSubmittedApplication_ShouldReturn400() throws Exception {
        // Change status to SUBMITTED
        draftApplication.setStatus(ApplicationStatus.SUBMITTED);
        applicationRepository.saveAndFlush(draftApplication);

        String requestBody = """
                {
                    "loanAmount": 20000
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Only DRAFT applications can be edited")));
    }

    @Test
    void testUpdateApplication_Unauthenticated_ShouldReturn401() throws Exception {
        String requestBody = """
                {
                    "loanAmount": 20000
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateApplication_DifferentBorrower_ShouldReturn404() throws Exception {
        // Create another borrower
        User anotherBorrower = new User();
        anotherBorrower.setEmail("another_" + UUID.randomUUID() + "@test.com");
        anotherBorrower.setPasswordHash("hashed");
        anotherBorrower.setFirstName("Jane");
        anotherBorrower.setLastName("Smith");
        anotherBorrower.setRole(UserRole.BORROWER);
        anotherBorrower = userRepository.saveAndFlush(anotherBorrower);

        String anotherToken = jwtTokenService.generateToken(anotherBorrower);

        String requestBody = """
                {
                    "loanAmount": 20000
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateApplication_NonExistent_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        String requestBody = """
                {
                    "loanAmount": 20000
                }
                """;

        mockMvc.perform(put("/api/borrower/applications/" + nonExistentId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }
}
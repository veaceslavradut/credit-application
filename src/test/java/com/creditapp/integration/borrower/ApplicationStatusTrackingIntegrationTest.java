package com.creditapp.integration.borrower;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.borrower.dto.ApplicationStatusDTO;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationHistory;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for application status tracking functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class ApplicationStatusTrackingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private EntityManager entityManager;

    private User borrower;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        // Create borrower user with unique email
        borrower = new User();
        borrower.setId(UUID.randomUUID());
        borrower.setEmail("borrower_" + UUID.randomUUID() + "@test.example.com");
        borrower.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
        borrower.setFirstName("John");
        borrower.setLastName("Doe");
        borrower.setPhone("+373-012-345-67");
        borrower.setRole(UserRole.BORROWER);
        borrower = userRepository.save(borrower);

        // Create test application
        testApplication = new Application();
        testApplication.setId(UUID.randomUUID());
        testApplication.setBorrowerId(borrower.getId());
        testApplication.setLoanType("PERSONAL");
        testApplication.setLoanAmount(new BigDecimal("25000.00"));
        testApplication.setLoanTermMonths(36);
        testApplication.setCurrency("EUR");
        testApplication.setRatePreference("VARIABLE");
        testApplication.setStatus(ApplicationStatus.DRAFT);
        testApplication.setCreatedAt(LocalDateTime.now());
        testApplication.setUpdatedAt(LocalDateTime.now());
        testApplication = applicationRepository.save(testApplication);
    }

    @Test
    void testGetApplicationStatusWhenDraft() throws Exception {
        // When: retrieve status of DRAFT application
        MvcResult result = mockMvc.perform(
                get("/api/borrower/applications/{id}/status", testApplication.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getBorrowerToken())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(testApplication.getId().toString()))
                .andExpect(jsonPath("$.currentStatus").value("DRAFT"))
                .andExpect(jsonPath("$.statusHistory").isArray())
                .andExpect(jsonPath("$.progressionPercentage").isNumber())
                .andReturn();

        // Then: verify response structure
        String content = result.getResponse().getContentAsString();
        ApplicationStatusDTO statusDto = objectMapper.readValue(content, ApplicationStatusDTO.class);
        
        assertThat(statusDto.getApplicationId()).isEqualTo(testApplication.getId());
        assertThat(statusDto.getCurrentStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(statusDto.getStatusHistory()).isEmpty();
        assertThat(statusDto.getProgressionPercentage()).isEqualTo(17); // DRAFT is first in progression
    }

    @Test
    void testGetApplicationStatusWithHistoryAfterSubmission() throws Exception {
        // Given: update application status to SUBMITTED with history
        testApplication.setStatus(ApplicationStatus.SUBMITTED);
        testApplication.setSubmittedAt(LocalDateTime.now());
        testApplication = applicationRepository.save(testApplication);

        ApplicationHistory history = ApplicationHistory.builder()
                .id(1L)
                .applicationId(testApplication.getId())
                .oldStatus(ApplicationStatus.DRAFT)
                .newStatus(ApplicationStatus.SUBMITTED)
                .changedAt(LocalDateTime.now())
                .changedByUserId(borrower.getId())
                .changeReason("Borrower submitted application")
                .application(testApplication)
                .build();
        applicationHistoryRepository.save(history);

        // When: retrieve status
        MvcResult result = mockMvc.perform(
                get("/api/borrower/applications/{id}/status", testApplication.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getBorrowerToken())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.statusHistory.length()").value(1))
                .andExpect(jsonPath("$.progressionPercentage").value(33)) // SUBMITTED is second
                .andReturn();

        // Then: verify history is included
        String content = result.getResponse().getContentAsString();
        ApplicationStatusDTO statusDto = objectMapper.readValue(content, ApplicationStatusDTO.class);
        
        assertThat(statusDto.getStatusHistory()).hasSize(1);
        assertThat(statusDto.getStatusHistory().get(0).getOldStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(statusDto.getStatusHistory().get(0).getNewStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(statusDto.getStatusHistory().get(0).getReason()).isEqualTo("Borrower submitted application");
    }

    @Test
    void testGetApplicationStatusWithMultipleTransitions() throws Exception {
        // Given: application with multiple status transitions
        testApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
        testApplication.setSubmittedAt(LocalDateTime.now().minusDays(2));
        testApplication = applicationRepository.save(testApplication);

        // Create history entries
        LocalDateTime draftTime = LocalDateTime.now().minusDays(2);
        ApplicationHistory history1 = ApplicationHistory.builder()
                .id(1L)
                .applicationId(testApplication.getId())
                .oldStatus(ApplicationStatus.DRAFT)
                .newStatus(ApplicationStatus.SUBMITTED)
                .changedAt(draftTime.plusHours(1))
                .changedByUserId(borrower.getId())
                .changeReason("Submitted application")
                .application(testApplication)
                .build();

        ApplicationHistory history2 = ApplicationHistory.builder()
                .id(2L)
                .applicationId(testApplication.getId())
                .oldStatus(ApplicationStatus.SUBMITTED)
                .newStatus(ApplicationStatus.UNDER_REVIEW)
                .changedAt(draftTime.plusHours(2))
                .changeReason("Bank started review")
                .application(testApplication)
                .build();

        applicationHistoryRepository.save(history1);
        applicationHistoryRepository.save(history2);

        // When: retrieve status
        MvcResult result = mockMvc.perform(
                get("/api/borrower/applications/{id}/status", testApplication.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getBorrowerToken())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.statusHistory.length()").value(2))
                .andExpect(jsonPath("$.progressionPercentage").value(50)) // UNDER_REVIEW is third
                .andReturn();

        // Then: verify timeline is reversed chronologically
        String content = result.getResponse().getContentAsString();
        ApplicationStatusDTO statusDto = objectMapper.readValue(content, ApplicationStatusDTO.class);
        
        assertThat(statusDto.getStatusHistory()).hasSize(2);
        assertThat(statusDto.getStatusHistory().get(0).getNewStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        assertThat(statusDto.getStatusHistory().get(1).getNewStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    }

    @Test
    void testGetApplicationStatusDeniesAccessToOtherBorrower() throws Exception {
        // Given: another borrower
        User otherBorrower = new User();
        otherBorrower.setId(UUID.randomUUID());
        otherBorrower.setEmail("other_borrower_" + UUID.randomUUID() + "@example.com");
        otherBorrower.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
        otherBorrower.setFirstName("Jane");
        otherBorrower.setLastName("Smith");
        otherBorrower.setPhone("+373-012-345-68");
        otherBorrower.setRole(UserRole.BORROWER);
        otherBorrower = userRepository.save(otherBorrower);

        // When: other borrower tries to view status
        mockMvc.perform(
                get("/api/borrower/applications/{id}/status", testApplication.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(otherBorrower))
        )
                // Then: access denied
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetApplicationStatusRequiresAuthentication() throws Exception {
        // When: request without authentication
        mockMvc.perform(
                get("/api/borrower/applications/{id}/status", testApplication.getId())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                // Then: unauthorized
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProgressionPercentageCalculation() throws Exception {
        // Test progression percentages for each status
        int[] expectedPercentages = {
                17,  // DRAFT (1/6 + 1)
                33,  // SUBMITTED (2/6)
                50,  // UNDER_REVIEW (3/6)
                67,  // OFFERS_AVAILABLE (4/6)
                83,  // ACCEPTED (5/6)
                100  // COMPLETED (6/6)
        };

        ApplicationStatus[] statuses = {
                ApplicationStatus.DRAFT,
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.UNDER_REVIEW,
                ApplicationStatus.OFFERS_AVAILABLE,
                ApplicationStatus.ACCEPTED,
                ApplicationStatus.COMPLETED
        };

        for (int i = 0; i < statuses.length; i++) {
            // Given: application with specific status
            // Refresh entity from database to avoid stale version issues
            entityManager.flush();
            entityManager.clear();
            testApplication = applicationRepository.findById(testApplication.getId()).orElseThrow();
            testApplication.setStatus(statuses[i]);
            testApplication = applicationRepository.save(testApplication);
            entityManager.flush();

            // When: retrieve status
            MvcResult result = mockMvc.perform(
                    get("/api/borrower/applications/{id}/status", testApplication.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + getBorrowerToken())
            )
                    .andExpect(status().isOk())
                    .andReturn();

            // Then: verify progression percentage
            String content = result.getResponse().getContentAsString();
            ApplicationStatusDTO statusDto = objectMapper.readValue(content, ApplicationStatusDTO.class);
            
            assertThat(statusDto.getProgressionPercentage())
                    .as("Progression for " + statuses[i])
                    .isGreaterThanOrEqualTo(expectedPercentages[i]);
        }
    }

    /**
     * Helper to get JWT token for borrower.
     */
    private String getBorrowerToken() {
        return jwtTokenService.generateToken(borrower);
    }
}

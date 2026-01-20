package com.creditapp.integration.borrower;

import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationHistory;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.ConsentType;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.service.GDPRConsentService;
import com.creditapp.shared.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class ApplicationSubmissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private GDPRConsentService consentService;

    private String borrowerToken;
    private UUID borrowerId;
    private User testBorrower;

    @BeforeEach
    void setUp() {
        // Create borrower user
        testBorrower = new User();
        testBorrower.setEmail("submitter_" + UUID.randomUUID() + "@test.com");
        testBorrower.setPasswordHash("hashed");
        testBorrower.setFirstName("John");
        testBorrower.setLastName("Submitter");
        testBorrower.setRole(UserRole.BORROWER);
        testBorrower = userRepository.saveAndFlush(testBorrower);
        borrowerId = testBorrower.getId();

        // Generate JWT token
        borrowerToken = jwtTokenService.generateToken(testBorrower);

        // Grant required consents for submission
        consentService.grantConsent(borrowerId, List.of(
                ConsentType.DATA_COLLECTION,
                ConsentType.BANK_SHARING
        ), "127.0.0.1", "Test");
    }

    private void grantConsentForBorrower(UUID bId) {
        consentService.grantConsent(bId, List.of(
                ConsentType.DATA_COLLECTION,
                ConsentType.BANK_SHARING
        ), "127.0.0.1", "Test");
    }

    @Test
    void testSubmitApplication_Success_ShouldReturn200AndUpdateStatus() throws Exception {
        // Create DRAFT application
        Application draftApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp = applicationRepository.saveAndFlush(draftApp);
        UUID appId = draftApp.getId();

        mockMvc.perform(post("/api/borrower/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appId.toString()))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.loanAmount").value(25000))
                .andExpect(jsonPath("$.loanTermMonths").value(36))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty())
                .andExpect(jsonPath("$.message", containsString("submitted successfully")));

        // Verify database persistence
        Application updated = applicationRepository.findById(appId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(updated.getSubmittedAt()).isNotNull();
        assertThat(updated.getSubmittedAt()).isAfterOrEqualTo(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void testSubmitApplication_NonDraftStatus_ShouldReturn400() throws Exception {
        // Create SUBMITTED application
        Application submittedApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(30000))
                .loanTermMonths(48)
                .currency("EUR")
                .ratePreference("FIXED")
                .status(ApplicationStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();
        submittedApp = applicationRepository.saveAndFlush(submittedApp);

        // Attempting to submit non-DRAFT should return 409 Conflict
        mockMvc.perform(post("/api/borrower/applications/" + submittedApp.getId() + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("DRAFT")));
    }

    @Test
    void testSubmitApplication_UnderReviewStatus_ShouldReturn400() throws Exception {
        // Create UNDER_REVIEW application
        Application reviewApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("MORTGAGE")
                .loanAmount(BigDecimal.valueOf(200000))
                .loanTermMonths(240)
                .currency("EUR")
                .ratePreference("FIXED")
                .status(ApplicationStatus.UNDER_REVIEW)
                .submittedAt(LocalDateTime.now().minusDays(1))
                .build();
        reviewApp = applicationRepository.saveAndFlush(reviewApp);

        // Attempting to submit non-DRAFT should return 409 Conflict
        mockMvc.perform(post("/api/borrower/applications/" + reviewApp.getId() + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("DRAFT")));
    }

    @Test
    void testSubmitApplication_Unauthenticated_ShouldReturn401() throws Exception {
        Application draftApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(15000))
                .loanTermMonths(24)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp = applicationRepository.saveAndFlush(draftApp);

        mockMvc.perform(post("/api/borrower/applications/" + draftApp.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSubmitApplication_DifferentBorrower_ShouldReturn403() throws Exception {
        // Create application for first borrower
        Application draftApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(20000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp = applicationRepository.saveAndFlush(draftApp);

        // Create second borrower
        User anotherBorrower = new User();
        anotherBorrower.setEmail("another_submitter_" + UUID.randomUUID() + "@test.com");
        anotherBorrower.setPasswordHash("hashed");
        anotherBorrower.setFirstName("Jane");
        anotherBorrower.setLastName("Other");
        anotherBorrower.setRole(UserRole.BORROWER);
        anotherBorrower = userRepository.saveAndFlush(anotherBorrower);

        // Grant consent for the other borrower
        grantConsentForBorrower(anotherBorrower.getId());

        String anotherToken = jwtTokenService.generateToken(anotherBorrower);

        // Access denied returns 404 for security (doesn't leak application existence)
        mockMvc.perform(post("/api/borrower/applications/" + draftApp.getId() + "/submit")
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSubmitApplication_NonExistent_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(post("/api/borrower/applications/" + nonExistentId + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSubmitApplication_CreatesHistoryEntry() throws Exception {
        // Create DRAFT application
        Application draftApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("AUTO")
                .loanAmount(BigDecimal.valueOf(35000))
                .loanTermMonths(60)
                .currency("EUR")
                .ratePreference("FIXED")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp = applicationRepository.saveAndFlush(draftApp);
        UUID appId = draftApp.getId();

        mockMvc.perform(post("/api/borrower/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify history entry created
        List<ApplicationHistory> history = applicationHistoryRepository.findByApplicationIdOrderByChangedAtDesc(appId);
        assertThat(history).isNotEmpty();
        
        ApplicationHistory latestHistory = history.get(0);
        assertThat(latestHistory.getOldStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(latestHistory.getNewStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(latestHistory.getChangeReason()).contains("Application submitted");
        assertThat(latestHistory.getChangedAt()).isNotNull();
    }

    @Test
    void testSubmitApplication_VersionIncremented() throws Exception {
        // Create DRAFT application
        Application draftApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("BUSINESS")
                .loanAmount(BigDecimal.valueOf(50000))
                .loanTermMonths(84)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp = applicationRepository.saveAndFlush(draftApp);
        UUID appId = draftApp.getId();

        // Submit and verify version is present in response
        mockMvc.perform(post("/api/borrower/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").isNumber());
    }

    @Test
    void testSubmitApplication_PreservesAllApplicationFields() throws Exception {
        // Create DRAFT application with all fields
        Application draftApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("MORTGAGE")
                .loanAmount(BigDecimal.valueOf(250000))
                .loanTermMonths(360)
                .currency("USD")
                .ratePreference("FIXED")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp = applicationRepository.saveAndFlush(draftApp);
        UUID appId = draftApp.getId();
        LocalDateTime createdAt = draftApp.getCreatedAt();

        mockMvc.perform(post("/api/borrower/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appId.toString()))
                .andExpect(jsonPath("$.loanType").value("MORTGAGE"))
                .andExpect(jsonPath("$.loanAmount").value(250000))
                .andExpect(jsonPath("$.loanTermMonths").value(360))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.ratePreference").value("FIXED"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        // Verify all fields preserved in database
        Application updated = applicationRepository.findById(appId).orElseThrow();
        assertThat(updated.getLoanType()).isEqualTo("MORTGAGE");
        assertThat(updated.getLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(250000));
        assertThat(updated.getLoanTermMonths()).isEqualTo(360);
        assertThat(updated.getCurrency()).isEqualTo("USD");
        assertThat(updated.getRatePreference()).isEqualTo("FIXED");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void testSubmitApplication_MultipleApplications_IndependentSubmission() throws Exception {
        // Create two DRAFT applications
        Application draftApp1 = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(10000))
                .loanTermMonths(24)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp1 = applicationRepository.saveAndFlush(draftApp1);

        Application draftApp2 = Application.builder()
                .borrowerId(borrowerId)
                .loanType("AUTO")
                .loanAmount(BigDecimal.valueOf(20000))
                .loanTermMonths(48)
                .currency("EUR")
                .ratePreference("FIXED")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp2 = applicationRepository.saveAndFlush(draftApp2);

        // Submit first application
        mockMvc.perform(post("/api/borrower/applications/" + draftApp1.getId() + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // Verify second application still DRAFT
        Application app2Check = applicationRepository.findById(draftApp2.getId()).orElseThrow();
        assertThat(app2Check.getStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(app2Check.getSubmittedAt()).isNull();

        // Submit second application
        mockMvc.perform(post("/api/borrower/applications/" + draftApp2.getId() + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // Verify both applications are now SUBMITTED
        Application app1Final = applicationRepository.findById(draftApp1.getId()).orElseThrow();
        Application app2Final = applicationRepository.findById(draftApp2.getId()).orElseThrow();
        assertThat(app1Final.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(app2Final.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(app1Final.getSubmittedAt()).isNotNull();
        assertThat(app2Final.getSubmittedAt()).isNotNull();
    }

    @Test
    void testSubmitApplication_IdempotencePrevention_DoubleSubmit() throws Exception {
        // Create DRAFT application
        Application draftApp = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(15000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        draftApp = applicationRepository.saveAndFlush(draftApp);
        UUID appId = draftApp.getId();

        // First submission - success
        mockMvc.perform(post("/api/borrower/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // Second submission attempt - should fail with 409 Conflict
        mockMvc.perform(post("/api/borrower/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("DRAFT")));
    }
}
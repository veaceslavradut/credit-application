package com.creditapp.integration.borrower;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.service.JwtTokenService;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class ApplicationHistoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4")
        .withDatabaseName("creditapp_test")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID borrowerId;
    private UUID bankId;
    private String borrowerToken;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // Create borrower user
        User borrower = new User();
        borrower.setEmail("borrower-history@test.com");
        borrower.setPasswordHash(passwordEncoder.encode("Password123!"));
        borrower.setRole(UserRole.BORROWER);
        borrower.setEnabled(true);
        borrower = userRepository.saveAndFlush(borrower);
        borrowerId = borrower.getId();

        // Create bank organization
        Organization bank = new Organization();
        bank.setName("Test Bank");
        bank.setTaxId("123456789");
        bank.setCountryCode("US");
        bank = organizationRepository.saveAndFlush(bank);
        bankId = bank.getId();

        borrowerToken = jwtTokenService.generateToken(borrower);
    }

    @Test
    void testGetApplicationHistory_ReturnsAllApplications() throws Exception {
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setBorrowerId(borrowerId);
        app.setLoanAmount(BigDecimal.valueOf(10000));
        app.setLoanTermMonths(36);
        app.setLoanType("Personal Loan");
        app.setCurrency("USD");
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setCreatedAt(LocalDateTime.now().minusDays(10));
        app.setSubmittedAt(LocalDateTime.now().minusDays(9));
        applicationRepository.save(app);

        mockMvc.perform(get("/api/borrower/history/applications")
                .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(1)))
                .andExpect(jsonPath("$.applications[0].status", equalTo("SUBMITTED")))
                .andExpect(jsonPath("$.applications[0].loanAmount", equalTo(10000.0)))
                .andExpect(jsonPath("$.applications[0].termMonths", equalTo(36)))
                .andExpect(jsonPath("$.totalCount", equalTo(1)));
    }

    @Test
    void testGetApplicationHistory_FilterByStatus() throws Exception {
        Application submittedApp = new Application();
        submittedApp.setId(UUID.randomUUID());
        submittedApp.setBorrowerId(borrowerId);
        submittedApp.setLoanAmount(BigDecimal.valueOf(10000));
        submittedApp.setLoanTermMonths(36);
        submittedApp.setLoanType("Personal Loan");
        submittedApp.setCurrency("USD");
        submittedApp.setStatus(ApplicationStatus.SUBMITTED);
        submittedApp.setCreatedAt(LocalDateTime.now().minusDays(10));
        submittedApp.setSubmittedAt(LocalDateTime.now().minusDays(9));
        applicationRepository.save(submittedApp);

        Application draftApp = new Application();
        draftApp.setId(UUID.randomUUID());
        draftApp.setBorrowerId(borrowerId);
        draftApp.setLoanAmount(BigDecimal.valueOf(5000));
        draftApp.setLoanTermMonths(24);
        draftApp.setLoanType("Auto Loan");
        draftApp.setCurrency("USD");
        draftApp.setStatus(ApplicationStatus.DRAFT);
        draftApp.setCreatedAt(LocalDateTime.now().minusDays(3));
        applicationRepository.save(draftApp);

        mockMvc.perform(get("/api/borrower/history/applications")
                .header("Authorization", "Bearer " + borrowerToken)
                .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(1)))
                .andExpect(jsonPath("$.applications[0].status", equalTo("SUBMITTED")));
    }

    @Test
    void testGetApplicationHistory_CalculatesOfferCount() throws Exception {
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setBorrowerId(borrowerId);
        app.setLoanAmount(BigDecimal.valueOf(10000));
        app.setLoanTermMonths(36);
        app.setLoanType("Personal Loan");
        app.setCurrency("USD");
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setCreatedAt(LocalDateTime.now().minusDays(10));
        app.setSubmittedAt(LocalDateTime.now().minusDays(9));
        applicationRepository.save(app);

        // Add 2 offers to the application
        for (int i = 0; i < 2; i++) {
            Offer offer = new Offer();
            offer.setId(UUID.randomUUID());
            offer.setApplicationId(app.getId());
            offer.setBankId(bankId);
            offer.setOfferStatus(OfferStatus.SUBMITTED);
            offer.setApr(BigDecimal.valueOf(4.0 + i * 0.5));
            offer.setMonthlyPayment(BigDecimal.valueOf(300));
            offer.setTotalCost(BigDecimal.valueOf(10800));
            offer.setOriginationFee(BigDecimal.valueOf(100));
            offer.setInsuranceCost(BigDecimal.valueOf(50));
            offer.setProcessingTimeDays(5);
            offer.setValidityPeriodDays(30);
            offer.setExpiresAt(LocalDateTime.now().plusDays(30));
            offer.setOfferSubmittedAt(LocalDateTime.now().minusDays(2));
            offerRepository.save(offer);
        }

        mockMvc.perform(get("/api/borrower/history/applications")
                .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications[0].offerCount", equalTo(2)))
                .andExpect(jsonPath("$.applications[0].bestAPR", equalTo(4.0)));
    }

    @Test
    void testGetApplicationHistory_DeterminesExpirationStatus() throws Exception {
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setBorrowerId(borrowerId);
        app.setLoanAmount(BigDecimal.valueOf(10000));
        app.setLoanTermMonths(36);
        app.setLoanType("Personal Loan");
        app.setCurrency("USD");
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setCreatedAt(LocalDateTime.now().minusDays(10));
        app.setSubmittedAt(LocalDateTime.now().minusDays(9));
        applicationRepository.save(app);

        Offer activeOffer = new Offer();
        activeOffer.setId(UUID.randomUUID());
        activeOffer.setApplicationId(app.getId());
        activeOffer.setBankId(bankId);
        activeOffer.setOfferStatus(OfferStatus.SUBMITTED);
        activeOffer.setApr(BigDecimal.valueOf(4.0));
        activeOffer.setMonthlyPayment(BigDecimal.valueOf(300));
        activeOffer.setTotalCost(BigDecimal.valueOf(10800));
        activeOffer.setOriginationFee(BigDecimal.valueOf(100));
        activeOffer.setInsuranceCost(BigDecimal.valueOf(50));
        activeOffer.setProcessingTimeDays(5);
        activeOffer.setValidityPeriodDays(30);
        activeOffer.setExpiresAt(LocalDateTime.now().plusDays(30));
        activeOffer.setOfferSubmittedAt(LocalDateTime.now().minusDays(2));
        offerRepository.save(activeOffer);

        mockMvc.perform(get("/api/borrower/history/applications")
                .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications[0].expirationStatus", equalTo("all_active")));
    }

    @Test
    void testGetApplicationHistory_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(get("/api/borrower/history/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetApplicationHistory_EmptyList() throws Exception {
        mockMvc.perform(get("/api/borrower/history/applications")
                .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(0)))
                .andExpect(jsonPath("$.totalCount", equalTo(0)))
                .andExpect(jsonPath("$.hasMore", equalTo(false)));
    }
}

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
@Disabled("OfferHistoryIntegrationTest disabled - requires OfferCalculationService mock")
public class OfferHistoryIntegrationTest {

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
    private UUID applicationId;
    private UUID bankId;
    private String borrowerToken;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        borrowerId = UUID.randomUUID();
        bankId = UUID.randomUUID();
        applicationId = UUID.randomUUID();

        User borrower = new User();
        borrower.setId(borrowerId);
        borrower.setEmail("borrower@test.com");
        borrower.setPasswordHash(passwordEncoder.encode("Password123!"));
        borrower.setRole(UserRole.BORROWER);
        borrower = userRepository.save(borrower);
        userRepository.flush();

        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Test Bank");
        bank.setTaxId("123456789");
        bank.setCountryCode("US");
        organizationRepository.save(bank);
        organizationRepository.flush();

        Application app = new Application();
        app.setId(applicationId);
        app.setBorrowerId(borrowerId);
        app.setLoanAmount(BigDecimal.valueOf(10000));
        app.setLoanTermMonths(36);
        app.setLoanType("Personal Loan");
        app.setCurrency("USD");
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setCreatedAt(LocalDateTime.now().minusDays(10));
        app.setSubmittedAt(LocalDateTime.now().minusDays(9));
        applicationRepository.save(app);

        borrowerToken = jwtTokenService.generateToken(borrower);
    }

    @Test
    void testGetOfferHistory_ReturnsAllOffers() throws Exception {
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        offer.setApr(BigDecimal.valueOf(5.5));
        offer.setMonthlyPayment(BigDecimal.valueOf(310.50));
        offer.setTotalCost(BigDecimal.valueOf(11178.00));
        offer.setOriginationFee(BigDecimal.valueOf(100));
        offer.setInsuranceCost(BigDecimal.valueOf(50));
        offer.setValidityPeriodDays(30);
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        offer.setOfferSubmittedAt(LocalDateTime.now().minusDays(2));
        offerRepository.save(offer);

        mockMvc.perform(get("/api/borrower/history/offers")
                .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offers", hasSize(1)))
                .andExpect(jsonPath("$.offers[0].bankName", equalTo("Test Bank")))
                .andExpect(jsonPath("$.offers[0].apr", equalTo(5.5)))
                .andExpect(jsonPath("$.totalCount", equalTo(1)));
    }

    @Test
    void testGetOfferHistory_PaginationWorks() throws Exception {
        for (int i = 0; i < 25; i++) {
            Offer offer = new Offer();
            offer.setId(UUID.randomUUID());
            offer.setApplicationId(applicationId);
            offer.setBankId(bankId);
            offer.setOfferStatus(OfferStatus.SUBMITTED);
            offer.setApr(BigDecimal.valueOf(4.0 + i * 0.1));
            offer.setMonthlyPayment(BigDecimal.valueOf(300));
            offer.setTotalCost(BigDecimal.valueOf(10800));
            offer.setOriginationFee(BigDecimal.valueOf(100));
            offer.setInsuranceCost(BigDecimal.valueOf(50));
            offer.setValidityPeriodDays(30);
            offer.setExpiresAt(LocalDateTime.now().plusDays(30));
            offer.setOfferSubmittedAt(LocalDateTime.now().minusDays(2));
            offerRepository.save(offer);
        }

        mockMvc.perform(get("/api/borrower/history/offers")
                .header("Authorization", "Bearer " + borrowerToken)
                .param("limit", "20")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offers", hasSize(20)))
                .andExpect(jsonPath("$.totalCount", equalTo(25)))
                .andExpect(jsonPath("$.hasMore", equalTo(true)));
    }

    @Test
    void testGetOfferHistory_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(get("/api/borrower/history/offers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetOfferHistory_EmptyList() throws Exception {
        mockMvc.perform(get("/api/borrower/history/offers")
                .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offers", hasSize(0)))
                .andExpect(jsonPath("$.totalCount", equalTo(0)))
                .andExpect(jsonPath("$.hasMore", equalTo(false)));
    }
}

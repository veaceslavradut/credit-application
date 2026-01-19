package com.creditapp.integration.bank;

import com.creditapp.auth.repository.UserRepository;
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
import com.creditapp.shared.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Bank Application Review Integration Tests")
class BankApplicationReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OfferRepository offerRepository;

    private Organization testBank;
    private String testBankToken;
    private UUID applicationId;

    @BeforeEach
    void setup() {
        testBank = new Organization();
        testBank.setName("Test Bank " + UUID.randomUUID().toString().substring(0, 8));
        testBank.setTaxId("TAX" + UUID.randomUUID().toString().substring(0, 12));
        testBank.setCountryCode("MD");
        testBank.setActive(true);
        testBank = organizationRepository.save(testBank);

        User bankAdmin = new User();
        bankAdmin.setEmail("admin-" + testBank.getId() + "@bank.com");
        bankAdmin.setFirstName("Admin");
        bankAdmin.setLastName("Test");
        bankAdmin.setOrganizationId(testBank.getId());
        bankAdmin.setRole(UserRole.BANK_ADMIN);
        bankAdmin.setPasswordHash("hash");
        bankAdmin.setIsActive(true);
        userRepository.save(bankAdmin);

        testBankToken = "Bearer " + jwtTokenService.generateToken(bankAdmin);

        User borrower = new User();
        borrower.setEmail("borrower@test.com");
        borrower.setFirstName("Jane");
        borrower.setLastName("Doe");
        borrower.setPhoneNumber("+373123456789");
        borrower.setRole(UserRole.BORROWER);
        borrower.setPasswordHash("hash");
        borrower.setIsActive(true);
        userRepository.save(borrower);

        applicationId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrower.getId());
        application.setLoanType("AUTO");
        application.setLoanAmount(BigDecimal.valueOf(75000));
        application.setLoanTermMonths(84);
        application.setCurrency("EUR");
        application.setStatus(ApplicationStatus.SUBMITTED);
        applicationRepository.save(application);

        Offer offer = new Offer();
        offer.setApplicationId(applicationId);
        offer.setBankId(testBank.getId());
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setApr(BigDecimal.valueOf(6.5));
        offer.setMonthlyPayment(BigDecimal.valueOf(1400));
        offer.setTotalCost(BigDecimal.valueOf(117600));
        offer.setOriginationFee(BigDecimal.valueOf(2250));
        offer.setProcessingTimeDays(7);
        offer.setValidityPeriodDays(30);
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        offerRepository.save(offer);
    }

    @Test
    @DisplayName("Should return 200 with application details")
    void testGetDetails_Success() throws Exception {
        mockMvc.perform(get("/api/bank/applications/{id}/details", applicationId)
                .header("Authorization", testBankToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId", is(applicationId.toString())))
                .andExpect(jsonPath("$.borrower", notNullValue()))
                .andExpect(jsonPath("$.loanRequest", notNullValue()))
                .andExpect(jsonPath("$.consents", notNullValue()));
    }

    @Test
    @DisplayName("Should include borrower details")
    void testGetDetails_BorrowerInfo() throws Exception {
        mockMvc.perform(get("/api/bank/applications/{id}/details", applicationId)
                .header("Authorization", testBankToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrower.firstName", is("Jane")))
                .andExpect(jsonPath("$.borrower.email", is("borrower@test.com")));
    }

    @Test
    @DisplayName("Should return 3 consents")
    void testGetDetails_Consents() throws Exception {
        mockMvc.perform(get("/api/bank/applications/{id}/details", applicationId)
                .header("Authorization", testBankToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consents", hasSize(3)))
                .andExpect(jsonPath("$.consents[0].consentNumber", is(1)))
                .andExpect(jsonPath("$.consents[1].consentNumber", is(2)))
                .andExpect(jsonPath("$.consents[2].consentNumber", is(3)));
    }

    @Test
    @DisplayName("Should return 401 without token")
    void testGetDetails_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/bank/applications/{id}/details", applicationId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 for non-existent application")
    void testGetDetails_NotFound() throws Exception {
        mockMvc.perform(get("/api/bank/applications/{id}/details", UUID.randomUUID())
                .header("Authorization", testBankToken))
                .andExpect(status().is(400));
    }
}
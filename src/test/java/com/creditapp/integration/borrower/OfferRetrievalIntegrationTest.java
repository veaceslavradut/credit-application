package com.creditapp.integration.borrower;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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
import com.creditapp.shared.repository.OrganizationRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class OfferRetrievalIntegrationTest {

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
    private OrganizationRepository organizationRepository;

    private UUID borrowerId;
    private UUID applicationId;
    private UUID bankId;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();
        applicationRepository.deleteAll();
        organizationRepository.deleteAll();

        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        bankId = UUID.randomUUID();

        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Test Bank");
        bank.setCountryCode("MD");
        bank.setLogoUrl("https://bank.com/logo.png");
        organizationRepository.save(bank);

        Application app = new Application();
        app.setId(applicationId);
        app.setBorrowerId(borrowerId);
        app.setStatus(ApplicationStatus.SUBMITTED);
        applicationRepository.save(app);

        Offer offer1 = new Offer();
        offer1.setApplicationId(applicationId);
        offer1.setBankId(bankId);
        offer1.setApr(new BigDecimal("3.50"));
        offer1.setMonthlyPayment(new BigDecimal("1500.00"));
        offer1.setTotalCost(new BigDecimal("45000.00"));
        offer1.setOriginationFee(new BigDecimal("500.00"));
        offer1.setInsuranceCost(new BigDecimal("200.00"));
        offer1.setProcessingTimeDays(5);
        offer1.setValidityPeriodDays(30);
        offer1.setRequiredDocuments("ID, Paycheck, Tax Return");
        offer1.setExpiresAt(LocalDateTime.now().plusDays(30));
        offer1.setOfferStatus(OfferStatus.CALCULATED);
        offerRepository.save(offer1);

        Offer offer2 = new Offer();
        offer2.setApplicationId(applicationId);
        offer2.setBankId(bankId);
        offer2.setApr(new BigDecimal("4.25"));
        offer2.setMonthlyPayment(new BigDecimal("1520.00"));
        offer2.setTotalCost(new BigDecimal("46000.00"));
        offer2.setOriginationFee(new BigDecimal("600.00"));
        offer2.setInsuranceCost(new BigDecimal("250.00"));
        offer2.setProcessingTimeDays(7);
        offer2.setValidityPeriodDays(30);
        offer2.setRequiredDocuments("ID, Recent Bank Statement");
        offer2.setExpiresAt(LocalDateTime.now().plusDays(30));
        offer2.setOfferStatus(OfferStatus.CALCULATED);
        offerRepository.save(offer2);
    }

    @Test
    @WithMockUser(username = "borrower@example.com", authorities = {"BORROWER"})
    void testGetOffersSuccessful() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/{applicationId}/offers", applicationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.offers", hasSize(2)))
            .andExpect(jsonPath("$.totalOffersCount", is(2)))
            .andExpect(jsonPath("$.disclaimer", notNullValue()))
            .andExpect(jsonPath("$.retrievedAt", notNullValue()))
            .andExpect(jsonPath("$.nextRefreshAvailableAt", notNullValue()));
    }

    @Test
    @WithMockUser(username = "borrower@example.com", authorities = {"BORROWER"})
    void testOffersSortedByAprAscending() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/{applicationId}/offers", applicationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.offers[0].apr", is(3.50)))
            .andExpect(jsonPath("$.offers[1].apr", is(4.25)));
    }

    @Test
    @WithMockUser(username = "wrongborrower@example.com", authorities = {"BORROWER"})
    void testAccessDeniedDifferentBorrower() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/{applicationId}/offers", applicationId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "borrower@example.com", authorities = {"BORROWER"})
    void testApplicationNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/borrower/applications/{applicationId}/offers", nonExistentId))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "borrower@example.com", authorities = {"BORROWER"})
    void testDraftApplicationNotAllowed() throws Exception {
        Application draftApp = new Application();
        draftApp.setId(UUID.randomUUID());
        draftApp.setBorrowerId(borrowerId);
        draftApp.setStatus(ApplicationStatus.DRAFT);
        applicationRepository.save(draftApp);

        mockMvc.perform(get("/api/borrower/applications/{applicationId}/offers", draftApp.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "borrower@example.com", authorities = {"BORROWER"})
    void testEmptyOffersReturnsEmptyArray() throws Exception {
        Application emptyApp = new Application();
        emptyApp.setId(UUID.randomUUID());
        emptyApp.setBorrowerId(borrowerId);
        emptyApp.setStatus(ApplicationStatus.SUBMITTED);
        applicationRepository.save(emptyApp);

        mockMvc.perform(get("/api/borrower/applications/{applicationId}/offers", emptyApp.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.offers", hasSize(0)))
            .andExpect(jsonPath("$.totalOffersCount", is(0)));
    }

    @Test
    void testUnauthenticatedAccessForbidden() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/{applicationId}/offers", applicationId))
            .andExpect(status().isForbidden());
    }
}
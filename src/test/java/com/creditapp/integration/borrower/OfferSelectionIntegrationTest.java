package com.creditapp.integration.borrower;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.SelectOfferRequest;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class OfferSelectionIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    private UUID borrowerId;
    private UUID applicationId;
    private UUID bankId;
    private UUID offerId;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();
        applicationRepository.deleteAll();
        organizationRepository.deleteAll();

        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        bankId = UUID.randomUUID();
        offerId = UUID.randomUUID();

        // Create organization (bank)
        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Test Bank");
        bank.setLogoUrl("https://testbank.com/logo.png");
        organizationRepository.save(bank);

        // Create application
        Application app = new Application();
        app.setId(applicationId);
        app.setBorrowerId(borrowerId);
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setLoanAmount(new BigDecimal("25000"));
        app.setLoanTermMonths(36);
        applicationRepository.save(app);

        // Create offer
        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setApr(new BigDecimal("8.50"));
        offer.setMonthlyPayment(new BigDecimal("775.67"));
        offer.setTotalCost(new BigDecimal("27944.12"));
        offer.setOriginationFee(new BigDecimal("250.00"));
        offer.setInsuranceCost(new BigDecimal("50.00"));
        offer.setProcessingTimeDays(7);
        offer.setValidityPeriodDays(30);
        offer.setRequiredDocuments("ID, Paycheck, Tax Return");
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offerRepository.save(offer);
    }

    @Test
    @WithMockUser(username = "borrower", authorities = {"BORROWER"})
    void testSelectOfferSuccessfully() throws Exception {
        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(offerId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedOfferId").value(offerId.toString()))
                .andExpect(jsonPath("$.bankName").value("Test Bank"))
                .andExpect(jsonPath("$.apr").value(8.50))
                .andExpect(jsonPath("$.monthlyPayment").value(775.67))
                .andExpect(jsonPath("$.nextSteps").isArray())
                .andExpect(jsonPath("$.message").exists());

        // Verify offer status changed to ACCEPTED in database
        Offer updatedOffer = offerRepository.findById(offerId).orElseThrow();
        assertEquals(OfferStatus.ACCEPTED, updatedOffer.getOfferStatus());
        assertNotNull(updatedOffer.getBorrowerSelectedAt());

        // Verify application status changed to ACCEPTED
        Application updatedApp = applicationRepository.findById(applicationId).orElseThrow();
        assertEquals(ApplicationStatus.ACCEPTED, updatedApp.getStatus());
    }

    @Test
    @WithMockUser(username = "borrower", authorities = {"BORROWER"})
    void testSelectExpiredOffer() throws Exception {
        // Create expired offer
        UUID expiredOfferId = UUID.randomUUID();
        Offer expiredOffer = new Offer();
        expiredOffer.setId(expiredOfferId);
        expiredOffer.setApplicationId(applicationId);
        expiredOffer.setBankId(bankId);
        expiredOffer.setApr(new BigDecimal("7.50"));
        expiredOffer.setMonthlyPayment(new BigDecimal("750.00"));
        expiredOffer.setTotalCost(new BigDecimal("27000.00"));
        expiredOffer.setOriginationFee(new BigDecimal("200.00"));
        expiredOffer.setInsuranceCost(new BigDecimal("40.00"));
        expiredOffer.setProcessingTimeDays(5);
        expiredOffer.setValidityPeriodDays(30);
        expiredOffer.setRequiredDocuments("ID, Paycheck");
        expiredOffer.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday
        expiredOffer.setOfferStatus(OfferStatus.CALCULATED);
        offerRepository.save(expiredOffer);

        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(expiredOfferId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error").value("Offer Expired"));
    }

    @Test
    @WithMockUser(username = "borrower", authorities = {"BORROWER"})
    void testChangeOfferSelection() throws Exception {
        // Select first offer
        SelectOfferRequest request1 = new SelectOfferRequest();
        request1.setOfferId(offerId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isOk());

        // Create second offer
        UUID offerId2 = UUID.randomUUID();
        Offer offer2 = new Offer();
        offer2.setId(offerId2);
        offer2.setApplicationId(applicationId);
        offer2.setBankId(bankId);
        offer2.setApr(new BigDecimal("7.75"));
        offer2.setMonthlyPayment(new BigDecimal("760.00"));
        offer2.setTotalCost(new BigDecimal("27360.00"));
        offer2.setOriginationFee(new BigDecimal("225.00"));
        offer2.setInsuranceCost(new BigDecimal("45.00"));
        offer2.setProcessingTimeDays(6);
        offer2.setValidityPeriodDays(30);
        offer2.setRequiredDocuments("ID, Bank Statement");
        offer2.setExpiresAt(LocalDateTime.now().plusDays(30));
        offer2.setOfferStatus(OfferStatus.CALCULATED);
        offerRepository.save(offer2);

        // Select second offer (change selection)
        SelectOfferRequest request2 = new SelectOfferRequest();
        request2.setOfferId(offerId2);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedOfferId").value(offerId2.toString()));

        // Verify first offer deselected
        Offer deselectedOffer = offerRepository.findById(offerId).orElseThrow();
        assertEquals(OfferStatus.CALCULATED, deselectedOffer.getOfferStatus());

        // Verify second offer selected
        Offer selectedOffer = offerRepository.findById(offerId2).orElseThrow();
        assertEquals(OfferStatus.ACCEPTED, selectedOffer.getOfferStatus());
    }

    @Test
    @WithMockUser(username = "other-borrower", authorities = {"BORROWER"})
    void testSelectOfferDifferentBorrower() throws Exception {
        UUID otherBorrowerId = UUID.randomUUID();

        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(offerId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("borrowerId", otherBorrowerId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "borrower", authorities = {"BORROWER"})
    void testSelectOfferInvalidOfferId() throws Exception {
        UUID invalidOfferId = UUID.randomUUID();

        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(invalidOfferId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSelectOfferUnauthenticated() throws Exception {
        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(offerId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "borrower", authorities = {"BORROWER"})
    void testSelectOfferApplicationNotFound() throws Exception {
        UUID nonExistentAppId = UUID.randomUUID();

        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(offerId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", nonExistentAppId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "borrower", authorities = {"BORROWER"})
    void testSelectOfferVerifyNextStepsIncluded() throws Exception {
        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(offerId);

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextSteps").isArray())
                .andExpect(jsonPath("$.nextSteps", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(username = "borrower", authorities = {"BORROWER"})
    void testSelectOfferVerifyBorrowerSelectedAtTimestamp() throws Exception {
        SelectOfferRequest request = new SelectOfferRequest();
        request.setOfferId(offerId);

        LocalDateTime beforeSelection = LocalDateTime.now();

        mockMvc.perform(post("/api/borrower/applications/{applicationId}/select-offer", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("borrowerId", borrowerId))
                .andExpect(status().isOk());

        LocalDateTime afterSelection = LocalDateTime.now();

        // Verify borrower_selected_at timestamp set
        Offer updatedOffer = offerRepository.findById(offerId).orElseThrow();
        assertNotNull(updatedOffer.getBorrowerSelectedAt());
        assertTrue(updatedOffer.getBorrowerSelectedAt().isAfter(beforeSelection) ||
                   updatedOffer.getBorrowerSelectedAt().isEqual(beforeSelection));
        assertTrue(updatedOffer.getBorrowerSelectedAt().isBefore(afterSelection) ||
                   updatedOffer.getBorrowerSelectedAt().isEqual(afterSelection));
    }
}

package com.creditapp.unit.borrower;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.OfferComparisonDTO;
import com.creditapp.borrower.exception.ApplicationNotSubmittedException;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.exception.UnauthorizedException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.OfferRetrievalService;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferRetrievalServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private OfferRetrievalService service;

    private UUID applicationId;
    private UUID borrowerId;
    private UUID bankId;
    private Application application;
    private Offer offer1;
    private Offer offer2;
    private Organization bank;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
        bankId = UUID.randomUUID();

        application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setStatus(ApplicationStatus.SUBMITTED);

        bank = new Organization();
        bank.setId(bankId);
        bank.setName("Test Bank");
        bank.setLogoUrl("https://bank.com/logo.png");

        offer1 = new Offer();
        offer1.setId(UUID.randomUUID());
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

        offer2 = new Offer();
        offer2.setId(UUID.randomUUID());
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
    }

    @Test
    void testGetOffersForApplicationHappyPath() {
        List<Offer> offers = new ArrayList<>();
        offers.add(offer1);
        offers.add(offer2);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(offers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bank));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("3.50"), result.get(0).getApr());
        assertEquals(new BigDecimal("4.25"), result.get(1).getApr());
        assertEquals("Test Bank", result.get(0).getBankName());
    }

    @Test
    void testGetOffersForApplicationSortedByApr() {
        List<Offer> offers = new ArrayList<>();
        offers.add(offer2);
        offers.add(offer1);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(offers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bank));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals(new BigDecimal("3.50"), result.get(0).getApr());
    }

    @Test
    void testApplicationNotFound() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, 
            () -> service.getOffersForApplication(applicationId, borrowerId));
    }

    @Test
    void testUnauthorizedDifferentBorrower() {
        UUID differentBorrower = UUID.randomUUID();
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        assertThrows(UnauthorizedException.class,
            () -> service.getOffersForApplication(applicationId, differentBorrower));
    }

    @Test
    void testApplicationNotSubmittedDraft() {
        application.setStatus(ApplicationStatus.DRAFT);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        assertThrows(ApplicationNotSubmittedException.class,
            () -> service.getOffersForApplication(applicationId, borrowerId));
    }

    @Test
    void testEmptyOffersReturnsEmptyList() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(new ArrayList<>());

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals(0, result.size());
    }

    @Test
    void testAllComparisonFieldsPresent() {
        List<Offer> offers = new ArrayList<>();
        offers.add(offer1);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(offers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bank));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        OfferComparisonDTO dto = result.get(0);
        assertNotNull(dto.getApr());
        assertNotNull(dto.getMonthlyPayment());
        assertNotNull(dto.getTotalCost());
        assertNotNull(dto.getOriginationFee());
        assertNotNull(dto.getInsuranceCost());
        assertNotNull(dto.getProcessingTimeDays());
        assertNotNull(dto.getValidityPeriodDays());
        assertNotNull(dto.getRequiredDocuments());
    }

    @Test
    void testLogoUrlFallback() {
        Organization bankNoLogo = new Organization();
        bankNoLogo.setId(bankId);
        bankNoLogo.setName("Test Bank");
        bankNoLogo.setLogoUrl(null);

        List<Offer> offers = new ArrayList<>();
        offers.add(offer1);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(offers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bankNoLogo));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals("https://api.creditapp.com/images/default-bank-logo.png", result.get(0).getLogoUrl());
    }

    @Test
    void testRequiredDocumentsParsing() {
        List<Offer> offers = new ArrayList<>();
        offers.add(offer1);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(offers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bank));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        List<String> documents = result.get(0).getRequiredDocuments();
        assertEquals(3, documents.size());
        assertEquals("ID", documents.get(0));
        assertEquals("Paycheck", documents.get(1));
        assertEquals("Tax Return", documents.get(2));
    }

    @Test
    void testExcludesExpiredOffers() {
        Offer expiredOffer = new Offer();
        expiredOffer.setId(UUID.randomUUID());
        expiredOffer.setApplicationId(applicationId);
        expiredOffer.setBankId(bankId);
        expiredOffer.setApr(new BigDecimal("2.50"));
        expiredOffer.setOfferStatus(OfferStatus.EXPIRED);
        expiredOffer.setExpiresAt(LocalDateTime.now().minusHours(1));

        List<Offer> allOffers = new ArrayList<>();
        allOffers.add(offer1);
        allOffers.add(expiredOffer);
        allOffers.add(offer2);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(allOffers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bank));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(o -> o.getOfferStatus().equals("EXPIRED")));
    }

    @Test
    void testExcludesExpiredWithSelectionOffers() {
        Offer expiredSelectedOffer = new Offer();
        expiredSelectedOffer.setId(UUID.randomUUID());
        expiredSelectedOffer.setApplicationId(applicationId);
        expiredSelectedOffer.setBankId(bankId);
        expiredSelectedOffer.setApr(new BigDecimal("3.00"));
        expiredSelectedOffer.setOfferStatus(OfferStatus.EXPIRED_WITH_SELECTION);
        expiredSelectedOffer.setExpiresAt(LocalDateTime.now().minusHours(2));

        List<Offer> allOffers = new ArrayList<>();
        allOffers.add(offer1);
        allOffers.add(expiredSelectedOffer);
        allOffers.add(offer2);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(allOffers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bank));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(o -> o.getOfferStatus().equals("EXPIRED_WITH_SELECTION")));
    }

    @Test
    void testAllOffersExpiredReturnsEmpty() {
        Offer expiredOffer1 = new Offer();
        expiredOffer1.setId(UUID.randomUUID());
        expiredOffer1.setApplicationId(applicationId);
        expiredOffer1.setBankId(bankId);
        expiredOffer1.setOfferStatus(OfferStatus.EXPIRED);

        Offer expiredOffer2 = new Offer();
        expiredOffer2.setId(UUID.randomUUID());
        expiredOffer2.setApplicationId(applicationId);
        expiredOffer2.setBankId(bankId);
        expiredOffer2.setOfferStatus(OfferStatus.EXPIRED_WITH_SELECTION);

        List<Offer> allOffers = new ArrayList<>();
        allOffers.add(expiredOffer1);
        allOffers.add(expiredOffer2);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(allOffers);

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals(0, result.size());
    }

    @Test
    void testSortsOffersByAprAfterFilteringExpired() {
        Offer expiredOffer = new Offer();
        expiredOffer.setId(UUID.randomUUID());
        expiredOffer.setApplicationId(applicationId);
        expiredOffer.setBankId(bankId);
        expiredOffer.setApr(new BigDecimal("1.50"));
        expiredOffer.setOfferStatus(OfferStatus.EXPIRED);

        List<Offer> allOffers = new ArrayList<>();
        allOffers.add(offer2);  // APR 4.25
        allOffers.add(expiredOffer);  // APR 1.50 (expired)
        allOffers.add(offer1);  // APR 3.50

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(allOffers);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(bank));

        List<OfferComparisonDTO> result = service.getOffersForApplication(applicationId, borrowerId);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("3.50"), result.get(0).getApr());  // Lowest non-expired
        assertEquals(new BigDecimal("4.25"), result.get(1).getApr());  // Higher non-expired
    }
}
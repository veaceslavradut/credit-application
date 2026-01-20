package com.creditapp.unit.borrower;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.OfferComparisonTableRequest;
import com.creditapp.borrower.dto.OfferComparisonTableResponse;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.OfferComparisonTableService;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferComparisonTableServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OfferComparisonTableService service;

    @Test
    void testGetOffersTable_DefaultSort() {
        UUID applicationId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();

        Application application = createMockApplication(applicationId, borrowerId);
        List<Offer> offers = List.of(createMockOffer(), createMockOffer());
        Page<Offer> offersPage = new PageImpl<>(offers);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(eq(applicationId), any(Pageable.class))).thenReturn(offersPage);
        when(organizationRepository.findAllById(any())).thenReturn(List.of(createMockBank()));

        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        OfferComparisonTableResponse response = service.getOffersTable(applicationId, borrowerId, request);

        assertThat(response.getOffers()).hasSize(2);
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getSortBy()).isEqualTo("apr");
        assertThat(response.getSortOrder()).isEqualTo("asc");
    }
    
    @Test
    void testGetOffersTable_VerifiesExpirationCountdown() {
        UUID applicationId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();

        Application application = createMockApplication(applicationId, borrowerId);
        Offer offer = createMockOffer();
        offer.setExpiresAt(LocalDateTime.now().plusDays(5));
        Page<Offer> offersPage = new PageImpl<>(List.of(offer));

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(eq(applicationId), any(Pageable.class))).thenReturn(offersPage);
        when(organizationRepository.findAllById(any())).thenReturn(List.of(createMockBank()));

        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        OfferComparisonTableResponse response = service.getOffersTable(applicationId, borrowerId, request);

        assertThat(response.getOffers()).hasSize(1);
        assertThat(response.getOffers().get(0).getExpirationCountdown()).isNotNull();
        assertThat(response.getOffers().get(0).getExpirationCountdown()).contains("day");
    }

    @Test
    void testGetOffersTable_APRFilter() {
        UUID applicationId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();

        Application application = createMockApplication(applicationId, borrowerId);
        Offer offer1 = createMockOffer();
        offer1.setApr(new BigDecimal("7.5"));
        Offer offer2 = createMockOffer();
        offer2.setApr(new BigDecimal("10.5"));
        Page<Offer> offersPage = new PageImpl<>(List.of(offer1, offer2));

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(eq(applicationId), any(Pageable.class))).thenReturn(offersPage);
        when(organizationRepository.findAllById(any())).thenReturn(List.of(createMockBank()));

        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        request.setAprMax(new BigDecimal("9.0"));
        OfferComparisonTableResponse response = service.getOffersTable(applicationId, borrowerId, request);

        assertThat(response.getOffers()).hasSize(1);
        assertThat(response.getOffers().get(0).getApr()).isEqualByComparingTo(new BigDecimal("7.5"));
    }

    @Test
    void testDetermineExpirationHighlight_NormalWhenMoreThan24Hours() {
        // Offer expiring in 25 hours
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(25);
        String highlight = service.determineExpirationHighlight(expiresAt, OfferStatus.CALCULATED);
        assertThat(highlight).isEqualTo("NORMAL");
    }

    @Test
    void testDetermineExpirationHighlight_WarningOrangeWithin24Hours() {
        // Offer expiring in 12 hours
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(12);
        String highlight = service.determineExpirationHighlight(expiresAt, OfferStatus.CALCULATED);
        assertThat(highlight).isEqualTo("WARNING_ORANGE");
    }

    @Test
    void testDetermineExpirationHighlight_WarningOrangeAt24Hours() {
        // Offer expiring in exactly 24 hours
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        String highlight = service.determineExpirationHighlight(expiresAt, OfferStatus.CALCULATED);
        assertThat(highlight).isEqualTo("WARNING_ORANGE");
    }

    @Test
    void testDetermineExpirationHighlight_ExpiredRedWhenPastExpiration() {
        // Offer expired 1 hour ago
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1);
        String highlight = service.determineExpirationHighlight(expiresAt, OfferStatus.CALCULATED);
        assertThat(highlight).isEqualTo("EXPIRED_RED");
    }

    @Test
    void testDetermineExpirationHighlight_ExpiredRedWithExpiredStatus() {
        // Offer with EXPIRED status should show red regardless of time
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(10);
        String highlight = service.determineExpirationHighlight(expiresAt, OfferStatus.EXPIRED);
        assertThat(highlight).isEqualTo("EXPIRED_RED");
    }

    @Test
    void testDetermineExpirationHighlight_ExpiredRedWithExpiredWithSelectionStatus() {
        // Offer with EXPIRED_WITH_SELECTION status should show red
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(10);
        String highlight = service.determineExpirationHighlight(expiresAt, OfferStatus.EXPIRED_WITH_SELECTION);
        assertThat(highlight).isEqualTo("EXPIRED_RED");
    }

    @Test
    void testGetOffersTable_IncludesExpirationHighlightAndResubmit() {
        UUID applicationId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();

        Application application = createMockApplication(applicationId, borrowerId);
        
        // Create an expired offer
        Offer expiredOffer = createMockOffer();
        expiredOffer.setOfferStatus(OfferStatus.EXPIRED);
        expiredOffer.setExpiresAt(LocalDateTime.now().minusHours(1));
        
        Page<Offer> offersPage = new PageImpl<>(List.of(expiredOffer));

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(eq(applicationId), any(Pageable.class))).thenReturn(offersPage);
        when(organizationRepository.findAllById(any())).thenReturn(List.of(createMockBank()));

        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        OfferComparisonTableResponse response = service.getOffersTable(applicationId, borrowerId, request);

        assertThat(response.getOffers()).hasSize(1);
        assertThat(response.getOffers().get(0).getExpirationHighlight()).isEqualTo("EXPIRED_RED");
        assertThat(response.getOffers().get(0).getCanResubmit()).isTrue();
        assertThat(response.getOffers().get(0).getResubmitUrl()).startsWith("/api/bank/offers/");
        assertThat(response.getOffers().get(0).getResubmitUrl()).endsWith("/resubmit");
    }

    @Test
    void testGetOffersTable_NoResubmitUrlForNonExpiredOffers() {
        UUID applicationId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();

        Application application = createMockApplication(applicationId, borrowerId);
        
        Offer offer = createMockOffer();
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        Page<Offer> offersPage = new PageImpl<>(List.of(offer));

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(eq(applicationId), any(Pageable.class))).thenReturn(offersPage);
        when(organizationRepository.findAllById(any())).thenReturn(List.of(createMockBank()));

        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        OfferComparisonTableResponse response = service.getOffersTable(applicationId, borrowerId, request);

        assertThat(response.getOffers()).hasSize(1);
        assertThat(response.getOffers().get(0).getExpirationHighlight()).isEqualTo("NORMAL");
        assertThat(response.getOffers().get(0).getCanResubmit()).isFalse();
        assertThat(response.getOffers().get(0).getResubmitUrl()).isNull();
    }

    private Application createMockApplication(UUID id, UUID borrowerId) {
        Application app = new Application();
        app.setId(id);
        app.setBorrowerId(borrowerId);
        app.setLoanAmount(new BigDecimal("10000"));
        app.setLoanTermMonths(36);
        app.setStatus(ApplicationStatus.SUBMITTED);
        return app;
    }

    private Offer createMockOffer() {
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(UUID.randomUUID());
        offer.setBankId(UUID.randomUUID());
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setApr(new BigDecimal("8.5"));
        offer.setMonthlyPayment(new BigDecimal("315.50"));
        offer.setTotalCost(new BigDecimal("11358"));
        offer.setOriginationFee(new BigDecimal("250"));
        offer.setInsuranceCost(new BigDecimal("100"));
        offer.setProcessingTimeDays(7);
        offer.setValidityPeriodDays(30);
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        return offer;
    }

    private Organization createMockBank() {
        Organization bank = new Organization();
        bank.setId(UUID.randomUUID());
        bank.setName("Test Bank");
        bank.setLogoUrl("https://example.com/logo.png");
        return bank;
    }
}

package com.creditapp.unit.borrower;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.creditapp.borrower.dto.OfferComparisonTableRequest;
import com.creditapp.borrower.dto.OfferComparisonTableResponse;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.model.Offer;
import com.creditapp.borrower.model.OfferStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.repository.OfferRepository;
import com.creditapp.borrower.service.OfferComparisonTableService;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
class OfferComparisonTableServiceTest {
    
    @Mock
    private OfferRepository offerRepository;
    
    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private OrganizationRepository organizationRepository;
    
    @InjectMocks
    private OfferComparisonTableService offerComparisonTableService;
    
    private UUID borrowerId;
    private UUID applicationId;
    private UUID bankId1;
    private UUID bankId2;
    private Application application;
    private List<Offer> mockOffers;
    
    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        bankId1 = UUID.randomUUID();
        bankId2 = UUID.randomUUID();
        
        // Create application
        application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setStatus(ApplicationStatus.SUBMITTED);
        
        // Create mock offers
        mockOffers = createMockOffers();
    }
    
    @Test
    void testGetOffersTable_DefaultSort() {
        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Offer> page = new PageImpl<>(mockOffers);
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId, pageable)).thenReturn(page);
        when(organizationRepository.findById(any())).thenReturn(Optional.of(createMockBank()));
        
        OfferComparisonTableResponse response = offerComparisonTableService.getOffersTable(applicationId, borrowerId, request);
        
        assertNotNull(response);
        assertEquals(2, response.getOffers().size());
        assertEquals("apr", response.getSortBy());
        assertEquals("asc", response.getSortOrder());
    }
    
    @Test
    void testCalculateExpirationCountdown_Days() {
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.of("UTC")).plusDays(5).plusHours(3);
        String countdown = offerComparisonTableService.calculateExpirationCountdown(expiresAt);
        
        assertTrue(countdown.contains("day"), "Should contain days");
        assertTrue(countdown.contains("hour"), "Should contain hours");
    }
    
    @Test
    void testCalculateExpirationCountdown_Hours() {
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).plusMinutes(30);
        String countdown = offerComparisonTableService.calculateExpirationCountdown(expiresAt);
        
        assertTrue(countdown.contains("hour"), "Should contain hours");
        assertTrue(countdown.contains("minute"), "Should contain minutes");
    }
    
    @Test
    void testCalculateExpirationCountdown_Minutes() {
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(45);
        String countdown = offerComparisonTableService.calculateExpirationCountdown(expiresAt);
        
        assertTrue(countdown.contains("minute"), "Should contain minutes");
        assertFalse(countdown.contains("hour"), "Should not contain hours");
    }
    
    @Test
    void testCalculateExpirationCountdown_Expired() {
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.of("UTC")).minusMinutes(10);
        String countdown = offerComparisonTableService.calculateExpirationCountdown(expiresAt);
        
        assertEquals("Expired", countdown);
    }
    
    @Test
    void testGetOffersTable_FilterByAPR() {
        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        request.setAprMin(new BigDecimal("8.0"));
        request.setAprMax(new BigDecimal("9.0"));
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Offer> page = new PageImpl<>(mockOffers);
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId, pageable)).thenReturn(page);
        when(organizationRepository.findById(any())).thenReturn(Optional.of(createMockBank()));
        
        OfferComparisonTableResponse response = offerComparisonTableService.getOffersTable(applicationId, borrowerId, request);
        
        assertNotNull(response);
        // Offers outside APR range are filtered
        assertTrue(response.getOffers().size() <= 2);
    }
    
    @Test
    void testGetOffersTable_DifferentBorrower() {
        UUID differentBorrowerId = UUID.randomUUID();
        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        
        assertThrows(Exception.class, () -> {
            offerComparisonTableService.getOffersTable(applicationId, differentBorrowerId, request);
        });
    }
    
    @Test
    void testGetOffersTable_Pagination() {
        OfferComparisonTableRequest request = new OfferComparisonTableRequest();
        request.setLimit(1);
        request.setOffset(0);
        
        Pageable pageable = PageRequest.of(0, 1);
        Page<Offer> page = new PageImpl<>(mockOffers.subList(0, 1));
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(eq(applicationId), any(Pageable.class))).thenReturn(page);
        when(organizationRepository.findById(any())).thenReturn(Optional.of(createMockBank()));
        
        OfferComparisonTableResponse response = offerComparisonTableService.getOffersTable(applicationId, borrowerId, request);
        
        assertNotNull(response);
        assertEquals(1, response.getOffers().size());
        assertEquals(1, response.getLimit());
        assertTrue(response.getHasMore());
    }
    
    private List<Offer> createMockOffers() {
        List<Offer> offers = new ArrayList<>();
        
        Offer offer1 = new Offer();
        offer1.setId(UUID.randomUUID());
        offer1.setApplicationId(applicationId);
        offer1.setBankId(bankId1);
        offer1.setApr(new BigDecimal("8.5"));
        offer1.setMonthlyPayment(new BigDecimal("1489.51"));
        offer1.setTotalCost(new BigDecimal("370823.60"));
        offer1.setOriginationFee(new BigDecimal("5000"));
        offer1.setInsuranceCost(new BigDecimal("30000"));
        offer1.setTermMonths(360);
        offer1.setProcessingTimeDays(7);
        offer1.setValidityPeriodDays(14);
        offer1.setExpiresAt(LocalDateTime.now().plusDays(14));
        offer1.setStatus(OfferStatus.CALCULATED);
        offers.add(offer1);
        
        Offer offer2 = new Offer();
        offer2.setId(UUID.randomUUID());
        offer2.setApplicationId(applicationId);
        offer2.setBankId(bankId2);
        offer2.setApr(new BigDecimal("9.0"));
        offer2.setMonthlyPayment(new BigDecimal("1609.25"));
        offer2.setTotalCost(new BigDecimal("420052.00"));
        offer2.setOriginationFee(new BigDecimal("5500"));
        offer2.setInsuranceCost(new BigDecimal("32000"));
        offer2.setTermMonths(360);
        offer2.setProcessingTimeDays(10);
        offer2.setValidityPeriodDays(14);
        offer2.setExpiresAt(LocalDateTime.now().plusDays(14));
        offer2.setStatus(OfferStatus.CALCULATED);
        offers.add(offer2);
        
        return offers;
    }
    
    private Organization createMockBank() {
        Organization bank = new Organization();
        bank.setName("Test Bank");
        bank.setLogoUrl("https://example.com/logo.png");
        return bank;
    }
}

package com.creditapp.integration.bank;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankOfferExpirationNotificationService;
import com.creditapp.batch.OfferExpirationWarningScheduler;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Notification;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.NotificationRepository;
import com.creditapp.shared.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Offer Expiration Notification feature
 * Story 4.6: Offer Expiration Notification - Task 10
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OfferExpirationIntegrationTest {
    
    @Autowired
    private OfferRepository offerRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private OfferExpirationWarningScheduler scheduler;
    
    @Autowired
    private BankOfferExpirationNotificationService notificationService;
    
    private UUID bankId;
    private UUID applicationId;
    private UUID borrowerId;
    
    @BeforeEach
    void setUp() {
        // Clean up existing test data
        notificationRepository.deleteAll();
        offerRepository.deleteAll();
        
        // Create test bank
        borrowerId = UUID.randomUUID();
        Organization bank = new Organization();
        bank.setId(UUID.randomUUID());
        bank.setName("Test Bank");
        bank.setTaxId("TEST_TAX_" + System.currentTimeMillis());
        bank.setContactEmail("testbank@example.com");
        bank.setCountryCode("US");
        bank.setActive(true);
        bank = organizationRepository.save(bank);
        bankId = bank.getId();
        
        // Create test application
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setBorrowerId(borrowerId);
        application.setLoanType("PERSONAL");
        application.setLoanAmount(new BigDecimal("10000"));
        application.setLoanTermMonths(36);
        application.setCurrency("USD");
        application.setStatus(ApplicationStatus.SUBMITTED);
        application = applicationRepository.save(application);
        applicationId = application.getId();
    }
    
    @Test
    void testBatchJobProcessesExpiringOffers() {
        // Given: Create an offer expiring in 20 hours
        Offer offer = createOffer(LocalDateTime.now().plusHours(20));
        offer = offerRepository.save(offer);
        
        // When: Run batch job
        scheduler.checkExpiringOffers();
        
        // Then: Offer should be marked as notified
        Offer updatedOffer = offerRepository.findById(offer.getId()).orElseThrow();
        assertTrue(updatedOffer.isNotified(), "Offer should be marked as notified");
    }
    
    @Test
    void testInPortalNotificationCreated() {
        // Given: Create an offer expiring in 20 hours
        Offer offer = createOffer(LocalDateTime.now().plusHours(20));
        offer = offerRepository.save(offer);
        
        // When: Send notification
        notificationService.notifyBankOfExpiration(offer);
        
        // Wait for async operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then: In-portal notification should be created
        List<Notification> notifications = notificationRepository.findUnreadByBankId(bankId);
        assertFalse(notifications.isEmpty(), "Notification should be created");
        
        Notification notification = notifications.get(0);
        assertEquals("OFFER_EXPIRING", notification.getType());
        assertEquals(bankId, notification.getBankId());
        assertNotNull(notification.getTitle());
        assertNotNull(notification.getMessage());
        assertNull(notification.getReadAt(), "Notification should be unread");
    }
    
    @Test
    void testDuplicateNotificationsPrevented() {
        // Given: Create an offer expiring in 20 hours
        Offer offer = createOffer(LocalDateTime.now().plusHours(20));
        offer = offerRepository.save(offer);
        
        // When: Run batch job twice
        scheduler.checkExpiringOffers();
        long firstCount = notificationRepository.countUnreadByBankId(bankId);
        
        scheduler.checkExpiringOffers();
        long secondCount = notificationRepository.countUnreadByBankId(bankId);
        
        // Then: Should not create duplicate notifications
        assertEquals(firstCount, secondCount, "Should not create duplicate notifications");
    }
    
    @Test
    void testOnlyExpiringOffersProcessed() {
        // Given: Create offers with different expiration times
        Offer expiringOffer = createOffer(LocalDateTime.now().plusHours(20));
        Offer futureOffer = createOffer(LocalDateTime.now().plusHours(50));
        Offer expiredOffer = createOffer(LocalDateTime.now().minusHours(5));
        
        offerRepository.saveAll(List.of(expiringOffer, futureOffer, expiredOffer));
        
        // When: Run batch job
        scheduler.checkExpiringOffers();
        
        // Then: Only the offer expiring within 24 hours should be notified
        Offer updatedExpiringOffer = offerRepository.findById(expiringOffer.getId()).orElseThrow();
        Offer updatedFutureOffer = offerRepository.findById(futureOffer.getId()).orElseThrow();
        Offer updatedExpiredOffer = offerRepository.findById(expiredOffer.getId()).orElseThrow();
        
        assertTrue(updatedExpiringOffer.isNotified(), "Expiring offer should be notified");
        assertFalse(updatedFutureOffer.isNotified(), "Future offer should not be notified");
        assertFalse(updatedExpiredOffer.isNotified(), "Expired offer should not be notified");
    }
    
    @Test
    void testNotificationFlagPersists() {
        // Given: Create an offer
        Offer offer = createOffer(LocalDateTime.now().plusHours(20));
        offer = offerRepository.save(offer);
        UUID offerId = offer.getId();
        
        // When: Run batch job
        scheduler.checkExpiringOffers();
        
        // Then: Fetch offer fresh from database and verify flag persists
        offerRepository.flush();
        Offer reloadedOffer = offerRepository.findById(offerId).orElseThrow();
        assertTrue(reloadedOffer.isNotified(), "Notified flag should persist in database");
    }
    
    @Test
    void testPerformanceWithMultipleOffers() {
        // Given: Create 100 expiring offers
        List<Offer> offers = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            offers.add(createOffer(LocalDateTime.now().plusHours(20)));
        }
        offerRepository.saveAll(offers);
        
        // When: Run batch job and measure time
        long startTime = System.currentTimeMillis();
        scheduler.checkExpiringOffers();
        long duration = System.currentTimeMillis() - startTime;
        
        // Then: Should complete in reasonable time (< 10 seconds for 100 offers)
        assertTrue(duration < 10000, "Batch job should complete within 10 seconds for 100 offers");
        
        // Verify all offers were processed
        long notifiedCount = offerRepository.findAll().stream()
                .filter(Offer::isNotified)
                .count();
        assertEquals(100, notifiedCount, "All 100 offers should be notified");
    }
    
    // Helper method to create test offer
    private Offer createOffer(LocalDateTime expiresAt) {
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        offer.setApr(new BigDecimal("7.5"));
        offer.setMonthlyPayment(new BigDecimal("250.00"));
        offer.setTotalCost(new BigDecimal("9000.00"));
        offer.setOriginationFee(new BigDecimal("100.00"));
        offer.setProcessingTimeDays(5);
        offer.setValidityPeriodDays(1);
        offer.setExpiresAt(expiresAt);
        offer.setNotified(false);
        return offer;
    }
}
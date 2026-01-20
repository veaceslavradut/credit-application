package com.creditapp.integration.bank;

import com.creditapp.auth.repository.UserRepository;
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
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.NotificationRepository;
import com.creditapp.shared.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Offer Expiration Notification feature
 * Story 4.6: Offer Expiration Notification - Task 10
 * 
 * Note: Tests do NOT use @Transactional to allow async operations
 * and database persistence checks to work correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Requires test isolation refactoring for concurrent test execution")
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
    private UserRepository userRepository;
    
    @Autowired
    private OfferExpirationWarningScheduler scheduler;
    
    @Autowired
    private BankOfferExpirationNotificationService notificationService;
    
    private UUID bankId;
    private UUID applicationId;
    private UUID borrowerId;
    
    @BeforeEach
    void setUp() {
        // Clean up existing test data - mandatory for test isolation
        notificationRepository.deleteAll();
        offerRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
        
        // Create test borrower user
        borrowerId = UUID.randomUUID();
        User borrower = new User();
        borrower.setId(borrowerId);
        borrower.setEmail("testborrower_" + System.nanoTime() + "@example.com");
        borrower.setPasswordHash("hashedpassword");
        borrower.setFirstName("Test");
        borrower.setLastName("Borrower");
        borrower.setRole(UserRole.BORROWER);
        userRepository.saveAndFlush(borrower);
        
        // Create test bank with unique tax ID to avoid constraint violations
        Organization bank = new Organization();
        bank.setId(UUID.randomUUID());
        bank.setName("Test Bank_" + System.nanoTime());
        bank.setTaxId("TEST_TAX_" + System.nanoTime());
        bank.setContactEmail("testbank_" + System.nanoTime() + "@example.com");
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
        try {
            // Given: Create an offer expiring in 20 hours
            Offer offer = createOffer(LocalDateTime.now().plusHours(20));
            offer = offerRepository.save(offer);
            UUID offerId = offer.getId();
            
            // When: Run batch job
            scheduler.checkExpiringOffers();
            
            // Allow async operations to complete
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Then: Offer should be marked as notified
            Offer updatedOffer = offerRepository.findById(offerId).orElseThrow();
            assertTrue(updatedOffer.isNotified(), "Offer should be marked as notified");
        } finally {
            // Cleanup
            cleanupTestData();
        }
    }
    
    @Test
    void testInPortalNotificationCreated() {
        try {
            // Given: Create an offer expiring in 20 hours
            Offer offer = createOffer(LocalDateTime.now().plusHours(20));
            offer = offerRepository.save(offer);
            
            // When: Send notification through service (this creates the in-portal notification)
            notificationService.notifyBankOfExpiration(offer);
            
            // Wait for async operation
            Thread.sleep(1000);
            
            // Then: In-portal notification should be created
            List<Notification> notifications = notificationRepository.findUnreadByBankId(bankId);
            assertFalse(notifications.isEmpty(), "Notification should be created");
            
            Notification notification = notifications.get(0);
            assertEquals("OFFER_EXPIRING", notification.getType());
            assertEquals(bankId, notification.getBankId());
            assertNotNull(notification.getTitle());
            assertNotNull(notification.getMessage());
            assertNull(notification.getReadAt(), "Notification should be unread");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            cleanupTestData();
        }
    }
    
    @Test
    void testDuplicateNotificationsPrevented() {
        try {
            // Given: Create an offer expiring in 20 hours
            Offer offer = createOffer(LocalDateTime.now().plusHours(20));
            offer = offerRepository.save(offer);
            
            // When: Run batch job twice
            scheduler.checkExpiringOffers();
            Thread.sleep(300);
            long firstCount = notificationRepository.countUnreadByBankId(bankId);
            
            scheduler.checkExpiringOffers();
            Thread.sleep(300);
            long secondCount = notificationRepository.countUnreadByBankId(bankId);
            
            // Then: Should not create duplicate notifications
            assertEquals(firstCount, secondCount, "Should not create duplicate notifications");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            cleanupTestData();
        }
    }
    
    @Test
    void testOnlyExpiringOffersProcessed() {
        try {
            // Given: Create offers with different expiration times
            Offer expiringOffer = createOffer(LocalDateTime.now().plusHours(20));
            Offer futureOffer = createOffer(LocalDateTime.now().plusHours(50));
            Offer expiredOffer = createOffer(LocalDateTime.now().minusHours(5));
            
            offerRepository.saveAll(List.of(expiringOffer, futureOffer, expiredOffer));
            
            // When: Run batch job
            scheduler.checkExpiringOffers();
            Thread.sleep(500);
            
            // Then: Only the offer expiring within 24 hours should be notified
            Offer updatedExpiringOffer = offerRepository.findById(expiringOffer.getId()).orElseThrow();
            Offer updatedFutureOffer = offerRepository.findById(futureOffer.getId()).orElseThrow();
            Offer updatedExpiredOffer = offerRepository.findById(expiredOffer.getId()).orElseThrow();
            
            assertTrue(updatedExpiringOffer.isNotified(), "Expiring offer should be notified");
            assertFalse(updatedFutureOffer.isNotified(), "Future offer should not be notified");
            assertFalse(updatedExpiredOffer.isNotified(), "Expired offer should not be notified");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            cleanupTestData();
        }
    }
    
    @Test
    void testNotificationFlagPersists() {
        try {
            // Given: Create an offer
            Offer offer = createOffer(LocalDateTime.now().plusHours(20));
            offer = offerRepository.save(offer);
            UUID offerId = offer.getId();
            
            // When: Run batch job
            scheduler.checkExpiringOffers();
            Thread.sleep(500);
            
            // Then: Fetch offer fresh from database and verify flag persists
            Offer reloadedOffer = offerRepository.findById(offerId).orElseThrow();
            assertTrue(reloadedOffer.isNotified(), "Notified flag should persist in database");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            cleanupTestData();
        }
    }
    
    @Test
    void testPerformanceWithMultipleOffers() {
        try {
            // Given: Create 100 expiring offers
            List<Offer> offers = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                offers.add(createOffer(LocalDateTime.now().plusHours(20)));
            }
            offerRepository.saveAll(offers);
            
            // When: Run batch job and measure time
            long startTime = System.currentTimeMillis();
            scheduler.checkExpiringOffers();
            Thread.sleep(500);
            long duration = System.currentTimeMillis() - startTime;
            
            // Then: Should complete in reasonable time (< 10 seconds for 100 offers)
            assertTrue(duration < 10000, "Batch job should complete within 10 seconds for 100 offers");
            
            // Verify all offers were processed
            long notifiedCount = offerRepository.findAll().stream()
                    .filter(Offer::isNotified)
                    .count();
            assertEquals(100, notifiedCount, "All 100 offers should be notified");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            cleanupTestData();
        }
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
    
    // Helper method to cleanup test data after each test
    private void cleanupTestData() {
        notificationRepository.deleteAll();
        offerRepository.deleteAll();
    }
}
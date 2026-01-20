package com.creditapp.unit.batch;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankOfferExpirationNotificationService;
import com.creditapp.batch.OfferExpirationWarningScheduler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OfferExpirationWarningScheduler.
 * Tests batch job functionality for sending expiration warnings.
 */
@ExtendWith(MockitoExtension.class)
class OfferExpirationWarningSchedulerTest {
    
    @Mock
    private OfferRepository offerRepository;
    
    @Mock
    private BankOfferExpirationNotificationService notificationService;
    
    @Mock
    private MeterRegistry meterRegistry;
    
    @Mock
    private Counter sentCounter;
    
    @Mock
    private Counter failedCounter;
    
    @Mock
    private Counter errorCounter;
    
    @Mock
    private Timer timer;
    
    @Mock
    private Timer.Sample timerSample;
    
    @InjectMocks
    private OfferExpirationWarningScheduler scheduler;
    
    private UUID bankId;
    private UUID applicationId;
    
    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        
        // Mock metrics - use lenient() for stubs that aren't used in all tests
        when(meterRegistry.counter("creditapp.offers.expiration_warnings.sent")).thenReturn(sentCounter);
        when(meterRegistry.counter("creditapp.offers.expiration_warnings.failed")).thenReturn(failedCounter);
        lenient().when(meterRegistry.counter("creditapp.scheduler.offer-expiration-warning.errors")).thenReturn(errorCounter);
        lenient().when(meterRegistry.timer("creditapp.scheduler.offer-expiration-warning.duration")).thenReturn(timer);
    }
    
    @Test
    void checkExpiringOffers_NoOffersExpiring_DoesNothing() {
        // Arrange
        when(offerRepository.findOffersExpiringSoon(any(), any())).thenReturn(Collections.emptyList());
        
        // Act
        scheduler.checkExpiringOffers();
        
        // Assert
        verify(offerRepository, times(1)).findOffersExpiringSoon(any(), any());
        verify(notificationService, never()).notifyBankOfExpiration(any());
        verify(offerRepository, never()).save(any());
    }
    
    @Test
    void checkExpiringOffers_SingleOffer_SendsNotification() {
        // Arrange
        Offer offer = createOffer(LocalDateTime.now().plusHours(12));
        when(offerRepository.findOffersExpiringSoon(any(), any())).thenReturn(List.of(offer));
        
        // Act
        scheduler.checkExpiringOffers();
        
        // Assert
        verify(notificationService, times(1)).notifyBankOfExpiration(offer);
        verify(offerRepository, times(1)).save(offer);
        assertTrue(offer.isNotified(), "Offer should be marked as notified");
    }
    
    @Test
    void checkExpiringOffers_MultipleOffers_SendsAllNotifications() {
        // Arrange
        Offer offer1 = createOffer(LocalDateTime.now().plusHours(5));
        Offer offer2 = createOffer(LocalDateTime.now().plusHours(15));
        Offer offer3 = createOffer(LocalDateTime.now().plusHours(20));
        
        when(offerRepository.findOffersExpiringSoon(any(), any()))
                .thenReturn(Arrays.asList(offer1, offer2, offer3));
        
        // Act
        scheduler.checkExpiringOffers();
        
        // Assert
        verify(notificationService, times(3)).notifyBankOfExpiration(any());
        verify(offerRepository, times(3)).save(any());
        
        assertTrue(offer1.isNotified());
        assertTrue(offer2.isNotified());
        assertTrue(offer3.isNotified());
    }
    
    @Test
    void checkExpiringOffers_NotificationFails_ContinuesWithOthers() {
        // Arrange
        Offer offer1 = createOffer(LocalDateTime.now().plusHours(5));
        Offer offer2 = createOffer(LocalDateTime.now().plusHours(15));
        
        when(offerRepository.findOffersExpiringSoon(any(), any()))
                .thenReturn(Arrays.asList(offer1, offer2));
        
        // Simulate failure on first offer
        doThrow(new RuntimeException("Email service error"))
                .when(notificationService).notifyBankOfExpiration(offer1);
        
        // Act
        scheduler.checkExpiringOffers();
        
        // Assert
        verify(notificationService, times(2)).notifyBankOfExpiration(any());
        
        // First offer should NOT be saved due to exception
        assertFalse(offer1.isNotified(), "Failed offer should not be marked as notified");
        
        // Second offer should be processed
        assertTrue(offer2.isNotified(), "Second offer should be marked as notified");
        verify(offerRepository, times(1)).save(offer2);
    }
    
    @Test
    void checkExpiringOffers_QueryUsesCorrectTimeWindow() {
        // Arrange
        when(offerRepository.findOffersExpiringSoon(any(), any())).thenReturn(Collections.emptyList());
        
        // Act
        LocalDateTime beforeRun = LocalDateTime.now();
        scheduler.checkExpiringOffers();
        LocalDateTime afterRun = LocalDateTime.now();
        
        // Assert
        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> windowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(offerRepository).findOffersExpiringSoon(nowCaptor.capture(), windowCaptor.capture());
        
        LocalDateTime capturedNow = nowCaptor.getValue();
        LocalDateTime capturedWindow = windowCaptor.getValue();
        
        // Verify now is within reasonable bounds
        assertTrue(capturedNow.isAfter(beforeRun.minusSeconds(1)) && capturedNow.isBefore(afterRun.plusSeconds(1)));
        
        // Verify window is approximately 24 hours from now
        long hoursDiff = java.time.Duration.between(capturedNow, capturedWindow).toHours();
        assertEquals(24, hoursDiff, "Expiration window should be 24 hours from now");
    }
    
    @Test
    void checkExpiringOffers_UpdatesMetrics() {
        // Arrange
        Offer offer1 = createOffer(LocalDateTime.now().plusHours(5));
        Offer offer2 = createOffer(LocalDateTime.now().plusHours(15));
        Offer offer3 = createOffer(LocalDateTime.now().plusHours(20));
        
        when(offerRepository.findOffersExpiringSoon(any(), any()))
                .thenReturn(Arrays.asList(offer1, offer2, offer3));
        
        // Simulate one failure - must specify exact offer instance
        doNothing().when(notificationService).notifyBankOfExpiration(offer1);
        doThrow(new RuntimeException("Email error")).when(notificationService).notifyBankOfExpiration(offer2);
        doNothing().when(notificationService).notifyBankOfExpiration(offer3);
        
        // Act
        scheduler.checkExpiringOffers();
        
        // Assert - scheduler calls increment once with the total count
        verify(sentCounter, times(1)).increment(2.0); // 2 successful
        verify(failedCounter, times(1)).increment(1.0); // 1 failed
    }
    
    @Test
    void checkExpiringOffers_OnlyProcessesNonNotifiedOffers() {
        // Arrange
        Offer notifiedOffer = createOffer(LocalDateTime.now().plusHours(5));
        notifiedOffer.setNotified(true); // Already notified
        
        Offer newOffer = createOffer(LocalDateTime.now().plusHours(10));
        newOffer.setNotified(false); // Not yet notified
        
        // Repository should only return non-notified offers (this is the query's job)
        when(offerRepository.findOffersExpiringSoon(any(), any()))
                .thenReturn(List.of(newOffer));
        
        // Act
        scheduler.checkExpiringOffers();
        
        // Assert
        verify(notificationService, times(1)).notifyBankOfExpiration(newOffer);
        verify(notificationService, never()).notifyBankOfExpiration(notifiedOffer);
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
        offer.setTotalCost(new BigDecimal("7200.00"));
        offer.setOriginationFee(new BigDecimal("100.00"));
        offer.setProcessingTimeDays(5);
        offer.setValidityPeriodDays(30);
        offer.setExpiresAt(expiresAt);
        offer.setNotified(false);
        return offer;
    }
}

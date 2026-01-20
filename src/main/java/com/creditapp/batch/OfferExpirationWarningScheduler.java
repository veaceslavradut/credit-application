package com.creditapp.batch;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankOfferExpirationNotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled batch job to check for offers expiring within 24 hours and send notifications.
 * Part of Story 4.6 - Offer Expiration Notification.
 * Runs every hour.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OfferExpirationWarningScheduler {
    
    private final OfferRepository offerRepository;
    private final BankOfferExpirationNotificationService notificationService;
    private final MeterRegistry meterRegistry;
    
    /**
     * Run every 1 hour to check for offers expiring within 24 hours.
     * fixedRate = 3600000ms = 1 hour
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkExpiringOffers() {
        log.info("Starting offer expiration warning batch job at {}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expirationWindow = now.plusHours(24);
            
            // Query offers expiring within 24 hours that haven't been notified
            List<Offer> expiringOffers = offerRepository.findOffersExpiringSoon(now, expirationWindow);
            
            log.info("Found {} offers expiring within 24 hours", expiringOffers.size());
            
            int notifiedCount = 0;
            int failedCount = 0;
            
            for (Offer offer : expiringOffers) {
                try {
                    // Send notification (async)
                    notificationService.notifyBankOfExpiration(offer);
                    
                    // Mark as notified
                    offer.setNotified(true);
                    offerRepository.save(offer);
                    
                    notifiedCount++;
                    log.debug("Notified bank for offer {}. Expires at: {}", offer.getId(), offer.getExpiresAt());
                } catch (Exception e) {
                    failedCount++;
                    log.error("Failed to notify bank for offer {}: {}", offer.getId(), e.getMessage(), e);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Metrics
            meterRegistry.counter("creditapp.offers.expiration_warnings.sent").increment(notifiedCount);
            meterRegistry.counter("creditapp.offers.expiration_warnings.failed").increment(failedCount);
            meterRegistry.timer("creditapp.scheduler.offer-expiration-warning.duration")
                    .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            log.info("Offer expiration warning batch job completed. Notified: {}, Failed: {}, Duration: {}ms",
                    notifiedCount, failedCount, duration);
                    
        } catch (Exception e) {
            log.error("Offer expiration warning batch job failed: {}", e.getMessage(), e);
            meterRegistry.counter("creditapp.scheduler.offer-expiration-warning.errors").increment();
        }
    }
}

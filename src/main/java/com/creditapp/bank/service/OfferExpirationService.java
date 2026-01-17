package com.creditapp.bank.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.service.AuditService;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OfferExpirationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OfferExpirationService.class);
    
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditService auditService;
    private final MeterRegistry meterRegistry;
    
    public OfferExpirationService(OfferRepository offerRepository,
                                  ApplicationRepository applicationRepository,
                                  AuditService auditService,
                                  MeterRegistry meterRegistry) {
        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
        this.auditService = auditService;
        this.meterRegistry = meterRegistry;
    }
    
    @Transactional
    public int expireOffers() {
        logger.info("Starting offer expiration batch job");
        LocalDateTime now = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        
        List<Offer> expiredOffers = offerRepository.findByExpiresAtBefore(now);
        
        int expiredCount = 0;
        for (Offer offer : expiredOffers) {
            try {
                if (canExpire(offer.getOfferStatus())) {
                    expireOffer(offer);
                    expiredCount++;
                }
            } catch (Exception e) {
                logger.error("Failed to expire offer {}: {}", offer.getId(), e.getMessage(), e);
            }
        }
        
        // Metrics
        long duration = System.currentTimeMillis() - startTime;
        meterRegistry.counter("creditapp.offers.expired.count").increment(expiredCount);
        meterRegistry.timer("creditapp.scheduler.offer-expiration.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        logger.info("Offer expiration batch job completed. {} offers expired in {} ms", expiredCount, duration);
        return expiredCount;
    }
    
    @Transactional
    public void manualExpireOffer(UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
        
        if (!canExpire(offer.getOfferStatus())) {
            throw new IllegalStateException("Offer cannot be expired from status: " + offer.getOfferStatus());
        }
        
        expireOffer(offer);
        meterRegistry.counter("creditapp.offers.expired.manual").increment();
    }
    
    private void expireOffer(Offer offer) {
        OfferStatus originalStatus = offer.getOfferStatus();
        
        if (originalStatus == OfferStatus.ACCEPTED || originalStatus == OfferStatus.SUBMITTED) {
            offer.setOfferStatus(OfferStatus.EXPIRED_WITH_SELECTION);
            logger.info("Expiring selected offer {} from status {}", offer.getId(), originalStatus);
            
            if (originalStatus == OfferStatus.ACCEPTED) {
                revertApplicationStatus(offer.getApplicationId());
            }
        } else {
            offer.setOfferStatus(OfferStatus.EXPIRED);
            logger.info("Expiring offer {} from status {}", offer.getId(), originalStatus);
        }
        
        offerRepository.save(offer);
    }
    
    private void revertApplicationStatus(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElse(null);
        
        if (application != null && application.getStatus() == ApplicationStatus.ACCEPTED) {
            logger.info("Reverting application {} status from ACCEPTED to SUBMITTED due to offer expiration",
                    applicationId);
            
            application.setStatus(ApplicationStatus.SUBMITTED);
            applicationRepository.save(application);
            
            auditService.logAction(
                    "Application",
                    applicationId,
                    com.creditapp.shared.model.AuditAction.APPLICATION_STATUS_CHANGED
            );
        }
    }
    
    private boolean canExpire(OfferStatus status) {
        return status == OfferStatus.CALCULATED || 
               status == OfferStatus.ACCEPTED || 
               status == OfferStatus.SUBMITTED;
    }
}

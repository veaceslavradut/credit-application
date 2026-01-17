package com.creditapp.bank.scheduler;

import com.creditapp.bank.service.OfferExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OfferExpirationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(OfferExpirationScheduler.class);
    
    private final OfferExpirationService offerExpirationService;
    
    public OfferExpirationScheduler(OfferExpirationService offerExpirationService) {
        this.offerExpirationService = offerExpirationService;
    }
    
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void expireOffersDaily() {
        logger.info("Starting scheduled offer expiration job at {}", java.time.LocalDateTime.now());
        
        try {
            int expiredCount = offerExpirationService.expireOffers();
            logger.info("Scheduled offer expiration completed successfully. {} offers expired", expiredCount);
        } catch (Exception e) {
            logger.error("Scheduled offer expiration failed: {}", e.getMessage(), e);
        }
    }
}
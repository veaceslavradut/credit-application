package com.creditapp.bank.service;

import com.creditapp.bank.dto.BankDashboardMetrics;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankDashboardService {

    private final OfferRepository offerRepository;

    public BankDashboardMetrics getDashboardMetrics(UUID bankId, String timePeriod) {
        long startTime = System.currentTimeMillis();
        log.debug("[DASHBOARD] Calculating metrics for bank {} with period {}", bankId, timePeriod);

        LocalDateTime filterStart = calculateFilterStart(timePeriod);
        LocalDateTime now = LocalDateTime.now();

        // Get all offers for this bank within the time period
        List<Offer> allOffers = offerRepository.findByBankIdAndCreatedAtAfter(bankId, filterStart);
        
        // Count today's applications (distinct by applicationId)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = allOffers.stream()
            .filter(o -> o.getCreatedAt().isAfter(todayStart))
            .map(Offer::getApplicationId)
            .distinct()
            .count();

        // Count all applications (distinct by applicationId)
        long allCount = allOffers.stream()
            .map(Offer::getApplicationId)
            .distinct()
            .count();

        // Count submitted offers
        long offersSubmitted = allOffers.stream()
            .filter(o -> o.getOfferStatus() == OfferStatus.SUBMITTED || 
                         o.getOfferStatus() == OfferStatus.ACCEPTED ||
                         o.getOfferStatus() == OfferStatus.REJECTED ||
                         o.getOfferStatus() == OfferStatus.EXPIRED ||
                         o.getOfferStatus() == OfferStatus.EXPIRED_WITH_SELECTION)
            .count();

        // Count accepted offers
        long offersAccepted = allOffers.stream()
            .filter(o -> o.getOfferStatus() == OfferStatus.ACCEPTED ||
                         o.getOfferStatus() == OfferStatus.EXPIRED_WITH_SELECTION)
            .count();

        // Calculate conversion rate
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (offersSubmitted > 0) {
            conversionRate = BigDecimal.valueOf(offersAccepted)
                .divide(BigDecimal.valueOf(offersSubmitted), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // Calculate average time to offer (from application created to offer submitted)
        int averageTimeToOfferDays = calculateAverageTimeToOffer(allOffers);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("[DASHBOARD] Metrics calculated in {}ms for bank {}", duration, bankId);

        return new BankDashboardMetrics(
            (int) todayCount,
            (int) allCount,
            (int) offersSubmitted,
            (int) offersAccepted,
            conversionRate,
            averageTimeToOfferDays,
            now
        );
    }

    private LocalDateTime calculateFilterStart(String timePeriod) {
        return switch (timePeriod.toUpperCase()) {
            case "TODAY" -> LocalDate.now().atStartOfDay();
            case "LAST_7_DAYS" -> LocalDateTime.now().minusDays(7);
            case "LAST_30_DAYS" -> LocalDateTime.now().minusDays(30);
            default -> LocalDate.now().atStartOfDay(); // Default to TODAY
        };
    }

    private int calculateAverageTimeToOffer(List<Offer> offers) {
        List<Offer> submittedOffers = offers.stream()
            .filter(o -> o.getOfferSubmittedAt() != null)
            .toList();

        if (submittedOffers.isEmpty()) {
            return 0;
        }

        long totalDays = submittedOffers.stream()
            .mapToLong(o -> ChronoUnit.DAYS.between(o.getCreatedAt(), o.getOfferSubmittedAt()))
            .sum();

        return (int) (totalDays / submittedOffers.size());
    }
}
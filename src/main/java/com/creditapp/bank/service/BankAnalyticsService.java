package com.creditapp.bank.service;

import com.creditapp.bank.dto.*;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAnalyticsService {
    
    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponseDTO getAnalytics(UUID bankId, AnalyticsRequest request) {
        log.info("Generating analytics for bank: {}", bankId);
        
        LocalDate[] dateRange = calculateDateRange(request);
        LocalDate dateFrom = dateRange[0];
        LocalDate dateTo = dateRange[1];
        
        LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo.atTime(LocalTime.MAX);
        
        log.debug("Analytics date range: {} to {}", dateFrom, dateTo);
        
        List<Application> applications = applicationRepository.findByCreatedAtBetween(dateTimeFrom, dateTimeTo);
        List<Offer> offers = offerRepository.findByBankIdAndOfferSubmittedAtBetween(bankId, dateTimeFrom, dateTimeTo);
        
        AnalyticsMetricsDTO metrics = calculateMetrics(applications, offers, bankId);
        
        Map<String, LoanTypeBreakdownDTO> loanTypeBreakdown = getAcceptanceByLoanType(bankId, dateFrom, dateTo);
        Map<String, AmountRangeBreakdownDTO> amountRangeBreakdown = getAcceptanceByAmountRange(bankId, dateFrom, dateTo);
        
        String granularity = determineGranularity(dateFrom, dateTo);
        List<AnalyticsTrendDTO> trends = getTrendData(bankId, dateFrom, dateTo, granularity);
        
        return AnalyticsResponseDTO.builder()
                .metrics(metrics)
                .trends(trends)
                .loanTypeBreakdown(loanTypeBreakdown)
                .amountRangeBreakdown(amountRangeBreakdown)
                .reportGeneratedAt(LocalDate.now())
                .build();
    }

    private LocalDate[] calculateDateRange(AnalyticsRequest request) {
        if (request.getPreset() == null || request.getPreset() == AnalyticsRequest.DatePreset.CUSTOM) {
            return new LocalDate[]{request.getDateFrom(), request.getDateTo()};
        }
        
        LocalDate today = LocalDate.now();
        switch (request.getPreset()) {
            case TODAY:
                return new LocalDate[]{today, today};
            case LAST_7:
                return new LocalDate[]{today.minusDays(6), today};
            case LAST_30:
                return new LocalDate[]{today.minusDays(29), today};
            case LAST_YEAR:
                return new LocalDate[]{today.minusYears(1), today};
            default:
                return new LocalDate[]{today.minusDays(29), today};
        }
    }

    private AnalyticsMetricsDTO calculateMetrics(List<Application> applications, List<Offer> offers, UUID bankId) {
        long applicationsReceived = applications.size();
        long offersSubmitted = offers.size();
        
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (applicationsReceived > 0) {
            conversionRate = BigDecimal.valueOf(offersSubmitted)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(applicationsReceived), 2, RoundingMode.HALF_UP);
        }
        
        double avgTimeToOfferHours = 0.0;
        if (!offers.isEmpty()) {
            long totalHours = 0;
            int validCount = 0;
            for (Offer offer : offers) {
                if (offer.getOfferSubmittedAt() != null) {
                    Optional<Application> appOpt = applications.stream()
                            .filter(a -> a.getId().equals(offer.getApplicationId()))
                            .findFirst();
                    if (appOpt.isPresent()) {
                        long hours = Duration.between(
                                appOpt.get().getCreatedAt(),
                                offer.getOfferSubmittedAt()
                        ).toHours();
                        totalHours += hours;
                        validCount++;
                    }
                }
            }
            if (validCount > 0) {
                avgTimeToOfferHours = (double) totalHours / validCount;
            }
        }
        
        BigDecimal avgAPR = BigDecimal.ZERO;
        if (!offers.isEmpty()) {
            BigDecimal totalAPR = offers.stream()
                    .map(Offer::getApr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgAPR = totalAPR.divide(BigDecimal.valueOf(offers.size()), 2, RoundingMode.HALF_UP);
        }
        
        return AnalyticsMetricsDTO.builder()
                .applicationsReceived(applicationsReceived)
                .offersSubmitted(offersSubmitted)
                .conversionRate(conversionRate)
                .avgTimeToOfferHours(avgTimeToOfferHours)
                .avgAPR(avgAPR)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, LoanTypeBreakdownDTO> getAcceptanceByLoanType(UUID bankId, LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo.atTime(LocalTime.MAX);
        
        List<Offer> offers = offerRepository.findByBankIdAndOfferSubmittedAtBetween(bankId, dateTimeFrom, dateTimeTo);
        
        Map<String, LoanTypeBreakdownDTO> breakdown = new HashMap<>();
        
        Map<String, List<Offer>> offersByType = new HashMap<>();
        for (Offer offer : offers) {
            Optional<Application> appOpt = applicationRepository.findById(offer.getApplicationId());
            if (appOpt.isPresent()) {
                String loanType = appOpt.get().getLoanType();
                offersByType.computeIfAbsent(loanType, k -> new ArrayList<>()).add(offer);
            }
        }
        
        for (Map.Entry<String, List<Offer>> entry : offersByType.entrySet()) {
            String loanType = entry.getKey();
            List<Offer> typeOffers = entry.getValue();
            
            long count = typeOffers.size();
            long accepted = typeOffers.stream()
                    .filter(o -> o.getBorrowerSelectedAt() != null)
                    .count();
            
            BigDecimal acceptanceRate = BigDecimal.ZERO;
            if (count > 0) {
                acceptanceRate = BigDecimal.valueOf(accepted)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            }
            
            breakdown.put(loanType, LoanTypeBreakdownDTO.builder()
                    .count(count)
                    .accepted(accepted)
                    .acceptanceRate(acceptanceRate)
                    .build());
        }
        
        return breakdown;
    }

    @Transactional(readOnly = true)
    public Map<String, AmountRangeBreakdownDTO> getAcceptanceByAmountRange(UUID bankId, LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo.atTime(LocalTime.MAX);
        
        List<Offer> offers = offerRepository.findByBankIdAndOfferSubmittedAtBetween(bankId, dateTimeFrom, dateTimeTo);
        
        String[] ranges = {"0-10k", "10k-25k", "25k-50k", "50k-100k", "100k+"};
        Map<String, AmountRangeBreakdownDTO> breakdown = new LinkedHashMap<>();
        
        for (String range : ranges) {
            breakdown.put(range, AmountRangeBreakdownDTO.builder()
                    .range(range)
                    .count(0L)
                    .accepted(0L)
                    .acceptanceRate(BigDecimal.ZERO)
                    .build());
        }
        
        for (Offer offer : offers) {
            Optional<Application> appOpt = applicationRepository.findById(offer.getApplicationId());
            if (appOpt.isPresent()) {
                BigDecimal amount = appOpt.get().getLoanAmount();
                String range = categorizeAmount(amount);
                
                AmountRangeBreakdownDTO dto = breakdown.get(range);
                dto.setCount(dto.getCount() + 1);
                if (offer.getBorrowerSelectedAt() != null) {
                    dto.setAccepted(dto.getAccepted() + 1);
                }
            }
        }
        
        for (AmountRangeBreakdownDTO dto : breakdown.values()) {
            if (dto.getCount() > 0) {
                BigDecimal rate = BigDecimal.valueOf(dto.getAccepted())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(dto.getCount()), 2, RoundingMode.HALF_UP);
                dto.setAcceptanceRate(rate);
            }
        }
        
        return breakdown;
    }

    private String categorizeAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(10000)) < 0) {
            return "0-10k";
        } else if (amount.compareTo(BigDecimal.valueOf(25000)) < 0) {
            return "10k-25k";
        } else if (amount.compareTo(BigDecimal.valueOf(50000)) < 0) {
            return "25k-50k";
        } else if (amount.compareTo(BigDecimal.valueOf(100000)) < 0) {
            return "50k-100k";
        } else {
            return "100k+";
        }
    }

    @Transactional(readOnly = true)
    public List<AnalyticsTrendDTO> getTrendData(UUID bankId, LocalDate dateFrom, LocalDate dateTo, String granularity) {
        LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo.atTime(LocalTime.MAX);
        
        List<Application> allApplications = applicationRepository.findByCreatedAtBetween(dateTimeFrom, dateTimeTo);
        List<Offer> allOffers = offerRepository.findByBankIdAndOfferSubmittedAtBetween(bankId, dateTimeFrom, dateTimeTo);
        
        List<AnalyticsTrendDTO> trends = new ArrayList<>();
        
        LocalDate current = dateFrom;
        while (!current.isAfter(dateTo)) {
            LocalDate periodEnd = calculatePeriodEnd(current, granularity, dateTo);
            
            LocalDateTime periodStart = current.atStartOfDay();
            LocalDateTime periodEndTime = periodEnd.atTime(LocalTime.MAX);
            
            long appsInPeriod = allApplications.stream()
                    .filter(a -> !a.getCreatedAt().isBefore(periodStart) && !a.getCreatedAt().isAfter(periodEndTime))
                    .count();
            
            long offersInPeriod = allOffers.stream()
                    .filter(o -> o.getOfferSubmittedAt() != null && 
                            !o.getOfferSubmittedAt().isBefore(periodStart) && 
                            !o.getOfferSubmittedAt().isAfter(periodEndTime))
                    .count();
            
            long acceptedInPeriod = allOffers.stream()
                    .filter(o -> o.getBorrowerSelectedAt() != null &&
                            o.getOfferSubmittedAt() != null &&
                            !o.getOfferSubmittedAt().isBefore(periodStart) &&
                            !o.getOfferSubmittedAt().isAfter(periodEndTime))
                    .count();
            
            BigDecimal acceptanceRate = BigDecimal.ZERO;
            if (offersInPeriod > 0) {
                acceptanceRate = BigDecimal.valueOf(acceptedInPeriod)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(offersInPeriod), 2, RoundingMode.HALF_UP);
            }
            
            trends.add(AnalyticsTrendDTO.builder()
                    .date(current)
                    .applicationsCount(appsInPeriod)
                    .offersCount(offersInPeriod)
                    .acceptanceRate(acceptanceRate)
                    .build());
            
            current = advancePeriod(current, granularity);
        }
        
        return trends;
    }

    private String determineGranularity(LocalDate dateFrom, LocalDate dateTo) {
        long days = ChronoUnit.DAYS.between(dateFrom, dateTo);
        if (days <= 7) {
            return "DAILY";
        } else if (days <= 90) {
            return "WEEKLY";
        } else {
            return "MONTHLY";
        }
    }

    private LocalDate calculatePeriodEnd(LocalDate start, String granularity, LocalDate maxDate) {
        LocalDate end;
        switch (granularity) {
            case "DAILY":
                end = start;
                break;
            case "WEEKLY":
                end = start.plusDays(6);
                break;
            case "MONTHLY":
                end = start.plusMonths(1).minusDays(1);
                break;
            default:
                end = start;
        }
        return end.isAfter(maxDate) ? maxDate : end;
    }

    private LocalDate advancePeriod(LocalDate current, String granularity) {
        switch (granularity) {
            case "DAILY":
                return current.plusDays(1);
            case "WEEKLY":
                return current.plusWeeks(1);
            case "MONTHLY":
                return current.plusMonths(1);
            default:
                return current.plusDays(1);
        }
    }
}
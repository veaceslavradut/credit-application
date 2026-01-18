package com.creditapp.bank.service;

import com.creditapp.bank.dto.*;
import com.creditapp.bank.exception.InsufficientMarketDataException;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BankMarketAnalysisService {

    private static final int MINIMUM_BANKS_FOR_ANALYSIS = 3;
    
    private final BankRateCardRepository rateCardRepository;

    public BankMarketAnalysisService(BankRateCardRepository rateCardRepository) {
        this.rateCardRepository = rateCardRepository;
    }

    @Cacheable(value = "bankMarketAnalysis", key = "#bankId.toString()")
    public MarketAnalysisDTO analyzeMarket(UUID bankId) {
        // Fetch all active rate cards for this bank (valid_to IS NULL)
        List<BankRateCard> myRateCards = rateCardRepository.findByBankIdAndValidToIsNull(bankId);
        
        if (myRateCards.isEmpty()) {
            throw new IllegalArgumentException("No active rate cards found for bank");
        }

        List<MyBankRateCardDTO> myBankRates = new ArrayList<>();
        List<MarketAverageDTO> marketAverages = new ArrayList<>();
        Set<UUID> allBankIds = new HashSet<>();

        // Analyze each rate card
        for (BankRateCard myCard : myRateCards) {
            // Get market data for this loan type and currency
            MarketAverageDTO marketAvg = calculateMarketAverage(myCard.getLoanType(), myCard.getCurrency());
            marketAverages.add(marketAvg);
            
                // Calculate percentile ranking using currently active cards filtered by loanType + currency
                List<BankRateCard> activeCards = rateCardRepository.findByValidToIsNull();
                List<BankRateCard> competitorCards = activeCards.stream()
                    .filter(c -> c.getLoanType() == myCard.getLoanType() && c.getCurrency() == myCard.getCurrency())
                    .collect(Collectors.toList());
                // Collect bank IDs for privacy check (for this market subset)
                Set<UUID> marketBankIds = competitorCards.stream()
                    .map(BankRateCard::getBankId)
                    .collect(Collectors.toSet());
                if (marketBankIds.size() < MINIMUM_BANKS_FOR_ANALYSIS) {
                throw new InsufficientMarketDataException(
                    String.format("Insufficient market data: only %d banks found, minimum %d required",
                        marketBankIds.size(), MINIMUM_BANKS_FOR_ANALYSIS));
                }
                // Track all unique bank IDs encountered (for response metadata)
                allBankIds.addAll(marketBankIds);
            
            List<BigDecimal> allAprs = competitorCards.stream()
                    .map(BankRateCard::getBaseApr)
                    .collect(Collectors.toList());
            
            Integer percentile = calculatePercentileRanking(myCard.getBaseApr(), allAprs);
            CompetitivePosition position = determineCompetitivePosition(percentile);
            
            MyBankRateCardDTO myBankRate = new MyBankRateCardDTO(
                    myCard.getLoanType(),
                    myCard.getCurrency(),
                    myCard.getBaseApr(),
                    percentile,
                    position,
                    myCard.getOriginationFeePercent(),
                    myCard.getInsurancePercent(),
                    myCard.getProcessingTimeDays()
            );
            myBankRates.add(myBankRate);
        }

        // Calculate overall competitive position (average of all percentiles)
        double avgPercentile = myBankRates.stream()
                .mapToInt(MyBankRateCardDTO::marketPercentileRanking)
                .average()
                .orElse(50.0);
        
        CompetitivePosition overallPosition = determineCompetitivePosition((int) Math.round(avgPercentile));

        MarketVisualizationDTO visualization = buildVisualizationData(myBankRates, marketAverages);
        return new MarketAnalysisDTO(
            myBankRates,
            marketAverages,
            overallPosition.name(),
            LocalDateTime.now(),
            allBankIds.size(),
            visualization
        );
    }

    @Cacheable(value = "marketAverage", key = "#loanType.name() + ':' + #currency.name()")
    public MarketAverageDTO calculateMarketAverage(LoanType loanType, Currency currency) {
        // Use active rate cards (valid_to IS NULL) and filter by loanType + currency
        List<BankRateCard> marketCards = rateCardRepository
                .findByValidToIsNull()
                .stream()
                .filter(c -> c.getLoanType() == loanType && c.getCurrency() == currency)
                .collect(Collectors.toList());

        if (marketCards.isEmpty()) {
            return new MarketAverageDTO(loanType, currency, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        }

        // Calculate statistics
        List<BigDecimal> aprs = marketCards.stream()
                .map(BankRateCard::getBaseApr)
                .sorted()
                .collect(Collectors.toList());

        BigDecimal avgApr = aprs.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(aprs.size()), 2, RoundingMode.HALF_UP);

        BigDecimal medianApr = calculateMedian(aprs);
        BigDecimal minApr = aprs.get(0);
        BigDecimal maxApr = aprs.get(aprs.size() - 1);

        BigDecimal avgOriginationFee = marketCards.stream()
                .map(BankRateCard::getOriginationFeePercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(marketCards.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgInsurance = marketCards.stream()
                .map(BankRateCard::getInsurancePercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(marketCards.size()), 2, RoundingMode.HALF_UP);

        int avgProcessingTime = (int) marketCards.stream()
                .mapToInt(BankRateCard::getProcessingTimeDays)
                .average()
                .orElse(0);

        // Get unique bank count for privacy
        Set<UUID> uniqueBanks = marketCards.stream()
                .map(BankRateCard::getBankId)
                .collect(Collectors.toSet());

        return new MarketAverageDTO(
                loanType,
                currency,
                avgApr,
                medianApr,
                minApr,
                maxApr,
                avgOriginationFee,
                avgInsurance,
                avgProcessingTime,
                uniqueBanks.size()
        );
    }

    public Integer calculatePercentileRanking(BigDecimal myApr, List<BigDecimal> allAprs) {
        // Sort APRs ascending (lowest to highest)
        List<BigDecimal> sortedAprs = allAprs.stream()
                .sorted()
                .collect(Collectors.toList());

        int total = sortedAprs.size();
        if (total == 0) return 0;

        // Determine position (1-based) as count of APRs strictly less than myApr, then +1
        int lessCount = 0;
        for (BigDecimal apr : sortedAprs) {
            if (apr.compareTo(myApr) < 0) {
                lessCount++;
            } else {
                break;
            }
        }
        int position = lessCount + 1;

        // Lower APR = better = higher percentile (invert ranking)
        return Math.round(((float) (total - position + 1) / total) * 100);
    }

    public CompetitivePosition determineCompetitivePosition(Integer percentile) {
        if (percentile >= 75) {
            return CompetitivePosition.MORE_COMPETITIVE;
        } else if (percentile >= 25) {
            return CompetitivePosition.AVERAGE;
        } else {
            return CompetitivePosition.LESS_COMPETITIVE;
        }
    }

    private BigDecimal calculateMedian(List<BigDecimal> sortedValues) {
        int size = sortedValues.size();
        if (size == 0) {
            return BigDecimal.ZERO;
        }
        
        if (size % 2 == 1) {
            return sortedValues.get(size / 2);
        } else {
            BigDecimal mid1 = sortedValues.get(size / 2 - 1);
            BigDecimal mid2 = sortedValues.get(size / 2);
            return mid1.add(mid2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
    }

        private MarketVisualizationDTO buildVisualizationData(List<MyBankRateCardDTO> myBankRates,
                                  List<MarketAverageDTO> marketAverages) {
        Map<String, MarketAverageDTO> avgByKey = marketAverages.stream()
            .collect(Collectors.toMap(
                m -> m.loanType().name() + ":" + m.currency().name(),
                m -> m
            ));

        List<MarketVisualizationDTO.AprComparisonItem> apr = new ArrayList<>();
        List<MarketVisualizationDTO.FeeComparisonItem> fees = new ArrayList<>();
        List<MarketVisualizationDTO.ProcessingTimeComparisonItem> times = new ArrayList<>();

        for (MyBankRateCardDTO my : myBankRates) {
            String key = my.loanType().name() + ":" + my.currency().name();
            MarketAverageDTO avg = avgByKey.get(key);
            if (avg == null) continue;

                apr.add(new MarketVisualizationDTO.AprComparisonItem(
                my.loanType(), my.currency(),
                my.baseApr(), avg.medianApr(), avg.minApr(), avg.maxApr()
            ));

                fees.add(new MarketVisualizationDTO.FeeComparisonItem(
                my.loanType(), my.currency(),
                my.originationFeePercent(), avg.averageOriginationFee(),
                my.insurancePercent(), avg.averageInsuranceCost()
            ));

                times.add(new MarketVisualizationDTO.ProcessingTimeComparisonItem(
                my.loanType(), my.currency(),
                my.processingTimeDays(), avg.averageProcessingTime()
            ));
        }

        return new MarketVisualizationDTO(apr, fees, times);
        }
}
package com.creditapp.borrower.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.OfferInsightsDTO;
import com.creditapp.borrower.dto.OfferSummaryDTO;
import com.creditapp.borrower.dto.SavingsAnalysisDTO;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OfferInsightsService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;

    public OfferInsightsService(OfferRepository offerRepository, 
                               ApplicationRepository applicationRepository,
                               OrganizationRepository organizationRepository) {
        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
        this.organizationRepository = organizationRepository;
    }

    public OfferInsightsDTO calculateInsights(UUID applicationId, UUID borrowerId) {
        // Verify borrower owns the application
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        
        if (!application.getBorrowerId().equals(borrowerId)) {
            throw new IllegalArgumentException("Cannot access another borrower's application");
        }

        // Fetch all offers for the application
        List<Offer> offers = offerRepository.findByApplicationId(applicationId);

        // Need at least 2 offers for meaningful insights
        if (offers.size() < 2) {
            return null; // Controller will return 204 No Content
        }

        // Get bank names for offers
        Map<UUID, String> bankNames = getBankNames(offers);

        // Find best offers
        Offer bestAprOffer = offers.stream()
                .min(Comparator.comparing(Offer::getApr))
                .orElseThrow();
        
        Offer lowestMonthlyPaymentOffer = offers.stream()
                .min(Comparator.comparing(Offer::getMonthlyPayment))
                .orElseThrow();
        
        Offer lowestTotalCostOffer = offers.stream()
                .min(Comparator.comparing(Offer::getTotalCost))
                .orElseThrow();

        // Calculate average APR
        BigDecimal averageApr = offers.stream()
                .map(Offer::getApr)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(offers.size()), 2, RoundingMode.HALF_UP);

        // Calculate APR spread
        BigDecimal maxApr = offers.stream()
                .map(Offer::getApr)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal minApr = offers.stream()
                .map(Offer::getApr)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal aprSpread = maxApr.subtract(minApr);

        // Calculate recommended offer (highest weighted score)
        UUID recommendedOfferId = calculateRecommendedOffer(offers);

        // Calculate savings analysis
        SavingsAnalysisDTO savingsAnalysis = calculateSavings(offers, bankNames);

        // Convert to DTOs
        OfferSummaryDTO bestAprDTO = toSummaryDTO(bestAprOffer, bankNames.get(bestAprOffer.getBankId()));
        OfferSummaryDTO lowestMonthlyPaymentDTO = toSummaryDTO(lowestMonthlyPaymentOffer, 
                                                                bankNames.get(lowestMonthlyPaymentOffer.getBankId()));
        OfferSummaryDTO lowestTotalCostDTO = toSummaryDTO(lowestTotalCostOffer, 
                                                            bankNames.get(lowestTotalCostOffer.getBankId()));

        return new OfferInsightsDTO(
                bestAprDTO,
                lowestMonthlyPaymentDTO,
                lowestTotalCostDTO,
                averageApr,
                aprSpread,
                recommendedOfferId,
                savingsAnalysis
        );
    }

    public UUID calculateRecommendedOffer(List<Offer> offers) {
        // Weighted scoring: APR (40%), monthly payment (30%), total cost (20%), processing time (10%)
        return offers.stream()
                .max(Comparator.comparing(offer -> calculateScore(offer, offers)))
                .map(Offer::getId)
                .orElseThrow();
    }

    private BigDecimal calculateScore(Offer offer, List<Offer> allOffers) {
        // Lower is better, so use 1/value for scoring
        // Normalize by finding min/max for each metric across all offers
        
        BigDecimal aprScore = BigDecimal.ONE.divide(offer.getApr(), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("0.4"));
        
        BigDecimal monthlyPaymentScore = BigDecimal.ONE.divide(offer.getMonthlyPayment(), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("0.3"));
        
        BigDecimal totalCostScore = BigDecimal.ONE.divide(offer.getTotalCost(), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("0.2"));
        
        BigDecimal processingTimeScore = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(offer.getProcessingTimeDays()), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("0.1"));

        return aprScore.add(monthlyPaymentScore).add(totalCostScore).add(processingTimeScore);
    }

    public SavingsAnalysisDTO calculateSavings(List<Offer> offers, Map<UUID, String> bankNames) {
        Offer bestOffer = offers.stream()
                .min(Comparator.comparing(Offer::getTotalCost))
                .orElseThrow();
        
        Offer worstOffer = offers.stream()
                .max(Comparator.comparing(Offer::getTotalCost))
                .orElseThrow();

        BigDecimal savingsVsWorst = worstOffer.getTotalCost().subtract(bestOffer.getTotalCost());

        BigDecimal averageTotalCost = offers.stream()
                .map(Offer::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(offers.size()), 2, RoundingMode.HALF_UP);

        BigDecimal savingsVsAverage = averageTotalCost.subtract(bestOffer.getTotalCost());

        String bestBankName = bankNames.getOrDefault(bestOffer.getBankId(), "Best Offer");
        String worstBankName = bankNames.getOrDefault(worstOffer.getBankId(), "Highest Cost Offer");

        String savingsMessage = String.format(
                "You could save $%,.2f by choosing %s over %s",
                savingsVsWorst,
                bestBankName,
                worstBankName
        );

        return new SavingsAnalysisDTO(
                bestOffer.getId(),
                savingsVsWorst,
                savingsVsAverage,
                savingsMessage
        );
    }

    private Map<UUID, String> getBankNames(List<Offer> offers) {
        List<UUID> bankIds = offers.stream()
                .map(Offer::getBankId)
                .distinct()
                .toList();

        return organizationRepository.findAllById(bankIds).stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getName));
    }

    private OfferSummaryDTO toSummaryDTO(Offer offer, String bankName) {
        return new OfferSummaryDTO(
                offer.getId(),
                bankName != null ? bankName : "Unknown Bank",
                offer.getApr(),
                offer.getMonthlyPayment(),
                offer.getTotalCost()
        );
    }
}

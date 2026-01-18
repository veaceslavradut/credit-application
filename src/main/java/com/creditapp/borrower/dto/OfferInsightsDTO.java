package com.creditapp.borrower.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OfferInsightsDTO(
    OfferSummaryDTO bestAprOffer,
    OfferSummaryDTO lowestMonthlyPaymentOffer,
    OfferSummaryDTO lowestTotalCostOffer,
    BigDecimal averageApr,
    BigDecimal aprSpread,
    UUID recommendedOfferId,
    SavingsAnalysisDTO savingsAnalysis
) {}

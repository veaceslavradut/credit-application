package com.creditapp.bank.dto;

import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import java.math.BigDecimal;
import java.util.List;

public record MarketVisualizationDTO(
        List<MarketVisualizationDTO.AprComparisonItem> aprComparisons,
        List<MarketVisualizationDTO.FeeComparisonItem> feeComparisons,
        List<MarketVisualizationDTO.ProcessingTimeComparisonItem> processingComparisons
) {
    public record AprComparisonItem(
            LoanType loanType,
            Currency currency,
            BigDecimal myApr,
            BigDecimal marketMedianApr,
            BigDecimal marketMinApr,
            BigDecimal marketMaxApr
    ) {}

    public record FeeComparisonItem(
            LoanType loanType,
            Currency currency,
            BigDecimal myOriginationFeePercent,
            BigDecimal marketAvgOriginationFeePercent,
            BigDecimal myInsurancePercent,
            BigDecimal marketAvgInsurancePercent
    ) {}

    public record ProcessingTimeComparisonItem(
            LoanType loanType,
            Currency currency,
            Integer myProcessingDays,
            Integer marketAverageProcessingDays
    ) {}
}

 
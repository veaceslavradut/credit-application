package com.creditapp.bank.dto;

import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;

import java.math.BigDecimal;

public record MyBankRateCardDTO(
    LoanType loanType,
    Currency currency,
    BigDecimal baseApr,
    Integer marketPercentileRanking,
    CompetitivePosition competitivePosition,
    BigDecimal originationFeePercent,
    BigDecimal insurancePercent,
    Integer processingTimeDays
) {}
package com.creditapp.bank.dto;

import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;

import java.math.BigDecimal;

public record MarketAverageDTO(
    LoanType loanType,
    Currency currency,
    BigDecimal averageApr,
    BigDecimal medianApr,
    BigDecimal minApr,
    BigDecimal maxApr,
    BigDecimal averageOriginationFee,
    BigDecimal averageInsuranceCost,
    Integer averageProcessingTime,
    Integer bankCount
) {}
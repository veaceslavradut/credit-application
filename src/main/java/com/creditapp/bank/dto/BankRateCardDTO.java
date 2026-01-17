package com.creditapp.bank.dto;

import com.creditapp.borrower.model.LoanType;
import com.creditapp.borrower.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BankRateCardDTO {
    public UUID id;
    public UUID bankId;
    public LoanType loanType;
    public Currency currency;
    public BigDecimal minLoanAmount;
    public BigDecimal maxLoanAmount;
    public BigDecimal baseApr;
    public BigDecimal aprAdjustmentRange;
    public BigDecimal originationFeePercent;
    public BigDecimal insurancePercent;
    public Integer processingTimeDays;
    public LocalDateTime validFrom;
    public LocalDateTime validTo;
    public boolean active;
}
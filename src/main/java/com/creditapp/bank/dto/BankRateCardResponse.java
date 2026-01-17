package com.creditapp.bank.dto;

import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BankRateCardResponse {
    
    private UUID id;
    private LoanType loanType;
    private Currency currency;
    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private BigDecimal baseApr;
    private BigDecimal aprAdjustmentRange;
    private BigDecimal originationFeePercent;
    private BigDecimal insurancePercent;
    private Integer processingTimeDays;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean active;
    
    // Constructors
    public BankRateCardResponse() {}
    
    public BankRateCardResponse(UUID id, LoanType loanType, Currency currency,
            BigDecimal minLoanAmount, BigDecimal maxLoanAmount, BigDecimal baseApr,
            BigDecimal aprAdjustmentRange, BigDecimal originationFeePercent,
            BigDecimal insurancePercent, Integer processingTimeDays,
            LocalDateTime validFrom, LocalDateTime validTo) {
        this.id = id;
        this.loanType = loanType;
        this.currency = currency;
        this.minLoanAmount = minLoanAmount;
        this.maxLoanAmount = maxLoanAmount;
        this.baseApr = baseApr;
        this.aprAdjustmentRange = aprAdjustmentRange;
        this.originationFeePercent = originationFeePercent;
        this.insurancePercent = insurancePercent;
        this.processingTimeDays = processingTimeDays;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = validTo == null;
    }
    
    // Helper method
    public boolean isActive() {
        return validTo == null;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public LoanType getLoanType() {
        return loanType;
    }
    
    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public BigDecimal getMinLoanAmount() {
        return minLoanAmount;
    }
    
    public void setMinLoanAmount(BigDecimal minLoanAmount) {
        this.minLoanAmount = minLoanAmount;
    }
    
    public BigDecimal getMaxLoanAmount() {
        return maxLoanAmount;
    }
    
    public void setMaxLoanAmount(BigDecimal maxLoanAmount) {
        this.maxLoanAmount = maxLoanAmount;
    }
    
    public BigDecimal getBaseApr() {
        return baseApr;
    }
    
    public void setBaseApr(BigDecimal baseApr) {
        this.baseApr = baseApr;
    }
    
    public BigDecimal getAprAdjustmentRange() {
        return aprAdjustmentRange;
    }
    
    public void setAprAdjustmentRange(BigDecimal aprAdjustmentRange) {
        this.aprAdjustmentRange = aprAdjustmentRange;
    }
    
    public BigDecimal getOriginationFeePercent() {
        return originationFeePercent;
    }
    
    public void setOriginationFeePercent(BigDecimal originationFeePercent) {
        this.originationFeePercent = originationFeePercent;
    }
    
    public BigDecimal getInsurancePercent() {
        return insurancePercent;
    }
    
    public void setInsurancePercent(BigDecimal insurancePercent) {
        this.insurancePercent = insurancePercent;
    }
    
    public Integer getProcessingTimeDays() {
        return processingTimeDays;
    }
    
    public void setProcessingTimeDays(Integer processingTimeDays) {
        this.processingTimeDays = processingTimeDays;
    }
    
    public LocalDateTime getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }
    
    public LocalDateTime getValidTo() {
        return validTo;
    }
    
    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
}
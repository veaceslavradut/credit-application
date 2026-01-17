package com.creditapp.bank.dto;

import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BankRateCardRequest {
    
    @NotNull(message = "Loan type is required")
    private LoanType loanType;
    
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    @NotNull(message = "Minimum loan amount is required")
    @DecimalMin(value = "100", message = "Minimum loan amount must be at least 100")
    @DecimalMax(value = "1000000", message = "Minimum loan amount cannot exceed 1,000,000")
    private BigDecimal minLoanAmount;
    
    @NotNull(message = "Maximum loan amount is required")
    @DecimalMin(value = "100", message = "Maximum loan amount must be at least 100")
    @DecimalMax(value = "1000000", message = "Maximum loan amount cannot exceed 1,000,000")
    private BigDecimal maxLoanAmount;
    
    @NotNull(message = "Base APR is required")
    @DecimalMin(value = "0.5", message = "Base APR must be at least 0.5%")
    @DecimalMax(value = "50", message = "Base APR cannot exceed 50%")
    private BigDecimal baseApr;
    
    @NotNull(message = "APR adjustment range is required")
    @DecimalMin(value = "0", message = "APR adjustment range cannot be negative")
    @DecimalMax(value = "5", message = "APR adjustment range cannot exceed 5%")
    private BigDecimal aprAdjustmentRange;
    
    @NotNull(message = "Origination fee percentage is required")
    @DecimalMin(value = "0", message = "Origination fee percentage cannot be negative")
    @DecimalMax(value = "10", message = "Origination fee percentage cannot exceed 10%")
    private BigDecimal originationFeePercent;
    
    @NotNull(message = "Insurance percentage is required")
    @DecimalMin(value = "0", message = "Insurance percentage cannot be negative")
    @DecimalMax(value = "5", message = "Insurance percentage cannot exceed 5%")
    private BigDecimal insurancePercent;
    
    // Constructors
    public BankRateCardRequest() {}
    
    public BankRateCardRequest(LoanType loanType, Currency currency, BigDecimal minLoanAmount,
            BigDecimal maxLoanAmount, BigDecimal baseApr, BigDecimal aprAdjustmentRange,
            BigDecimal originationFeePercent, BigDecimal insurancePercent) {
        this.loanType = loanType;
        this.currency = currency;
        this.minLoanAmount = minLoanAmount;
        this.maxLoanAmount = maxLoanAmount;
        this.baseApr = baseApr;
        this.aprAdjustmentRange = aprAdjustmentRange;
        this.originationFeePercent = originationFeePercent;
        this.insurancePercent = insurancePercent;
    }
    
    // Getters and Setters
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
}
package com.creditapp.bank.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.creditapp.borrower.model.LoanType;
import com.creditapp.borrower.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a bank's rate card configuration for a specific loan type and currency.
 * Rate cards are versioned: active cards have valid_to = NULL, inactive cards have valid_to set.
 */
@Entity
@Table(name = "bank_rate_cards")
public class BankRateCard {
    
    @Id
    private UUID id;
    
    @Column(name = "bank_id", nullable = false)
    private UUID bankId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;
    
    @Column(name = "min_loan_amount", nullable = false)
    private BigDecimal minLoanAmount;
    
    @Column(name = "max_loan_amount", nullable = false)
    private BigDecimal maxLoanAmount;
    
    @Column(name = "base_apr", nullable = false)
    private BigDecimal baseApr;
    
    @Column(name = "apr_adjustment_range", nullable = false)
    private BigDecimal aprAdjustmentRange;
    
    @Column(name = "origination_fee_percent", nullable = false)
    private BigDecimal originationFeePercent;
    
    @Column(name = "insurance_percent")
    private BigDecimal insurancePercent;
    
    @Column(name = "processing_time_days", nullable = false)
    private Integer processingTimeDays;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to")
    private LocalDateTime validTo;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public BankRateCard() {}

    public BankRateCard(UUID id, UUID bankId, LoanType loanType, Currency currency,
                       BigDecimal minLoanAmount, BigDecimal maxLoanAmount, BigDecimal baseApr,
                       BigDecimal aprAdjustmentRange, BigDecimal originationFeePercent,
                       BigDecimal insurancePercent, Integer processingTimeDays,
                       LocalDateTime validFrom, LocalDateTime validTo) {
        this.id = id;
        this.bankId = bankId;
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
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID bankId) {
        this.bankId = bankId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Check if this rate card is currently active.
     * @return true if valid_to is NULL (active), false otherwise
     */
    @Transient
    public boolean isActive() {
        return this.validTo == null;
    }
}
package com.creditapp.borrower.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OfferComparisonDTO {
    private UUID offerId;
    private UUID bankId;
    private String bankName;
    private String logoUrl;
    private BigDecimal apr;
    private BigDecimal monthlyPayment;
    private BigDecimal totalCost;
    private BigDecimal originationFee;
    private BigDecimal insuranceCost;
    private Integer processingTimeDays;
    private Integer validityPeriodDays;
    private List<String> requiredDocuments;
    private LocalDateTime expiresAt;
    private String offerStatus;
    private Boolean offersExpired;
    private String recalculateUrl;

    public OfferComparisonDTO() {}

    public OfferComparisonDTO(UUID offerId, UUID bankId, String bankName, String logoUrl,
                              BigDecimal apr, BigDecimal monthlyPayment, BigDecimal totalCost,
                              BigDecimal originationFee, BigDecimal insuranceCost,
                              Integer processingTimeDays, Integer validityPeriodDays,
                              List<String> requiredDocuments, LocalDateTime expiresAt, String offerStatus) {
        this.offerId = offerId;
        this.bankId = bankId;
        this.bankName = bankName;
        this.logoUrl = logoUrl;
        this.apr = apr;
        this.monthlyPayment = monthlyPayment;
        this.totalCost = totalCost;
        this.originationFee = originationFee;
        this.insuranceCost = insuranceCost;
        this.processingTimeDays = processingTimeDays;
        this.validityPeriodDays = validityPeriodDays;
        this.requiredDocuments = requiredDocuments;
        this.expiresAt = expiresAt;
        this.offerStatus = offerStatus;
    }

    public UUID getOfferId() { return offerId; }
    public void setOfferId(UUID offerId) { this.offerId = offerId; }
    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public BigDecimal getApr() { return apr; }
    public void setApr(BigDecimal apr) { this.apr = apr; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getOriginationFee() { return originationFee; }
    public void setOriginationFee(BigDecimal originationFee) { this.originationFee = originationFee; }
    public BigDecimal getInsuranceCost() { return insuranceCost; }
    public void setInsuranceCost(BigDecimal insuranceCost) { this.insuranceCost = insuranceCost; }
    public Integer getProcessingTimeDays() { return processingTimeDays; }
    public void setProcessingTimeDays(Integer processingTimeDays) { this.processingTimeDays = processingTimeDays; }
    public Integer getValidityPeriodDays() { return validityPeriodDays; }
    public void setValidityPeriodDays(Integer validityPeriodDays) { this.validityPeriodDays = validityPeriodDays; }
    public List<String> getRequiredDocuments() { return requiredDocuments; }
    public void setRequiredDocuments(List<String> requiredDocuments) { this.requiredDocuments = requiredDocuments; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getOfferStatus() { return offerStatus; }
    public void setOfferStatus(String offerStatus) { this.offerStatus = offerStatus; }
    public Boolean getOffersExpired() { return offersExpired; }
    public void setOffersExpired(Boolean offersExpired) { this.offersExpired = offersExpired; }
    public String getRecalculateUrl() { return recalculateUrl; }
    public void setRecalculateUrl(String recalculateUrl) { this.recalculateUrl = recalculateUrl; }
}
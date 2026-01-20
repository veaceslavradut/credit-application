package com.creditapp.borrower.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferComparisonTableRow {
    private UUID offerId;
    private UUID bankId;
    private String bankName;
    private String bankLogoUrl;
    private BigDecimal apr;
    private BigDecimal monthlyPayment;
    private BigDecimal totalCost;
    private BigDecimal originationFee;
    private BigDecimal insuranceCost;
    private Integer termMonths;
    private Integer processingTimeDays;
    private Integer validityPeriodDays;
    private LocalDateTime expiresAt;
    private String offerStatus;
    private String selectButtonState;
    private String expirationCountdown;
    private String expirationHighlight;
    private Boolean canResubmit;
    private String resubmitUrl;

    // Constructors
    public OfferComparisonTableRow() {}

    // Getters and Setters
    public UUID getOfferId() { return offerId; }
    public void setOfferId(UUID offerId) { this.offerId = offerId; }

    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankLogoUrl() { return bankLogoUrl; }
    public void setBankLogoUrl(String bankLogoUrl) { this.bankLogoUrl = bankLogoUrl; }

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

    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }

    public Integer getProcessingTimeDays() { return processingTimeDays; }
    public void setProcessingTimeDays(Integer processingTimeDays) { this.processingTimeDays = processingTimeDays; }

    public Integer getValidityPeriodDays() { return validityPeriodDays; }
    public void setValidityPeriodDays(Integer validityPeriodDays) { this.validityPeriodDays = validityPeriodDays; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getOfferStatus() { return offerStatus; }
    public void setOfferStatus(String offerStatus) { this.offerStatus = offerStatus; }

    public String getSelectButtonState() { return selectButtonState; }
    public void setSelectButtonState(String selectButtonState) { this.selectButtonState = selectButtonState; }

    public String getExpirationCountdown() { return expirationCountdown; }
    public void setExpirationCountdown(String expirationCountdown) { this.expirationCountdown = expirationCountdown; }

    public String getExpirationHighlight() { return expirationHighlight; }
    public void setExpirationHighlight(String expirationHighlight) { this.expirationHighlight = expirationHighlight; }

    public Boolean getCanResubmit() { return canResubmit; }
    public void setCanResubmit(Boolean canResubmit) { this.canResubmit = canResubmit; }

    public String getResubmitUrl() { return resubmitUrl; }
    public void setResubmitUrl(String resubmitUrl) { this.resubmitUrl = resubmitUrl; }
}

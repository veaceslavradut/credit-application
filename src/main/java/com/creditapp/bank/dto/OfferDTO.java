package com.creditapp.bank.dto;

import com.creditapp.bank.model.OfferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OfferDTO {
    public UUID id;
    public UUID applicationId;
    public UUID bankId;
    public String bankName;
    public OfferStatus offerStatus;
    public BigDecimal apr;
    public BigDecimal monthlyPayment;
    public BigDecimal totalCost;
    public BigDecimal originationFee;
    public BigDecimal insuranceCost;
    public Integer processingTimeDays;
    public Integer validityPeriodDays;
    public String requiredDocuments;
    public LocalDateTime createdAt;
    public LocalDateTime expiresAt;
    public LocalDateTime offerSubmittedAt;
}
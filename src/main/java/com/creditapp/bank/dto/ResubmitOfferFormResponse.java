package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for GET /api/bank/offers/{offerId}/resubmit
 * Returns previous offer details pre-filled for resubmission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResubmitOfferFormResponse {
    
    private UUID offerId;
    private UUID applicationId;
    private UUID bankId;
    
    // Previous offer details (pre-filled)
    private BigDecimal previousApr;
    private BigDecimal previousOriginationFee;
    private Integer previousProcessingTimeDays;
    private Integer previousValidityPeriodDays;
    private BigDecimal previousMonthlyPayment;
    private BigDecimal previousTotalCost;
    
    // Application details (read-only context)
    private BigDecimal loanAmount;
    private Integer loanTermMonths;
    
    // Expiration warning
    private LocalDateTime expiresAt;
    private Boolean isExpired;
    private String warningMessage;
}
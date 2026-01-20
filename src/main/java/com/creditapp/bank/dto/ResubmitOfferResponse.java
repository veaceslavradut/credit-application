package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for POST /api/bank/offers/{offerId}/resubmit response
 * Returns details of newly created resubmitted offer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResubmitOfferResponse {
    
    private UUID newOfferId;
    private UUID oldOfferId;
    private UUID applicationId;
    
    private BigDecimal apr;
    private BigDecimal originationFee;
    private Integer processingTimeDays;
    private Integer validityPeriodDays;
    
    private BigDecimal monthlyPayment;
    private BigDecimal totalCost;
    
    private LocalDateTime expiresAt;
    private String status;
    
    private String borrowerNotificationStatus;
    private String message;
}
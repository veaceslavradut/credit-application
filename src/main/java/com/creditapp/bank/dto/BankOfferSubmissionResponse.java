package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankOfferSubmissionResponse {

    private UUID offerId;
    
    private UUID applicationId;
    
    private BigDecimal apr;
    
    private BigDecimal monthlyPayment;
    
    private BigDecimal totalCost;
    
    private String status;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime expiresAt;
    
    private String borrowerNotificationStatus;
}
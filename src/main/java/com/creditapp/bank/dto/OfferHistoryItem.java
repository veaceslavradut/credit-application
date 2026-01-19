package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a single offer in the offer history list.
 * Contains all information needed for displaying an offer in the history view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferHistoryItem {

    private UUID offerId;
    
    private String borrowerName;
    
    private BigDecimal apr;
    
    private BigDecimal monthlyPayment;
    
    private Integer processingTimeDays;
    
    private LocalDateTime submittedDate;
    
    private String status; // SUBMITTED, ACCEPTED, EXPIRED, WITHDRAWN
    
    private String borrowerStatus; // NOT_VIEWED, VIEWED, ACCEPTED_OTHER
}

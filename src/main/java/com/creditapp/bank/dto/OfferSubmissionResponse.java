package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for bank offer submission.
 * Returns the submitted offer details with calculated values.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferSubmissionResponse {
    /**
     * The generated offer ID
     */
    private UUID offerId;
    
    /**
     * Annual Percentage Rate (APR) with 4 decimal precision
     */
    private BigDecimal apr;
    
    /**
     * Origination fees (2 decimal precision)
     */
    private BigDecimal fees;
    
    /**
     * Processing time in days
     */
    private Integer processingTime;
    
    /**
     * Monthly payment amount (2 decimal precision)
     */
    private BigDecimal monthlyPayment;
    
    /**
     * Total cost including all fees (2 decimal precision)
     */
    private BigDecimal totalCost;
    
    /**
     * Timestamp when offer was submitted (ISO-8601 UTC)
     */
    private LocalDateTime submittedAt;
    
    /**
     * HTTP status code (201 for new offer, 200 for existing)
     */
    private Integer httpStatus;
}

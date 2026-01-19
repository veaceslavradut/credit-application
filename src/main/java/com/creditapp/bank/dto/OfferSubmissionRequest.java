package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for bank offer submission.
 * Contains application ID and optional override parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferSubmissionRequest {
    /**
     * The application ID for which the offer is being submitted
     */
    private UUID applicationId;
    
    /**
     * If true, submit offer AS-IS from calculated offer (Story 3.3)
     * If false, apply overrides (APR, fees, processingTime)
     */
    private Boolean acceptCalculatedOffer;
    
    /**
     * Optional: Override APR (0.5% to 50%), only used if acceptCalculatedOffer=false
     */
    private BigDecimal overrideAPR;
    
    /**
     * Optional: Override origination fees (0 to 10,000), only used if acceptCalculatedOffer=false
     */
    private BigDecimal overrideFees;
    
    /**
     * Optional: Override processing time in days (1 to 365), only used if acceptCalculatedOffer=false
     */
    private Integer overrideProcessingTime;
}

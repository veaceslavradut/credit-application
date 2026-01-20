package com.creditapp.bank.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for POST /api/bank/offers/{offerId}/resubmit request
 * Contains updated offer values for resubmission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResubmitOfferRequest {
    
    @DecimalMin(value = "0.5", message = "APR must be at least 0.5%")
    @DecimalMax(value = "50.0", message = "APR must not exceed 50%")
    private BigDecimal apr;
    
    @DecimalMin(value = "0", message = "Origination fee must be non-negative")
    private BigDecimal originationFee;
    
    @Min(value = 1, message = "Processing time must be at least 1 day")
    @Max(value = 90, message = "Processing time must not exceed 90 days")
    private Integer processingTimeDays;
    
    @Min(value = 1, message = "Validity period must be at least 1 day")
    @Max(value = 90, message = "Validity period must not exceed 90 days")
    private Integer validityPeriodDays;
}
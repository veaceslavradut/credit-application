package com.creditapp.bank.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankOfferSubmissionRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    @NotNull(message = "APR is required")
    @DecimalMin(value = "4.0", message = "APR must be at least 4%")
    @DecimalMax(value = "20.0", message = "APR must not exceed 20%")
    private BigDecimal apr;

    @Min(value = 6, message = "Term must be at least 6 months")
    @Max(value = 480, message = "Term must not exceed 480 months")
    private Integer termMonths;

    private BigDecimal monthlyPayment;

    private BigDecimal totalCost;

    private BigDecimal originationFee;

    private BigDecimal insuranceCost;

    private Integer processingTimeDays;

    private List<String> requiredDocuments;

    private String notes;

    private Map<String, String> customTerms;
}
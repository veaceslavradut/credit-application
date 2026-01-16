package com.creditapp.borrower.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationRequest {

    @DecimalMin(value = "100", message = "Loan amount must be at least 100")
    @DecimalMax(value = "1000000", message = "Loan amount cannot exceed 1000000")
    private BigDecimal loanAmount;

    @Min(value = 6, message = "Loan term must be at least 6 months")
    @Max(value = 360, message = "Loan term cannot exceed 360 months")
    private Integer loanTermMonths;

    private String loanType;
    private String currency;
    private String ratePreference;

    private ApplicationDetailsDTO applicationDetails;
}
package com.creditapp.borrower.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateApplicationRequest {

    @NotBlank(message = "Loan type is required")
    private String loanType;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "100", message = "Loan amount must be at least 100")
    @DecimalMax(value = "1000000", message = "Loan amount cannot exceed 1,000,000")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan term is required")
    @Min(value = 6, message = "Loan term must be at least 6 months")
    @Max(value = 360, message = "Loan term cannot exceed 360 months")
    private Integer loanTermMonths;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String ratePreference;
}

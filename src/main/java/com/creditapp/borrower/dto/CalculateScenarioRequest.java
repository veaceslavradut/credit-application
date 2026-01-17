package com.creditapp.borrower.dto;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculateScenarioRequest {
    
    @jakarta.validation.constraints.NotNull(message = "Loan amount is required")
    @jakarta.validation.constraints.DecimalMin(value = "1000", message = "Loan amount must be at least 1000")
    @jakarta.validation.constraints.DecimalMax(value = "5000000", message = "Loan amount cannot exceed 5000000")
    private BigDecimal loanAmount;
    
    @jakarta.validation.constraints.NotNull(message = "Term months is required")
    @jakarta.validation.constraints.Min(value = 6, message = "Term must be at least 6 months")
    @jakarta.validation.constraints.Max(value = 480, message = "Term cannot exceed 480 months (40 years)")
    private Integer termMonths;
    
    private UUID bankId;
}
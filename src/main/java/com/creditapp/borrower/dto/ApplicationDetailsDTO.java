package com.creditapp.borrower.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Data transfer object for ApplicationDetails entity.
 * Used for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDetailsDTO {
    private BigDecimal annualIncome;
    private String employmentStatus;
    private BigDecimal downPaymentAmount;
}

package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for loan request details in application review panel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequestDetailsDTO {
    private String loanType;
    private BigDecimal amount;
    private Integer termMonths;
    private String purpose;
    private String incomeDocumentationStatus;
}

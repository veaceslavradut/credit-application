package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for employment details in application review panel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentDetailsDTO {
    private String employer;
    private String position;
    private BigDecimal annualIncome;
    private Integer yearsEmployed;
}

package com.creditapp.borrower.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPreferenceDTO {
    private UUID id;
    private BigDecimal preferredAmount;
    private Integer minTerm;
    private Integer maxTerm;
    private String purposeCategory;
    private Integer priority;
}
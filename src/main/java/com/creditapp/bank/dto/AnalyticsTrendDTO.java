package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsTrendDTO {
    private LocalDate date;
    private Long applicationsCount;
    private Long offersCount;
    private BigDecimal acceptanceRate;
}

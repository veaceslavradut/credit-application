package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmountRangeBreakdownDTO {
    private String range;
    private Long count;
    private Long accepted;
    private BigDecimal acceptanceRate;
}

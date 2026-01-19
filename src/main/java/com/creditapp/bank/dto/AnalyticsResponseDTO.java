package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponseDTO {
    private AnalyticsMetricsDTO metrics;
    private List<AnalyticsTrendDTO> trends;
    private Map<String, LoanTypeBreakdownDTO> loanTypeBreakdown;
    private Map<String, AmountRangeBreakdownDTO> amountRangeBreakdown;
    private LocalDate reportGeneratedAt;
}

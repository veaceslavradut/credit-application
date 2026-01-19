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
public class AnalyticsMetricsDTO {
    private Long applicationsReceived;
    private Long offersSubmitted;
    private BigDecimal conversionRate;
    private Double avgTimeToOfferHours;
    private BigDecimal avgAPR;
}

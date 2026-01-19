package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequest {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private DatePreset preset;
    
    public enum DatePreset {
        TODAY,
        LAST_7,
        LAST_30,
        LAST_YEAR,
        CUSTOM
    }
}

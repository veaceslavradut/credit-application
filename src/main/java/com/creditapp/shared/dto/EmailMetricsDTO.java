package com.creditapp.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EmailMetricsDTO for monitoring email delivery metrics
 * Used in health check endpoint and monitoring dashboards
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMetricsDTO {
    
    private Long emailsSent;
    private Long emailsDelivered;
    private Long emailsBounced;
    private Long emailsFailed;
    private Double successRate;
    private Double failureRate;
}
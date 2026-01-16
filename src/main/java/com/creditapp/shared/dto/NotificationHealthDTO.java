package com.creditapp.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NotificationHealthDTO for notification system health check
 * Provides status and metrics for monitoring
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationHealthDTO {
    
    private String status; // UP, DEGRADED, DOWN
    private Boolean sendgridConnected;
    private Boolean queueConnected;
    private LocalDateTime lastEmailSent;
    private Long emailsSentLastHour;
    private Double failureRate;
}

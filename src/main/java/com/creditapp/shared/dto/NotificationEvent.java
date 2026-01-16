package com.creditapp.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * NotificationEvent DTO for message queue
 * Represents an email notification event to be processed asynchronously
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String eventType;
    private String recipientEmail;
    private String templateName;
    private Map<String, String> variables;
    private LocalDateTime timestamp;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
}
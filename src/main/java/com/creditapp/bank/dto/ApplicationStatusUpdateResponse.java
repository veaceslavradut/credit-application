package com.creditapp.bank.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusUpdateResponse {
    private UUID applicationId;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime changedAt;
    private ApplicationQueueItem updatedApplication;
}
package com.creditapp.borrower.dto;

import com.creditapp.borrower.model.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data transfer object for ApplicationHistory entity.
 * Used for API responses showing status transitions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistoryDTO {
    private ApplicationStatus oldStatus;
    private ApplicationStatus newStatus;
    private LocalDateTime changedAt;
    private UUID changedByUserId;
    private String changeReason;
}

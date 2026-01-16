package com.creditapp.borrower.dto;

import com.creditapp.borrower.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a status transition in an application's history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTransitionDTO {
    private ApplicationStatus oldStatus;
    private ApplicationStatus newStatus;
    private LocalDateTime changedAt;
    private UUID changedByUserId;
    private String changedByName;
    private String reason;
}

package com.creditapp.borrower.dto;

import com.creditapp.borrower.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for application status and status history timeline.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusDTO {
    private UUID applicationId;
    private ApplicationStatus currentStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private List<StatusTransitionDTO> statusHistory;
    private Integer progressionPercentage;
}

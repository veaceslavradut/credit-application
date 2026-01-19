package com.creditapp.bank.dto;

import com.creditapp.borrower.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclineApplicationResponse {
    private UUID applicationId;
    private ApplicationStatus status;
    private LocalDateTime declinedAt;
    private String reason;
}
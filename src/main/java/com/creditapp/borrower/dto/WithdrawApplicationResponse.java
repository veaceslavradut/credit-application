package com.creditapp.borrower.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for withdrawn application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawApplicationResponse {
    private UUID id;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime withdrawnAt;
    private String withdrawalReason;
    private String message;
}

package com.creditapp.borrower.dto;

import com.creditapp.borrower.model.ApplicationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data transfer object for Application entity.
 * Used for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private UUID id;
    private String loanType;
    private BigDecimal loanAmount;
    private Integer loanTermMonths;
    private String currency;
    private String ratePreference;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}

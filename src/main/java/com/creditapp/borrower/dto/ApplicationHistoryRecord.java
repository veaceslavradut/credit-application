package com.creditapp.borrower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistoryRecord {
    private UUID applicationId;
    private String referenceNumber;
    private String status;
    private BigDecimal loanAmount;
    private Integer termMonths;
    private String loanPurpose;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime closedAt;
    private Integer offerCount;
    private BigDecimal bestAPR;
    private UUID selectedOfferId;
    private UUID finalAcceptedOfferId;
    private String expirationStatus;
}

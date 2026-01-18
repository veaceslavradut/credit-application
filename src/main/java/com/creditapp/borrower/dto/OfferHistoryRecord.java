package com.creditapp.borrower.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OfferHistoryRecord {
    private UUID offerId;
    private UUID applicationId;
    private String bankName;
    private BigDecimal apr;
    private BigDecimal monthlyPayment;
    private BigDecimal totalCost;
    private BigDecimal originationFee;
    private BigDecimal insuranceCost;
    private Integer termMonths;
    private Integer validityPeriodDays;
    private LocalDateTime expiresAt;
    private String offerStatus;
    private LocalDateTime offerReceivedAt;
    private LocalDateTime borrowerSelectedAt;
    private LocalDateTime finalAcceptedAt;
}

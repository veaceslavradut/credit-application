package com.creditapp.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationQueueItem {
    private UUID applicationId;
    private String referenceNumber;

    private String borrowerName;
    private String borrowerEmail;
    private String borrowerPhone;

    private BigDecimal loanAmount;
    private Integer termMonths;
    private BigDecimal selectedOfferAPR;
    private BigDecimal selectedOfferMonthlyPayment;

    /** Application status (e.g., OFFER_ACCEPTED, DOCUMENTS_SUBMITTED, APPROVED, FUNDED, REJECTED) */
    private String status;

    private LocalDateTime receivedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime lastUpdatedAt;

    /** documents status: none, submitted, verified */
    private String documentsStatus;
    /** approval status: pending, approved, rejected */
    private String approvalStatus;

    private List<String> actionItems;
}

package com.creditapp.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationQueueRequest {
    private Integer limit;
    private Integer offset;

    private String status; // filter by application status

    private BigDecimal aprMin;
    private BigDecimal aprMax;

    private BigDecimal loanAmountMin;
    private BigDecimal loanAmountMax;

    private LocalDateTime dateRangeStart;
    private LocalDateTime dateRangeEnd;

    private String sortBy;    // submittedAt | apr | loanAmount | status
    private String sortOrder; // asc | desc
}

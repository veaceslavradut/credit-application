package com.creditapp.borrower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistoryRequest {
    private String status;
    private LocalDateTime dateRangeStart;
    private LocalDateTime dateRangeEnd;
    private BigDecimal loanAmountMin;
    private BigDecimal loanAmountMax;
    private Integer limit;
    private Integer offset;
    private String sortBy;
}

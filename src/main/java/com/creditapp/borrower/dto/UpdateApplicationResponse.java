package com.creditapp.borrower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationResponse {

    private UUID id;
    private String loanType;
    private BigDecimal loanAmount;
    private Integer loanTermMonths;
    private String currency;
    private String ratePreference;
    private String status;
    private Long version;
    private LocalDateTime updatedAt;
    private List<String> editedFields;
}
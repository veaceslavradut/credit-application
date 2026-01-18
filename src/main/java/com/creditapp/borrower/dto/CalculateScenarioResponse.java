package com.creditapp.borrower.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculateScenarioResponse {
    
    private BigDecimal loanAmount;
    private Integer termMonths;
    private BigDecimal apr;
    private BigDecimal monthlyPayment;
    private BigDecimal totalCost;
    private BigDecimal originationFee;
    private BigDecimal insuranceCost;
    
    private UUID bankId;
    private String bankName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime calculatedAt;
    
    @Builder.Default
    private String disclaimer = "This is a preliminary calculation based on current rates. Actual rates and payments may vary based on final underwriting review.";
}
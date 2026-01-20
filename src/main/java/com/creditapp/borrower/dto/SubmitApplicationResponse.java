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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitApplicationResponse {
    private UUID id;
    
    @JsonProperty("loanType")
    private String loanType;
    
    @JsonProperty("loanAmount")
    private BigDecimal loanAmount;
    
    @JsonProperty("loanTermMonths")
    private Integer loanTermMonths;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("ratePreference")
    private String ratePreference;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("submittedAt")
    private LocalDateTime submittedAt;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("version")
    private Long version;
    
    @JsonProperty("message")
    private String message;
}
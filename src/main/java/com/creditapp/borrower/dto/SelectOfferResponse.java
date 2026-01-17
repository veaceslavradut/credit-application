package com.creditapp.borrower.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectOfferResponse {

    @JsonProperty("selectedOfferId")
    private UUID selectedOfferId;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("apr")
    private BigDecimal apr;

    @JsonProperty("monthlyPayment")
    private BigDecimal monthlyPayment;

    @JsonProperty("totalCost")
    private BigDecimal totalCost;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    @JsonProperty("nextSteps")
    private List<String> nextSteps;

    @JsonProperty("message")
    private String message;
}
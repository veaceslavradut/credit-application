package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for consent details in application review panel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentDetailsDTO {
    private Integer consentNumber;
    private String consentText;
    private Boolean signed;
    private String borrowerSignature;
    private LocalDateTime signedAt;
}

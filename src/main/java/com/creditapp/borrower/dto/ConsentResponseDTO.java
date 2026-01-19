package com.creditapp.borrower.dto;

import com.creditapp.shared.model.ConsentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentResponseDTO {
    
    private ConsentType consentType;
    private LocalDateTime consentedAt;
    private LocalDateTime withdrawnAt;
    private Integer version;
    private Boolean currentlyConsented;
}
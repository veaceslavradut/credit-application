package com.creditapp.borrower.dto;

import com.creditapp.shared.model.ConsentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentWithdrawalRequest {
    
    private ConsentType consentType;
}
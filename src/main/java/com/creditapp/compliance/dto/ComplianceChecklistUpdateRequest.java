package com.creditapp.compliance.dto;

import com.creditapp.shared.model.ComplianceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceChecklistUpdateRequest {
    
    private ComplianceStatus status;
    private String notes;
}
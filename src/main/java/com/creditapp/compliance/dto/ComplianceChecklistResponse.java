package com.creditapp.compliance.dto;

import com.creditapp.shared.model.ComplianceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceChecklistResponse {
    
    private List<ComplianceChecklistItemResponse> items;
    private ComplianceStatus overallStatus;
    private int redCount;
    private int yellowCount;
    private int greenCount;
    private String lastUpdateDate;
}
package com.creditapp.compliance.dto;

import com.creditapp.shared.model.ComplianceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceChecklistItemResponse {
    
    private UUID id;
    private String itemName;
    private String description;
    private ComplianceStatus status;
    private String gdprArticles;
    private String evidence;
    private LocalDateTime lastReviewedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
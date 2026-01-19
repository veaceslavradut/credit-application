package com.creditapp.shared.dto;

import com.creditapp.shared.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for listing legal documents
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalDocumentListDTO {
    private DocumentType documentType;
    private Integer currentVersion;
    private LocalDateTime lastUpdatedAt;
}
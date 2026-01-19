package com.creditapp.shared.dto;

import com.creditapp.shared.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for legal document responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalDocumentResponse {
    private UUID id;
    private DocumentType documentType;
    private Integer version;
    private String content;
    private String language;
    private LocalDateTime publishedAt;
    private String contentHash;
}
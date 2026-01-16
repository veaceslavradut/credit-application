package com.creditapp.borrower.dto;

import com.creditapp.borrower.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for document information.
 * Read-only response DTO for document details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {

    private UUID id;
    private UUID applicationId;
    private DocumentType documentType;
    private String originalFilename;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private UUID uploadedByUserId;
}

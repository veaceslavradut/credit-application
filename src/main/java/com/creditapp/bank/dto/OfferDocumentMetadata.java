package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadata DTO for offer documents.
 * Contains all document information and download URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDocumentMetadata {
    
    private UUID documentId;
    private UUID offerId;
    private String documentType;
    private String fileName;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private UUID uploadedByOfficerId;
    private String uploadedByOfficerName;
    private String description;
    
    // Virus scan status: PENDING, CLEAN, INFECTED
    private String virusScanStatus;
    
    // Pre-signed URL for downloading (expires in 24 hours)
    private String downloadUrl;
}
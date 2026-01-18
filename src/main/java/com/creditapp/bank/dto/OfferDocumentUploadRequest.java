package com.creditapp.bank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for uploading an offer-related document.
 * Supports TERMS_CONDITIONS, FEE_SCHEDULE, DISCLOSURE, TRUTH_IN_LENDING, CUSTOM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDocumentUploadRequest {
    
    @NotNull(message = "Offer ID is required")
    private java.util.UUID offerId;
    
    @NotNull(message = "Document type is required")
    private String documentType;
    
    @NotNull(message = "File is required")
    private MultipartFile file;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}
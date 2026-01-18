package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for retrieving list of offer documents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDocumentsListResponse {
    
    private UUID offerId;
    private List<OfferDocumentMetadata> documents;
    private Integer totalCount;
    private LocalDateTime retrievedAt;
}
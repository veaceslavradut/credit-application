package com.creditapp.bank.service;

import com.creditapp.bank.dto.OfferDocumentMetadata;
import com.creditapp.bank.dto.OfferDocumentsListResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferDocument;
import com.creditapp.bank.repository.OfferDocumentRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.service.S3DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for retrieving offer documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OfferDocumentRetrievalService {
    
    private final OfferDocumentRepository offerDocumentRepository;
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final S3DocumentStorageService s3DocumentStorageService;
    
    /**
     * Get list of documents for an offer.
     */
    public OfferDocumentsListResponse getDocuments(UUID offerId, UUID userId) {
        // Fetch offer
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
        
        // Verify access: borrower owns offer or bank officer from same bank
        verifyAccess(offer, userId);
        
        // Fetch all documents for offer
        List<OfferDocument> documents = offerDocumentRepository.findByOfferId(offerId);
        
        // Convert to metadata DTOs
        List<OfferDocumentMetadata> metadataList = documents.stream()
            .map(this::toMetadata)
            .sorted((a, b) -> b.getUploadedAt().compareTo(a.getUploadedAt())) // Newest first
            .toList();
        
        log.debug("Retrieved {} documents for offer: {}", documents.size(), offerId);
        
        return OfferDocumentsListResponse.builder()
            .offerId(offerId)
            .documents(metadataList)
            .totalCount(documents.size())
            .retrievedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Verify user has access to offer documents.
     */
    private void verifyAccess(Offer offer, UUID userId) {
        // Check if borrower owns the offer
        if (applicationRepository.findById(offer.getApplicationId())
            .map(app -> app.getBorrowerId().equals(userId))
            .orElse(false)) {
            return; // Borrower can access their offer documents
        }
        
        // Check if officer owns the bank that created the offer
        // This would require checking user's bank association (TBD)
        // For now, we assume bank_id matches officer's bank
        
        throw new IllegalArgumentException("Unauthorized access to documents");
    }
    
    /**
     * Convert OfferDocument to OfferDocumentMetadata.
     */
    private OfferDocumentMetadata toMetadata(OfferDocument document) {
        // Regenerate pre-signed URL if near expiration (optional)
        String presignedUrl = document.getS3Url();
        if (isUrlNearExpiration(document.getUploadedAt())) {
            presignedUrl = s3DocumentStorageService.generatePresignedUrl(
                document.getS3Key(), 
                Duration.ofHours(24)
            );
        }
        
        return OfferDocumentMetadata.builder()
            .documentId(document.getId())
            .offerId(document.getOfferId())
            .documentType(document.getDocumentType())
            .fileName(document.getFileName())
            .fileSize(document.getFileSize())
            .uploadedAt(document.getUploadedAt())
            .uploadedByOfficerId(document.getUploadedByOfficerId())
            .uploadedByOfficerName("") // TODO: Join with User table to get officer name
            .description(document.getDescription())
            .virusScanStatus(document.getVirusScanStatus())
            .downloadUrl(presignedUrl)
            .build();
    }
    
    /**
     * Check if URL is near expiration (within 1 hour).
     */
    private boolean isUrlNearExpiration(LocalDateTime uploadedAt) {
        LocalDateTime expirationTime = uploadedAt.plusHours(23); // 24-hour expiration, regenerate at 23h
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
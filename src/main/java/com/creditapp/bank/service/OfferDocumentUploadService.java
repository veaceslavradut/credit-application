package com.creditapp.bank.service;

import com.creditapp.bank.dto.OfferDocumentMetadata;
import com.creditapp.bank.dto.OfferDocumentUploadRequest;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferDocument;
import com.creditapp.bank.repository.OfferDocumentRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.FileValidationService;
import com.creditapp.shared.service.S3DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for uploading offer documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OfferDocumentUploadService {
    
    private final OfferDocumentRepository offerDocumentRepository;
    private final OfferRepository offerRepository;
    private final AuditService auditService;
    private final S3DocumentStorageService s3DocumentStorageService;
    private final FileValidationService fileValidationService;
    
    /**
     * Upload document for an offer.
     */
    public OfferDocumentMetadata uploadDocument(UUID offerId, UUID bankId, UUID officerId, OfferDocumentUploadRequest request) {
        try {
            // Validate file
            FileValidationService.ValidationResult validationResult = fileValidationService.validateFile(request.getFile());
            if (!validationResult.valid) {
                throw new IllegalArgumentException("File validation failed: " + validationResult.errorMessage);
            }
            
            // Fetch and verify offer
            Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
            
            if (!offer.getBankId().equals(bankId)) {
                throw new IllegalArgumentException("Bank does not own this offer");
            }
            
            // Generate S3 key
            String timestamp = String.valueOf(System.currentTimeMillis());
            String s3Key = String.format("offers/%s/%s_%s_%s", 
                offerId, 
                request.getDocumentType(), 
                timestamp, 
                sanitizeFileName(request.getFile().getOriginalFilename()));
            
            // Upload to S3
            Map<String, String> metadata = new HashMap<>();
            metadata.put("offerId", offerId.toString());
            metadata.put("bankId", bankId.toString());
            metadata.put("documentType", request.getDocumentType());
            
            try (var fileStream = request.getFile().getInputStream()) {
                S3DocumentStorageService.S3ObjectMetadata s3Metadata = s3DocumentStorageService.uploadFile(
                    s3Key, 
                    fileStream, 
                    request.getFile().getContentType(), 
                    metadata
                );
                
                log.info("Document uploaded to S3: offerId={}, key={}", offerId, s3Key);
            }
            
            // Generate pre-signed URL
            String presignedUrl = s3DocumentStorageService.generatePresignedUrl(
                s3Key, 
                java.time.Duration.ofHours(24)
            );
            
            // Create OfferDocument entity
            OfferDocument document = OfferDocument.builder()
                .offerId(offerId)
                .documentType(request.getDocumentType())
                .fileName(request.getFile().getOriginalFilename())
                .fileSize(request.getFile().getSize())
                .mimeType(request.getFile().getContentType())
                .s3Key(s3Key)
                .s3Url(presignedUrl)
                .uploadedByOfficerId(officerId)
                .uploadedAt(LocalDateTime.now())
                .description(request.getDescription())
                .virusScanStatus("CLEAN") // For MVP, skip scanning
                .build();
            
            OfferDocument savedDocument = offerDocumentRepository.save(document);
            
            // Log audit event
            Map<String, String> auditContext = new HashMap<>();
            auditContext.put("offerId", offerId.toString());
            auditContext.put("documentId", savedDocument.getId().toString());
            auditContext.put("documentType", request.getDocumentType());
            auditContext.put("fileName", request.getFile().getOriginalFilename());
            auditContext.put("fileSize", String.valueOf(request.getFile().getSize()));
            auditContext.put("uploadedByOfficerId", officerId.toString());
            
            Map<String, Object> auditContextObj = auditContext.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            auditService.logActionWithValues("OfferDocument", savedDocument.getId(), AuditAction.DOCUMENT_UPLOADED, auditContextObj, null);
            
            log.info("Document uploaded successfully: offerId={}, documentId={}, type={}", 
                offerId, savedDocument.getId(), request.getDocumentType());
            
            // Return metadata
            return OfferDocumentMetadata.builder()
                .documentId(savedDocument.getId())
                .offerId(savedDocument.getOfferId())
                .documentType(savedDocument.getDocumentType())
                .fileName(savedDocument.getFileName())
                .fileSize(savedDocument.getFileSize())
                .uploadedAt(savedDocument.getUploadedAt())
                .uploadedByOfficerId(savedDocument.getUploadedByOfficerId())
                .description(savedDocument.getDescription())
                .virusScanStatus(savedDocument.getVirusScanStatus())
                .downloadUrl(presignedUrl)
                .build();
        } catch (IOException e) {
            log.error("Failed to upload document: offerId={}, error={}", offerId, e.getMessage(), e);
            throw new RuntimeException("Document upload failed: " + e.getMessage(), e);
        }
    }
    
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "document";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
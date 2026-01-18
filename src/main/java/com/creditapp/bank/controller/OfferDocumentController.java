package com.creditapp.bank.controller;

import com.creditapp.bank.dto.OfferDocumentMetadata;
import com.creditapp.bank.dto.OfferDocumentUploadRequest;
import com.creditapp.bank.dto.OfferDocumentsListResponse;
import com.creditapp.bank.service.OfferDocumentDownloadService;
import com.creditapp.bank.service.OfferDocumentRetrievalService;
import com.creditapp.bank.service.OfferDocumentUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST controller for offer document management.
 */
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Slf4j
public class OfferDocumentController {
    
    private final OfferDocumentUploadService uploadService;
    private final OfferDocumentRetrievalService retrievalService;
    private final OfferDocumentDownloadService downloadService;
    
    /**
     * Upload a document for an offer.
     * POST /api/offers/{offerId}/documents
     */
    @PostMapping("/{offerId}/documents")
    @PreAuthorize("hasAuthority('BANK_OFFICER')")
    public ResponseEntity<OfferDocumentMetadata> uploadDocument(
            @PathVariable UUID offerId,
            @RequestParam UUID bankId,
            @RequestParam UUID officerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String description) {
        
        try {
            OfferDocumentUploadRequest request = OfferDocumentUploadRequest.builder()
                .offerId(offerId)
                .file(file)
                .documentType(documentType != null ? documentType : "CUSTOM")
                .description(description)
                .build();
            
            OfferDocumentMetadata metadata = uploadService.uploadDocument(offerId, bankId, officerId, request);
            
            log.info("Document uploaded successfully: offerId={}, documentId={}", offerId, metadata.getDocumentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(metadata);
        } catch (IllegalArgumentException e) {
            log.warn("Document upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Document upload error: offerId={}, error={}", offerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get list of documents for an offer.
     * GET /api/offers/{offerId}/documents
     */
    @GetMapping("/{offerId}/documents")
    @PreAuthorize("hasAuthority('BORROWER') or hasAuthority('BANK_OFFICER')")
    public ResponseEntity<OfferDocumentsListResponse> getDocuments(@PathVariable UUID offerId) {
        try {
            UUID userId = getCurrentUserId();
            OfferDocumentsListResponse response = retrievalService.getDocuments(offerId, userId);
            
            log.info("Documents retrieved: offerId={}, count={}", offerId, response.getTotalCount());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to retrieve documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Document retrieval error: offerId={}, error={}", offerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Download a document.
     * GET /api/offers/{offerId}/documents/{documentId}/download
     */
    @GetMapping("/{offerId}/documents/{documentId}/download")
    @PreAuthorize("hasAuthority('BORROWER') or hasAuthority('BANK_OFFICER')")
    public ResponseEntity<?> downloadDocument(
            @PathVariable UUID offerId,
            @PathVariable UUID documentId) {
        try {
            UUID userId = getCurrentUserId();
            String downloadUrl = downloadService.getDownloadUrl(offerId, documentId, userId);
            
            log.info("Document download URL generated: documentId={}, offerId={}", documentId, offerId);
            
            // Return redirect to S3 presigned URL or file stream
            return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(new DownloadResponse(downloadUrl));
        } catch (IllegalArgumentException e) {
            log.warn("Document download error: {}", e.getMessage());
            if (e.getMessage().contains("infected")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Document is infected");
            } else if (e.getMessage().contains("pending")) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Document scan pending");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }
        } catch (Exception e) {
            log.error("Document download error: documentId={}, offerId={}, error={}", documentId, offerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Extract current user ID from SecurityContext.
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // TODO: Extract UUID from UserDetails
            return UUID.randomUUID(); // Placeholder
        }
        throw new IllegalArgumentException("User not authenticated");
    }
    
    /**
     * Response DTO for download.
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DownloadResponse {
        private String downloadUrl;
    }
}
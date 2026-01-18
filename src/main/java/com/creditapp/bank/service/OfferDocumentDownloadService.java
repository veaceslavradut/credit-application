package com.creditapp.bank.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferDocument;
import com.creditapp.bank.repository.OfferDocumentRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for downloading offer documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfferDocumentDownloadService {
    
    private final OfferDocumentRepository offerDocumentRepository;
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditService auditService;
    
    /**
     * Get download URL for a document.
     */
    @Transactional(readOnly = true)
    public String getDownloadUrl(UUID offerId, UUID documentId, UUID userId) {
        // Fetch document and verify ownership
        OfferDocument document = offerDocumentRepository.findByIdAndOfferId(documentId, offerId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        // Fetch offer
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        
        // Verify access: borrower owns offer or bank officer from same bank
        verifyAccess(offer, userId);
        
        // Check virus scan status
        if ("INFECTED".equals(document.getVirusScanStatus())) {
            log.warn("Attempted to download infected document: documentId={}, userId={}", documentId, userId);
            throw new IllegalArgumentException("Document is infected and cannot be downloaded");
        }
        
        if ("PENDING".equals(document.getVirusScanStatus())) {
            log.info("Document scan pending: documentId={}, userId={}", documentId, userId);
            throw new IllegalArgumentException("Document is pending virus scan, please try again shortly");
        }
        
        // Log audit event
        Map<String, String> auditContext = new HashMap<>();
        auditContext.put("offerId", offerId.toString());
        auditContext.put("documentId", documentId.toString());
        auditContext.put("downloadedByUserId", userId.toString());
        auditContext.put("documentType", document.getDocumentType());
        
        Map<String, Object> auditContextObj = auditContext.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        auditService.logActionWithValues("OfferDocument", documentId, AuditAction.DOCUMENT_DOWNLOADED, auditContextObj, null);
        
        log.info("Document download initiated: documentId={}, offerId={}, userId={}", documentId, offerId, userId);
        
        return document.getS3Url();
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
        
        throw new IllegalArgumentException("Unauthorized access to document");
    }
}
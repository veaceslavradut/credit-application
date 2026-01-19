package com.creditapp.shared.controller;

import com.creditapp.shared.dto.LegalDocumentResponse;
import com.creditapp.shared.dto.UpdateLegalDocumentRequest;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.service.LegalDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for legal documents (Privacy Policy, Terms of Service)
 */
@RestController
@RequestMapping("/api/legal")
@RequiredArgsConstructor
@Slf4j
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    /**
     * GET /api/legal/privacy-policy
     * Retrieve the current privacy policy (public endpoint)
     */
    @GetMapping("/privacy-policy")
    public ResponseEntity<LegalDocumentResponse> getPrivacyPolicy(
            @RequestParam(defaultValue = "en") String language) {
        log.info("Retrieving privacy policy in language: {}", language);
        LegalDocumentResponse response = legalDocumentService.getPublishedDocument(
            DocumentType.PRIVACY_POLICY, language);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/legal/terms-of-service
     * Retrieve the current terms of service (public endpoint)
     */
    @GetMapping("/terms-of-service")
    public ResponseEntity<LegalDocumentResponse> getTermsOfService(
            @RequestParam(defaultValue = "en") String language) {
        log.info("Retrieving terms of service in language: {}", language);
        LegalDocumentResponse response = legalDocumentService.getPublishedDocument(
            DocumentType.TERMS_OF_SERVICE, language);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/legal/{documentType}
     * Update a legal document (admin-only endpoint)
     */
    @PutMapping("/{documentType}")
    @PreAuthorize("hasAuthority('COMPLIANCE_OFFICER') or hasAuthority('ADMIN')")
    public ResponseEntity<LegalDocumentResponse> updateDocument(
            @PathVariable String documentType,
            @Valid @RequestBody UpdateLegalDocumentRequest request) {
        
        log.info("Updating {} document. Material change: {}", documentType, request.getIsMaterialChange());
        
        DocumentType type = DocumentType.valueOf(documentType.toUpperCase());
        LegalDocumentResponse response = legalDocumentService.updateDocument(
            type,
            request.getContent(),
            request.getIsMaterialChange(),
            null // Will be populated by security context in actual implementation
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/legal/all-versions/{documentType}
     * Get all versions of a specific legal document (admin-only)
     */
    @GetMapping("/all-versions/{documentType}")
    @PreAuthorize("hasAuthority('COMPLIANCE_OFFICER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<LegalDocumentResponse>> getAllVersions(
            @PathVariable String documentType) {
        
        log.info("Retrieving all versions of {}", documentType);
        DocumentType type = DocumentType.valueOf(documentType.toUpperCase());
        List<LegalDocumentResponse> versions = legalDocumentService.getAllVersions(type);
        
        return ResponseEntity.ok(versions);
    }
}
package com.creditapp.shared.controller;

import com.creditapp.shared.model.Document;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.service.DocumentService;
import com.creditapp.shared.service.ESignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Document Management REST Controller
 * 
 * Endpoints:
 * - GET /documents - List documents for application
 * - POST /documents/{id}/sign - Send for e-signature (Phase 1: 501 Not Implemented)
 * - GET /documents/{id}/signature-status - Check signature status
 */
@RestController
@RequestMapping("/api/borrower/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {
    
    private final DocumentService documentService;
    private final ESignatureService eSignatureService;
    
    /**
     * GET /api/borrower/documents?applicationId={id}
     * List all documents for an application
     */
    @GetMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<List<Document>> listDocuments(
            @RequestParam UUID applicationId,
            Authentication authentication) {
        log.info("Listing documents for application {}", applicationId);
        
        List<Document> documents = documentService.findByApplicationId(applicationId);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * GET /api/borrower/documents/{id}
     * Get single document details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<Document> getDocument(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Retrieving document {}", id);
        
        Document document = documentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        return ResponseEntity.ok(document);
    }
    
    /**
     * POST /api/borrower/documents/{id}/sign
     * Send document for e-signature
     * 
     * Phase 1: Returns 501 Not Implemented
     * Phase 2: Integrates with DocuSign
     */
    @PostMapping("/{id}/sign")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<?> sendForSignature(
            @PathVariable UUID id,
            @RequestParam(required = false) String signerEmail,
            Authentication authentication) {
        log.info("Phase 1 stub: sendForSignature called for document {}", id);
        
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body("E-signature functionality not yet implemented (Phase 2)");
    }
    
    /**
     * GET /api/borrower/documents/{documentId}/signature-status
     * Get signature status
     */
    @GetMapping("/{documentId}/signature-status")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<?> getSignatureStatus(
            @PathVariable UUID documentId,
            Authentication authentication) {
        log.info("Retrieving signature status for document {}", documentId);
        
        try {
            // Placeholder: Would query signature log
            return ResponseEntity.ok().body("{\"status\": \"PENDING\", \"message\": \"Signature pending\"}");
        } catch (Exception e) {
            log.error("Error retrieving signature status", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving signature status");
        }
    }
    
    /**
     * GET /api/borrower/applications/{applicationId}/documents
     * Alternative endpoint: List documents by application
     */
    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<List<Document>> listApplicationDocuments(
            @PathVariable UUID applicationId) {
        log.info("Listing documents for application {}", applicationId);
        
        List<Document> documents = documentService.findByApplicationId(applicationId);
        return ResponseEntity.ok(documents);
    }
}
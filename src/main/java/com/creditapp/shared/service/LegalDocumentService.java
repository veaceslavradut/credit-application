package com.creditapp.shared.service;

import com.creditapp.shared.dto.LegalDocumentListDTO;
import com.creditapp.shared.dto.LegalDocumentResponse;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.model.LegalDocument;
import com.creditapp.shared.model.LegalStatus;
import com.creditapp.shared.repository.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing legal documents with versioning and audit logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final AuditService auditService;

    /**
     * Get the latest published legal document
     */
    public LegalDocumentResponse getPublishedDocument(DocumentType type, String language) {
        log.info("Retrieving published {} document in language: {}", type.getDisplayName(), language);
        
        LegalDocument document = legalDocumentRepository
            .findLatestPublishedByType(type, language)
            .orElseThrow(() -> {
                log.warn("Published {} document not found for language: {}", type.getDisplayName(), language);
                return new RuntimeException("Document not found: " + type + " in " + language);
            });
        
        return mapToResponse(document);
    }

    /**
     * Update a legal document with versioning
     */
    public LegalDocumentResponse updateDocument(DocumentType type, String content, boolean isMaterialChange, UUID updatedBy) {
        log.info("Updating {} document. Material change: {}", type.getDisplayName(), isMaterialChange);
        
        // Validate content structure
        validateContentStructure(content, type);
        
        // Get next version
        List<LegalDocument> existing = legalDocumentRepository.findAllByDocumentTypeOrderByVersionDesc(type);
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;
        
        // Calculate content hash for tamper detection
        String contentHash = calculateHash(content);
        
        // Create new document
        LegalDocument document = LegalDocument.builder()
            .id(UUID.randomUUID())
            .documentType(type)
            .version(nextVersion)
            .content(content)
            .contentHash(contentHash)
            .language("en")
            .status(LegalStatus.PUBLISHED)
            .updatedBy(updatedBy)
            .build();
        
        LegalDocument saved = legalDocumentRepository.save(document);
        log.info("Document updated: {} version {}", type.getDisplayName(), nextVersion);
        
        // Log audit event
        auditService.logAction("LegalDocument", saved.getId(), com.creditapp.shared.model.AuditAction.LEGAL_DOCUMENT_UPDATED);
        
        return mapToResponse(saved);
    }

    /**
     * Get all versions of a document
     */
    public List<LegalDocumentResponse> getAllVersions(DocumentType type) {
        log.info("Retrieving all versions of {}", type.getDisplayName());
        
        List<LegalDocument> documents = legalDocumentRepository.findAllByDocumentTypeOrderByVersionDesc(type);
        return documents.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get document list for all types
     */
    public List<LegalDocumentListDTO> getDocumentList() {
        List<LegalDocumentListDTO> result = List.of();
        
        for (DocumentType type : DocumentType.values()) {
            legalDocumentRepository
                .findLatestPublishedByType(type, "en")
                .ifPresent(doc -> {
                    result.add(LegalDocumentListDTO.builder()
                        .documentType(type)
                        .currentVersion(doc.getVersion())
                        .lastUpdatedAt(doc.getUpdatedAt())
                        .build());
                });
        }
        
        return result;
    }

    /**
     * Validate content structure for required sections
     */
    private void validateContentStructure(String content, DocumentType type) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        
        String lowerContent = content.toLowerCase();
        
        if (type == DocumentType.PRIVACY_POLICY) {
            // Verify privacy policy includes required sections
            if (!lowerContent.contains("data collected") && !lowerContent.contains("collected")) {
                throw new IllegalArgumentException("Privacy policy must include 'Data Collected' section");
            }
            if (!lowerContent.contains("purpose")) {
                throw new IllegalArgumentException("Privacy policy must include 'Purpose' section");
            }
            if (!lowerContent.contains("retention") || !lowerContent.contains("3 year")) {
                throw new IllegalArgumentException("Privacy policy must include '3-year Retention' policy");
            }
            if (!lowerContent.contains("contact")) {
                throw new IllegalArgumentException("Privacy policy must include contact information");
            }
        } else if (type == DocumentType.TERMS_OF_SERVICE) {
            // Verify terms includes required sections
            if (!lowerContent.contains("marketplace")) {
                throw new IllegalArgumentException("Terms must clarify platform role as marketplace");
            }
            if (!lowerContent.contains("dispute") || !lowerContent.contains("resolution")) {
                throw new IllegalArgumentException("Terms must include dispute resolution clause");
            }
            if (!lowerContent.contains("limitation") || !lowerContent.contains("liability")) {
                throw new IllegalArgumentException("Terms must include liability limitations");
            }
        }
        
        log.info("Content structure validated for {}", type.getDisplayName());
    }

    /**
     * Calculate SHA-256 hash of content for tamper detection
     */
    private String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error calculating hash", e);
            throw new RuntimeException("Failed to calculate content hash", e);
        }
    }

    /**
     * Map LegalDocument to response DTO
     */
    private LegalDocumentResponse mapToResponse(LegalDocument document) {
        return LegalDocumentResponse.builder()
            .id(document.getId())
            .documentType(document.getDocumentType())
            .version(document.getVersion())
            .content(document.getContent())
            .language(document.getLanguage())
            .publishedAt(document.getUpdatedAt())
            .contentHash(document.getContentHash())
            .build();
    }
}
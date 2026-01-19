package com.creditapp.shared.service;

import com.creditapp.shared.dto.LegalDocumentResponse;
import com.creditapp.shared.dto.UpdateLegalDocumentRequest;
import com.creditapp.shared.model.AuditAction;
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
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing legal documents (Privacy Policy, Terms of Service)
 * with versioning, content validation, and audit trail
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final AuditService auditService;

    /**
     * Retrieve the latest published legal document by type and language
     */
    @Transactional(readOnly = true)
    public LegalDocumentResponse getPublishedDocument(DocumentType type, String language) {
        log.debug("Retrieving published {} document in language: {}", type, language);

        LegalDocument document = legalDocumentRepository.findLatestPublishedByType(type, language)
            .orElseThrow(() -> {
                log.warn("Published {} document not found for language: {}", type, language);
                return new RuntimeException("Document not found: " + type);
            });

        return mapToResponse(document);
    }

    /**
     * Update a legal document with versioning and content validation
     */
    @Transactional
    public LegalDocumentResponse updateDocument(
            DocumentType type,
            String content,
            boolean isMaterialChange,
            UUID updatedBy) {

        // Validate content structure
        validateContentStructure(content, type);

        // Get current version
        int nextVersion = legalDocumentRepository
            .findAllByDocumentTypeOrderByVersionDesc(type)
            .stream()
            .mapToInt(LegalDocument::getVersion)
            .max()
            .orElse(0) + 1;

        // Calculate content hash
        String contentHash = calculateContentHash(content);

        // Create new version
        LegalDocument document = LegalDocument.builder()
            .documentType(type)
            .version(nextVersion)
            .content(content)
            .contentHash(contentHash)
            .language("en")
            .status(LegalStatus.PUBLISHED)
            .updatedBy(updatedBy)
            .build();

        LegalDocument saved = legalDocumentRepository.save(document);
        log.info("Document updated: {} version {}, material change: {}", 
            type.getDisplayName(), nextVersion, isMaterialChange);

        // Log audit event
        auditService.logAction("LegalDocument", saved.getId(), AuditAction.LEGAL_DOCUMENT_UPDATED);

        return mapToResponse(saved);
    }

    /**
     * Retrieve all versions of a legal document
     */
    @Transactional(readOnly = true)
    public List<LegalDocumentResponse> getAllVersions(DocumentType type) {
        log.debug("Retrieving all versions of {}", type);

        return legalDocumentRepository.findAllByDocumentTypeOrderByVersionDesc(type)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    /**
     * Validate document content structure based on document type
     * Privacy Policy must include: data collected, purpose, sharing, retention, rights, contact
     * Terms of Service must include: marketplace role, obligations, liability, dispute resolution
     */
    private void validateContentStructure(String content, DocumentType type) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        String lowerContent = content.toLowerCase();

        if (type == DocumentType.PRIVACY_POLICY) {
            // Privacy Policy required sections
            if (!lowerContent.contains("data collected")) {
                throw new IllegalArgumentException("Privacy Policy must include 'Data Collected' section");
            }
            if (!lowerContent.contains("purpose")) {
                throw new IllegalArgumentException("Privacy Policy must include 'Purpose' section");
            }
            if (!lowerContent.contains("sharing") || !lowerContent.contains("disclosure")) {
                throw new IllegalArgumentException("Privacy Policy must include 'Sharing/Disclosure' section");
            }
            if (!lowerContent.contains("retention") || !lowerContent.contains("3 years")) {
                throw new IllegalArgumentException("Privacy Policy must include 'Retention' period (3 years)");
            }
            if (!lowerContent.contains("rights") || !lowerContent.contains("access") || 
                !lowerContent.contains("deletion")) {
                throw new IllegalArgumentException("Privacy Policy must include user 'Rights' section");
            }
            if (!lowerContent.contains("contact")) {
                throw new IllegalArgumentException("Privacy Policy must include 'Contact' information");
            }
            log.info("Privacy Policy validation passed");

        } else if (type == DocumentType.TERMS_OF_SERVICE) {
            // Terms of Service required sections
            if (!lowerContent.contains("marketplace")) {
                throw new IllegalArgumentException("Terms of Service must describe platform as 'Marketplace'");
            }
            if (!lowerContent.contains("obligation")) {
                throw new IllegalArgumentException("Terms of Service must include 'Obligations' section");
            }
            if (!lowerContent.contains("liability")) {
                throw new IllegalArgumentException("Terms of Service must include 'Liability' limitations");
            }
            if (!lowerContent.contains("dispute")) {
                throw new IllegalArgumentException("Terms of Service must include 'Dispute Resolution' clause");
            }
            log.info("Terms of Service validation passed");
        }
    }

    /**
     * Calculate SHA-256 hash of document content for tamper detection
     */
    private String calculateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate content hash", e);
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    /**
     * Map LegalDocument entity to LegalDocumentResponse DTO
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
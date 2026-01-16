package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.DocumentDTO;
import com.creditapp.borrower.exception.*;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationDocument;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.model.DocumentType;
import com.creditapp.borrower.repository.ApplicationDocumentRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.audit.BusinessAudit;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing document uploads for loan applications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationDocumentService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final DocumentStorageService storageService;

    // File size limits
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 50 MB

    // Allowed MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Upload a document to an application.
     */
    @Transactional
    @BusinessAudit(action = AuditAction.DOCUMENT_UPLOADED, entityType = "ApplicationDocument")
    public DocumentDTO uploadDocument(UUID applicationId, UUID borrowerId, MultipartFile file, DocumentType documentType) {
        log.info("Uploading document for application {}, borrower {}, type {}", applicationId, borrowerId, documentType);

        // Verify application exists and borrower owns it
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            log.warn("Access denied: borrower {} attempted to upload to application {}", borrowerId, applicationId);
            throw new ApplicationNotFoundException("Application not found: " + applicationId);
        }

        // Verify application status allows uploads
        if (!isApplicationEditable(application.getStatus())) {
            throw new ApplicationLockedException(
                    "Documents cannot be uploaded to applications with status: " + application.getStatus());
        }

        // Validate file
        validateFile(file);

        // Check total size limit
        long currentTotalSize = documentRepository.calculateTotalFileSizeForApplication(applicationId);
        if (currentTotalSize + file.getSize() > MAX_TOTAL_SIZE) {
            throw new FileSizeExceededException(
                    String.format("Total document size would exceed limit of %d MB", MAX_TOTAL_SIZE / (1024 * 1024)));
        }

        // Generate unique stored filename
        String storedFilename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());

        // Store file
        try {
            storageService.storeFile(file.getInputStream(), storedFilename, file.getContentType());
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to read file content", e);
        }

        // Create document entity
        ApplicationDocument document = ApplicationDocument.builder()
                .id(UUID.randomUUID())
                .applicationId(applicationId)
                .documentType(documentType)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .uploadDate(LocalDateTime.now())
                .uploadedByUserId(borrowerId)
                .build();

        document = documentRepository.save(document);

        log.info("Document uploaded successfully: {} for application {}", document.getId(), applicationId);

        return toDTO(document);
    }

    /**
     * Delete a document (soft delete).
     */
    @Transactional
    @BusinessAudit(action = AuditAction.DOCUMENT_DELETED, entityType = "ApplicationDocument")
    public void deleteDocument(UUID applicationId, UUID borrowerId, UUID documentId) {
        log.info("Deleting document {} from application {}, borrower {}", documentId, applicationId, borrowerId);

        // Verify application exists and borrower owns it
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            log.warn("Access denied: borrower {} attempted to delete document from application {}", borrowerId, applicationId);
            throw new ApplicationNotFoundException("Application not found: " + applicationId);
        }

        // Find document
        ApplicationDocument document = documentRepository.findByIdAndApplicationIdAndDeletedAtIsNull(documentId, applicationId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        // Soft delete
        document.markAsDeleted();
        documentRepository.save(document);

        log.info("Document {} marked as deleted", documentId);
    }

    /**
     * List all active documents for an application.
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> listDocuments(UUID applicationId, UUID borrowerId) {
        log.info("Listing documents for application {}, borrower {}", applicationId, borrowerId);

        // Verify application exists and borrower owns it
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            log.warn("Access denied: borrower {} attempted to list documents for application {}", borrowerId, applicationId);
            throw new ApplicationNotFoundException("Application not found: " + applicationId);
        }

        List<ApplicationDocument> documents = documentRepository.findByApplicationIdAndDeletedAtIsNull(applicationId);

        return documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if application status allows document uploads.
     */
    private boolean isApplicationEditable(ApplicationStatus status) {
        return status == ApplicationStatus.DRAFT || status == ApplicationStatus.SUBMITTED;
    }

    /**
     * Validate uploaded file.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException(
                    String.format("File size exceeds limit of %d MB", MAX_FILE_SIZE / (1024 * 1024)));
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new InvalidDocumentException(
                    "Invalid file type. Allowed types: PDF, JPG, PNG, DOC, DOCX");
        }
    }

    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Convert entity to DTO.
     */
    private DocumentDTO toDTO(ApplicationDocument document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .applicationId(document.getApplicationId())
                .documentType(document.getDocumentType())
                .originalFilename(document.getOriginalFilename())
                .fileSize(document.getFileSize())
                .uploadDate(document.getUploadDate())
                .uploadedByUserId(document.getUploadedByUserId())
                .build();
    }
}

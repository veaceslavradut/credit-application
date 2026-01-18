package com.creditapp.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for validating uploaded files.
 * Checks file size, MIME type, and extension.
 */
@Service
@Slf4j
public class FileValidationService {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "pdf", "doc", "docx", "xls", "xlsx"
    ));
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ));
    
    /**
     * Validate uploaded file.
     */
    public ValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid("File is empty");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.invalid("File size exceeds 10MB limit");
        }
        
        // Get file extension
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            return ValidationResult.invalid("File has no extension");
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult.invalid("File type not allowed: " + extension);
        }
        
        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            return ValidationResult.invalid("MIME type not allowed: " + mimeType);
        }
        
        // Verify extension matches MIME type (basic check)
        if (!isValidMimeTypeForExtension(extension, mimeType)) {
            return ValidationResult.invalid("File extension does not match MIME type");
        }
        
        log.debug("File validation passed: {} ({}bytes, {})", fileName, file.getSize(), mimeType);
        return ValidationResult.valid();
    }
    
    private boolean isValidMimeTypeForExtension(String extension, String mimeType) {
        return switch (extension) {
            case "pdf" -> mimeType.equals("application/pdf");
            case "doc" -> mimeType.equals("application/msword");
            case "docx" -> mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xls" -> mimeType.equals("application/vnd.ms-excel");
            case "xlsx" -> mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default -> false;
        };
    }
    
    /**
     * Validation result containing status and error message.
     */
    public static class ValidationResult {
        public final boolean valid;
        public final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }
}
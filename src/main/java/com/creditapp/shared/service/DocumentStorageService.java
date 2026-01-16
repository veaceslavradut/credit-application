package com.creditapp.shared.service;

import com.creditapp.borrower.exception.DocumentStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service for storing and retrieving document files.
 * Currently supports local filesystem storage.
 * Can be extended to support S3 or other cloud storage.
 */
@Service
@Slf4j
public class DocumentStorageService {

    private final Path storageLocation;

    public DocumentStorageService(@Value("${app.document-storage.path:uploads/documents}") String storagePath) {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
            log.info("Document storage initialized at: {}", this.storageLocation);
        } catch (IOException ex) {
            throw new DocumentStorageException("Could not create document storage directory", ex);
        }
    }

    /**
     * Store a file and return the storage path.
     *
     * @param fileContent    Input stream of file content
     * @param storedFilename UUID-based filename to store
     * @param mimeType       MIME type of the file
     * @return Storage path relative to storage location
     */
    public String storeFile(InputStream fileContent, String storedFilename, String mimeType) {
        try {
            if (storedFilename.contains("..")) {
                throw new DocumentStorageException("Invalid filename: " + storedFilename);
            }

            Path targetLocation = this.storageLocation.resolve(storedFilename);
            Files.copy(fileContent, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", storedFilename);
            return storedFilename;
        } catch (IOException ex) {
            throw new DocumentStorageException("Failed to store file: " + storedFilename, ex);
        }
    }

    /**
     * Delete a file from storage.
     *
     * @param storedFilename The stored filename to delete
     */
    public void deleteFile(String storedFilename) {
        try {
            Path filePath = this.storageLocation.resolve(storedFilename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", storedFilename);
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", storedFilename, ex);
            // Don't throw exception - soft delete in DB is the primary mechanism
        }
    }

    /**
     * Retrieve a file from storage.
     *
     * @param storedFilename The stored filename to retrieve
     * @return InputStream of file content
     */
    public InputStream getFile(String storedFilename) {
        try {
            Path filePath = this.storageLocation.resolve(storedFilename).normalize();
            if (!Files.exists(filePath)) {
                throw new DocumentStorageException("File not found: " + storedFilename);
            }
            return Files.newInputStream(filePath);
        } catch (IOException ex) {
            throw new DocumentStorageException("Failed to retrieve file: " + storedFilename, ex);
        }
    }

    /**
     * Get the full path to storage location.
     */
    public Path getStorageLocation() {
        return storageLocation;
    }
}

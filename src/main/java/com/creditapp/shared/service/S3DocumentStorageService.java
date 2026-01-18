package com.creditapp.shared.service;

import com.creditapp.shared.config.S3Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

/**
 * Service for interacting with AWS S3 for document storage.
 */
@Service
@Slf4j
public class S3DocumentStorageService {
    
    private final S3Client s3Client;
    private final S3Configuration s3Configuration;
    
    public S3DocumentStorageService(S3Client s3Client, S3Configuration s3Configuration) {
        this.s3Client = s3Client;
        this.s3Configuration = s3Configuration;
    }
    
    /**
     * Upload file to S3.
     * @param key S3 object key
     * @param fileStream File content stream
     * @param contentType MIME type
     * @param metadata Optional metadata tags
     * @return S3 object metadata
     */
    public S3ObjectMetadata uploadFile(String key, InputStream fileStream, String contentType, Map<String, String> metadata) {
        try {
            PutObjectRequest.Builder putRequestBuilder = PutObjectRequest.builder()
                .bucket(s3Configuration.getBucketName())
                .key(key)
                .contentType(contentType)
                .serverSideEncryption(ServerSideEncryption.AES256);
            
            // Add metadata tags if provided
            if (metadata != null && !metadata.isEmpty()) {
                Tagging tagging = Tagging.builder()
                    .tagSet(metadata.entrySet().stream()
                        .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                        .toList())
                    .build();
                putRequestBuilder.tagging(tagging);
            }
            
            PutObjectRequest putRequest = putRequestBuilder.build();
            PutObjectResponse response = s3Client.putObject(putRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(fileStream, fileStream.available()));
            
            log.info("File uploaded to S3: bucket={}, key={}, etag={}", 
                s3Configuration.getBucketName(), key, response.eTag());
            
            return S3ObjectMetadata.builder()
                .bucketName(s3Configuration.getBucketName())
                .key(key)
                .etag(response.eTag())
                .versionId(response.versionId())
                .build();
        } catch (Exception e) {
            log.error("Failed to upload file to S3: key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("S3 upload failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate pre-signed URL for downloading file.
     * @param key S3 object key
     * @param expiration Duration for URL expiration
     * @return Pre-signed URL
     */
    public String generatePresignedUrl(String key, Duration expiration) {
        try {
            S3Presigner presigner = S3Presigner.builder().build();
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Configuration.getBucketName())
                .key(key)
                .build();
            
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();
            
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();
            
            log.debug("Pre-signed URL generated for key: {} (expires in {})", key, expiration);
            presigner.close();
            return url;
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for key: {}, error: {}", key, e.getMessage(), e);
            throw new RuntimeException("Pre-signed URL generation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete file from S3.
     * @param key S3 object key
     * @return true if deletion successful
     */
    public boolean deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Configuration.getBucketName())
                .key(key)
                .build();
            
            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from S3: bucket={}, key={}", s3Configuration.getBucketName(), key);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from S3: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Metadata returned from S3 upload.
     */
    public static class S3ObjectMetadata {
        public final String bucketName;
        public final String key;
        public final String etag;
        public final String versionId;
        
        public S3ObjectMetadata(String bucketName, String key, String etag, String versionId) {
            this.bucketName = bucketName;
            this.key = key;
            this.etag = etag;
            this.versionId = versionId;
        }
        
        public static S3ObjectMetadataBuilder builder() {
            return new S3ObjectMetadataBuilder();
        }
    }
    
    /**
     * Builder for S3ObjectMetadata.
     */
    public static class S3ObjectMetadataBuilder {
        private String bucketName;
        private String key;
        private String etag;
        private String versionId;
        
        public S3ObjectMetadataBuilder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }
        
        public S3ObjectMetadataBuilder key(String key) {
            this.key = key;
            return this;
        }
        
        public S3ObjectMetadataBuilder etag(String etag) {
            this.etag = etag;
            return this;
        }
        
        public S3ObjectMetadataBuilder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }
        
        public S3ObjectMetadata build() {
            return new S3ObjectMetadata(bucketName, key, etag, versionId);
        }
    }
}
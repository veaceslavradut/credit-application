package com.creditapp.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.time.Duration;

/**
 * Configuration for AWS S3 client.
 */
@Configuration
@Slf4j
public class S3Configuration {
    
    @Value("${creditapp.s3.region:us-east-1}")
    private String region;
    
    @Value("${creditapp.s3.bucket-name:loan-offers-documents}")
    private String bucketName;
    
    @Value("${creditapp.s3.encryption:AES256}")
    private String encryption;
    
    @Value("${creditapp.s3.presigned-url-expiration-hours:24}")
    private int presignedUrlExpirationHours;
    
    @Bean
    public S3Client s3Client() {
        log.info("Initializing S3 client for region: {} with bucket: {}", region, bucketName);
        
        S3ClientBuilder builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create());
        
        return builder.build();
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public String getEncryption() {
        return encryption;
    }
    
    public Duration getPresignedUrlExpiration() {
        return Duration.ofHours(presignedUrlExpirationHours);
    }
}
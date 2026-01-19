package com.creditapp.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class DataAnonymizationService {
    
    private static final int HASH_LENGTH = 8;
    private static final String ANONYMIZED_EMAIL_DOMAIN = "@deleted.local";
    private static final String ANONYMIZED_NAME_PREFIX = "Deleted User ";
    
    public String hashForAnonymity(String input) {
        if (input == null || input.isEmpty()) {
            return generateRandomHash();
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).substring(0, HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to hash input for anonymization", e);
            return generateRandomHash();
        }
    }
    
    public String anonymizeName(String originalName) {
        String hash = hashForAnonymity(originalName);
        return ANONYMIZED_NAME_PREFIX + hash;
    }
    
    public String anonymizeEmail(String originalEmail) {
        String hash = hashForAnonymity(originalEmail);
        return hash + ANONYMIZED_EMAIL_DOMAIN;
    }
    
    private String generateRandomHash() {
        return Long.toHexString(System.nanoTime()).substring(0, HASH_LENGTH);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
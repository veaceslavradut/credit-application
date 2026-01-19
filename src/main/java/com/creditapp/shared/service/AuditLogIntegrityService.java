package com.creditapp.shared.service;

import com.creditapp.shared.model.AuditLog;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class AuditLogIntegrityService {
    public String calculateHash(AuditLog entry) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = String.join("|",
                    String.valueOf(entry.getActorId()),
                    String.valueOf(entry.getAction()),
                    String.valueOf(entry.getCreatedAt()),
                    String.valueOf(entry.getEntityId())
            );
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }

    public boolean verifyIntegrity(AuditLog entry, String expectedHash) {
        return calculateHash(entry).equals(expectedHash);
    }
}

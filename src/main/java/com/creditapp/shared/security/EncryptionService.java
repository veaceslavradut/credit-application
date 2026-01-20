package com.creditapp.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption service for PII data at rest.
 * Uses local encryption keys for Phase 1.
 * Phase 2 will integrate with AWS KMS or HashiCorp Vault.
 */
@Service
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_SIZE = 256;
    
    private final SecretKey encryptionKey;
    private final SecureRandom secureRandom;

    @Value("${app.encryption.provider:local}")
    private String encryptionProvider;

    public EncryptionService(@Value("${app.encryption.local-key:}") String localKey) {
        this.secureRandom = new SecureRandom();
        
        if (localKey != null && !localKey.isEmpty()) {
            // Use provided key (base64 encoded)
            byte[] decodedKey = Base64.getDecoder().decode(localKey);
            this.encryptionKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } else {
            // Generate new key for development
            this.encryptionKey = generateKey();
            log.warn("Using generated encryption key. In production, provide app.encryption.local-key");
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Returns Base64 encoded: [IV (12 bytes)][Encrypted Data][Auth Tag (16 bytes)]
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);

            // Encrypt
            byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plainBytes);

            // Combine IV + ciphertext (includes auth tag)
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Return Base64 encoded
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts Base64 encoded ciphertext.
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }

        try {
            // Decode Base64
            byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedBytes);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec);

            // Decrypt
            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new DecryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Generates a new AES-256 key.
     */
    private SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE, secureRandom);
            SecretKey key = keyGenerator.generateKey();
            log.info("Generated new AES-256 encryption key: {}", 
                Base64.getEncoder().encodeToString(key.getEncoded()));
            return key;
        } catch (Exception e) {
            throw new EncryptionException("Failed to generate encryption key", e);
        }
    }

    public boolean isConfigured() {
        return encryptionKey != null;
    }

    public String getProvider() {
        return encryptionProvider;
    }
}
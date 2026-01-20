package com.creditapp.shared.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter for transparent encryption/decryption of PII fields.
 * Encrypts data before storing in database, decrypts after retrieval.
 */
@Converter
@Component
@RequiredArgsConstructor
@Slf4j
public class EncryptedAttributeConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return encryptionService.encrypt(attribute);
        } catch (Exception e) {
            log.error("Failed to encrypt attribute", e);
            throw new EncryptionException("Failed to encrypt attribute", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return encryptionService.decrypt(dbData);
        } catch (DecryptionException e) {
            log.error("Failed to decrypt attribute", e);
            throw e;  // Re-throw original exception
        } catch (Exception e) {
            log.error("Failed to decrypt attribute", e);
            throw new DecryptionException("Failed to decrypt attribute", e);
        }
    }
}
package com.creditapp.unit.security;

import com.creditapp.shared.security.EncryptedAttributeConverter;
import com.creditapp.shared.security.EncryptionService;
import com.creditapp.shared.security.DecryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EncryptedAttributeConverter (JPA converter).
 */
@ExtendWith(MockitoExtension.class)
class EncryptedAttributeConverterUnitTest {

    @Mock
    private EncryptionService encryptionService;

    private EncryptedAttributeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EncryptedAttributeConverter(encryptionService);
    }

    @Test
    void testConvertToDatabaseColumn_Encrypts() {
        // Given
        String plaintext = "john.doe@example.com";
        String encrypted = "base64EncryptedData==";
        when(encryptionService.encrypt(plaintext)).thenReturn(encrypted);

        // When
        String result = converter.convertToDatabaseColumn(plaintext);

        // Then
        assertThat(result).isEqualTo(encrypted);
        verify(encryptionService).encrypt(plaintext);
    }

    @Test
    void testConvertToDatabaseColumn_Null() {
        // When
        String result = converter.convertToDatabaseColumn(null);

        // Then
        assertThat(result).isNull();
        verify(encryptionService, never()).encrypt(anyString());
    }

    @Test
    void testConvertToEntityAttribute_Decrypts() {
        // Given
        String encrypted = "base64EncryptedData==";
        String decrypted = "john.doe@example.com";
        when(encryptionService.decrypt(encrypted)).thenReturn(decrypted);

        // When
        String result = converter.convertToEntityAttribute(encrypted);

        // Then
        assertThat(result).isEqualTo(decrypted);
        verify(encryptionService).decrypt(encrypted);
    }

    @Test
    void testConvertToEntityAttribute_Null() {
        // When
        String result = converter.convertToEntityAttribute(null);

        // Then
        assertThat(result).isNull();
        verify(encryptionService, never()).decrypt(anyString());
    }

    @Test
    void testConvertToEntityAttribute_DecryptionFails() {
        // Given
        String encrypted = "corruptedData";
        when(encryptionService.decrypt(encrypted))
                .thenThrow(new DecryptionException("Decryption failed"));

        // When/Then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(encrypted))
                .isInstanceOf(DecryptionException.class)
                .hasMessageContaining("Decryption failed");
    }
}
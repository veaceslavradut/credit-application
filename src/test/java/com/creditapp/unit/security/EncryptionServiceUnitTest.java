package com.creditapp.unit.security;

import com.creditapp.shared.security.EncryptionService;
import com.creditapp.shared.security.EncryptionException;
import com.creditapp.shared.security.DecryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for EncryptionService (AES-256-GCM) and BCrypt password hashing.
 */
class EncryptionServiceUnitTest {

    private EncryptionService encryptionService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService("");  // Generate key
        passwordEncoder = new BCryptPasswordEncoder(12);
    }

    @Test
    void testEncryptDecrypt_Success() {
        // Given
        String plaintext = "sensitive@email.com";

        // When
        String ciphertext = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(ciphertext);

        // Then
        assertThat(ciphertext).isNotNull();
        assertThat(ciphertext).isNotEqualTo(plaintext);  // Encrypted
        assertThat(decrypted).isEqualTo(plaintext);  // Correctly decrypted
    }

    @Test
    void testEncryptTwice_DifferentCiphertexts() {
        // Given
        String plaintext = "sensitive@email.com";

        // When
        String ciphertext1 = encryptionService.encrypt(plaintext);
        String ciphertext2 = encryptionService.encrypt(plaintext);

        // Then
        assertThat(ciphertext1).isNotEqualTo(ciphertext2);  // Different IVs
    }

    @Test
    void testEncryptNull_ReturnsNull() {
        // When
        String result = encryptionService.encrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testDecryptNull_ReturnsNull() {
        // When
        String result = encryptionService.decrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testDecryptInvalidCiphertext_ThrowsException() {
        // Given
        String invalidCiphertext = "notBase64!!!";

        // When/Then
        assertThatThrownBy(() -> encryptionService.decrypt(invalidCiphertext))
                .isInstanceOf(DecryptionException.class)
                .hasMessageContaining("Failed to decrypt data");
    }

    @Test
    void testBCryptHashPassword_DifferentFromPlaintext() {
        // Given
        String plainPassword = "MySecurePassword123!";

        // When
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        assertThat(hashedPassword).startsWith("$2a$12$");  // BCrypt format
    }

    @Test
    void testBCryptHashPassword_DifferentSalts() {
        // Given
        String plainPassword = "MySecurePassword123!";

        // When
        String hash1 = passwordEncoder.encode(plainPassword);
        String hash2 = passwordEncoder.encode(plainPassword);

        // Then
        assertThat(hash1).isNotEqualTo(hash2);  // Different salts
    }

    @Test
    void testBCryptVerifyPassword_CorrectPassword() {
        // Given
        String plainPassword = "MySecurePassword123!";
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // When
        boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    void testBCryptVerifyPassword_WrongPassword() {
        // Given
        String plainPassword = "MySecurePassword123!";
        String wrongPassword = "WrongPassword456!";
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // When
        boolean matches = passwordEncoder.matches(wrongPassword, hashedPassword);

        // Then
        assertThat(matches).isFalse();
    }

    @Test
    void testEncryptionServiceConfigured() {
        // When/Then
        assertThat(encryptionService.isConfigured()).isTrue();
        // Note: provider field is null in unit test (no @Value injection)
        // Integration test will verify actual provider value
    }
}
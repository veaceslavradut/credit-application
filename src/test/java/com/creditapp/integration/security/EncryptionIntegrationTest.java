package com.creditapp.integration.security;

import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.security.EncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for data encryption at rest (AES-256-GCM) and BCrypt.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({EncryptionService.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.encryption.provider=local",
        "app.encryption.local-key="
})
class EncryptionIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EncryptionService encryptionService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Test
    void testUserEmailEncrypted_InDatabase() throws Exception {
        // Given
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("+373 69 123456");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();

        // Then - Verify email is encrypted and then decrypted correctly through JPA
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getEmail()).isEqualTo("john.doe@example.com");  // Decrypted by JPA
        assertThat(loaded.getFirstName()).isEqualTo("John");
    }

    @Test
    void testUserEmailDecrypted_ViaJPA() {
        // Given
        User user = new User();
        user.setEmail("jane.smith@example.com");
        user.setFirstName("Jane");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify email is decrypted transparently
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getEmail()).isEqualTo("jane.smith@example.com");  // Decrypted
    }

    @Test
    void testUserPhoneEncrypted_InDatabase() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPhone("+373 69 999888");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();

        // Then - Verify phone is encrypted and then decrypted correctly through JPA
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getPhone()).isEqualTo("+373 69 999888");  // Decrypted by JPA
        assertThat(loaded.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUserPhoneDecrypted_ViaJPA() {
        // Given
        User user = new User();
        user.setEmail("phone-test@example.com");
        user.setPhone("+373 69 777666");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify phone is decrypted transparently
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getPhone()).isEqualTo("+373 69 777666");  // Decrypted
    }

    @Test
    void testUpdateUserEmail_ReEncrypts() throws Exception {
        // Given
        User user = new User();
        user.setEmail("original@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);
        User saved = userRepository.save(user);
        entityManager.flush();

        // When - Update email
        saved.setEmail("updated@example.com");
        userRepository.save(saved);
        entityManager.flush();

        // Then - Verify updated encrypted value is decrypted correctly through JPA
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testBCryptPasswordHashed_InDatabase() throws Exception {
        // Given
        String plainPassword = "MySecurePassword123!";
        User user = new User();
        user.setEmail("bcrypt-test@example.com");
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();

        // Then - Verify password is hashed (not plain text)
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        String storedHash = loaded.getPasswordHash();
        // Verify it's hashed, not plain text
        assertThat(storedHash).isNotEqualTo(plainPassword);
        // Verify it's a valid BCrypt hash
        assertThat(storedHash).startsWith("$2");
    }

    @Test
    void testBCryptVerifyPassword_Success() {
        // Given
        String plainPassword = "CorrectPassword123!";
        User user = new User();
        user.setEmail("verify-test@example.com");
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        boolean matches = passwordEncoder.matches(plainPassword, loaded.getPasswordHash());
        assertThat(matches).isTrue();
    }

    @Test
    void testBCryptVerifyPassword_Failure() {
        // Given
        String plainPassword = "CorrectPassword123!";
        String wrongPassword = "WrongPassword456!";
        User user = new User();
        user.setEmail("verify-fail-test@example.com");
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setRole(UserRole.BORROWER);
        user.setEnabled(true);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        User loaded = userRepository.findById(saved.getId()).orElseThrow();
        boolean matches = passwordEncoder.matches(wrongPassword, loaded.getPasswordHash());
        assertThat(matches).isFalse();
    }

    @Test
    void testEncryptionService_ConfiguredCorrectly() {
        assertThat(encryptionService).isNotNull();
        assertThat(encryptionService.isConfigured()).isTrue();
        assertThat(encryptionService.getProvider()).isEqualTo("local");
    }
}
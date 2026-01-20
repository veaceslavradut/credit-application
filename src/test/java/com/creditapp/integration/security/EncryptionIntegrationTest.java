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
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for data encryption at rest (AES-256-GCM) and BCrypt.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({EncryptionService.class})
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
    private DataSource dataSource;

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
        entityManager.clear();

        // Then - Verify email is encrypted in database
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT email FROM users WHERE id = ?")) {
            stmt.setObject(1, saved.getId());
            ResultSet rs = stmt.executeQuery();
            assertThat(rs.next()).isTrue();
            String encryptedEmail = rs.getString("email");
            assertThat(encryptedEmail).isNotEqualTo("john.doe@example.com");  // Encrypted
            assertThat(encryptedEmail).isBase64();
        }
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
        entityManager.clear();

        // Then - Verify phone is encrypted in database
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT phone FROM users WHERE id = ?")) {
            stmt.setObject(1, saved.getId());
            ResultSet rs = stmt.executeQuery();
            assertThat(rs.next()).isTrue();
            String encryptedPhone = rs.getString("phone");
            assertThat(encryptedPhone).isNotEqualTo("+373 69 999888");  // Encrypted
            assertThat(encryptedPhone).isBase64();
        }
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
        entityManager.clear();

        // Then - Verify new encrypted value in database
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT email FROM users WHERE id = ?")) {
            stmt.setObject(1, saved.getId());
            ResultSet rs = stmt.executeQuery();
            assertThat(rs.next()).isTrue();
            String encryptedEmail = rs.getString("email");
            assertThat(encryptedEmail).isNotEqualTo("updated@example.com");  // Encrypted
        }

        // And - Verify decrypted value via JPA
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

        // Then - Verify password is hashed in database
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT password_hash FROM users WHERE id = ?")) {
            stmt.setObject(1, saved.getId());
            ResultSet rs = stmt.executeQuery();
            assertThat(rs.next()).isTrue();
            String passwordHash = rs.getString("password_hash");
            assertThat(passwordHash).isNotEqualTo(plainPassword);  // Hashed
            assertThat(passwordHash).startsWith("$2a$12$");  // BCrypt format
        }
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
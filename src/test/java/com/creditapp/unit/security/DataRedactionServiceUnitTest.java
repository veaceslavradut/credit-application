package com.creditapp.unit.security;

import com.creditapp.shared.security.DataRedactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DataRedactionService (PII redaction for audit logs).
 */
class DataRedactionServiceUnitTest {

    private DataRedactionService redactionService;

    @BeforeEach
    void setUp() {
        redactionService = new DataRedactionService();
    }

    @Test
    void testRedactEmail_KeepsDomain() {
        // Given
        String email = "john.doe@example.com";

        // When
        String redacted = redactionService.redactEmail(email);

        // Then
        assertThat(redacted).isEqualTo("***@example.com");
    }

    @Test
    void testRedactEmail_Null() {
        // When
        String redacted = redactionService.redactEmail(null);

        // Then
        assertThat(redacted).isNull();
    }

    @Test
    void testRedactPhone_Complete() {
        // Given
        String phone = "+373 69 123456";

        // When
        String redacted = redactionService.redactPhone(phone);

        // Then
        assertThat(redacted).isEqualTo("[REDACTED]");
    }

    @Test
    void testRedactName_KeepsFirstLetter() {
        // Given
        String name = "John Doe";

        // When
        String redacted = redactionService.redactName(name);

        // Then
        assertThat(redacted).isEqualTo("J*** D***");
    }

    @Test
    void testRedactAddress_Complete() {
        // Given
        String address = "123 Main St, Chisinau";

        // When
        String redacted = redactionService.redactAddress(address);

        // Then
        assertThat(redacted).isEqualTo("[REDACTED]");
    }

    @Test
    void testRedactAuditDetails_RemovesPII() {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("email", "john.doe@example.com");
        details.put("phone", "+373 69 123456");
        details.put("firstName", "John");
        details.put("lastName", "Doe");
        details.put("address", "123 Main St");
        details.put("action", "LOGIN");
        details.put("userId", "12345");

        // When
        Map<String, Object> redacted = redactionService.redactAuditDetails(details);

        // Then
        assertThat(redacted.get("email")).isEqualTo("***@example.com");  // Redacted
        assertThat(redacted).doesNotContainKey("phone");  // Removed
        assertThat(redacted).doesNotContainKey("firstName");  // Removed
        assertThat(redacted).doesNotContainKey("lastName");  // Removed
        assertThat(redacted).doesNotContainKey("address");  // Removed
        assertThat(redacted.get("action")).isEqualTo("LOGIN");  // Kept
        assertThat(redacted.get("userId")).isEqualTo("12345");  // Kept
    }

    @Test
    void testRedactAuditDetails_RemovesPasswordFields() {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("password", "MySecurePassword123");
        details.put("passwordHash", "$2a$12$abcdef...");
        details.put("ssn", "123-45-6789");
        details.put("action", "REGISTER");

        // When
        Map<String, Object> redacted = redactionService.redactAuditDetails(details);

        // Then
        assertThat(redacted).doesNotContainKey("password");
        assertThat(redacted).doesNotContainKey("passwordHash");
        assertThat(redacted).doesNotContainKey("ssn");
        assertThat(redacted.get("action")).isEqualTo("REGISTER");
    }

    @Test
    void testRedactUrl_RemovesSensitiveParams() {
        // Given
        String url = "https://api.example.com/endpoint?password=secret123&token=abc&key=xyz&user=john";

        // When
        String redacted = redactionService.redactUrl(url);

        // Then
        assertThat(redacted).contains("password=[REDACTED]");
        assertThat(redacted).contains("token=[REDACTED]");
        assertThat(redacted).contains("key=[REDACTED]");
        assertThat(redacted).contains("user=john");  // user param not redacted
        assertThat(redacted).doesNotContain("secret123");
        assertThat(redacted).doesNotContain("abc");
        assertThat(redacted).doesNotContain("xyz");
    }

    @Test
    void testRedactAuditDetails_NullSafe() {
        // When
        Map<String, Object> redacted = redactionService.redactAuditDetails(null);

        // Then
        assertThat(redacted).isNull();
    }
}
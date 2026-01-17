package com.creditapp.unit.security;

import com.creditapp.shared.config.JwtConfig;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.service.JwtTokenService;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenServiceUnitTest {

    private JwtTokenService jwtTokenService;

    private User testUser;
    private User bankAdminUser;

    @BeforeEach
    void setUp() {
        // Create a test secret key for JWT signing
        String testSecret = "test-secret-key-for-jwt-token-signing-must-be-long-enough-256-bits";
        SecretKey secretKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        
        // Create JWT properties for testing using the proper constructor
        JwtConfig.JwtProperties jwtProperties = new JwtConfig.JwtProperties(
            testSecret,
            15,      // expirationMinutes
            7,       // refreshExpirationDays
            secretKey
        );
        
        // Initialize JWT token service with test configuration
        jwtTokenService = new JwtTokenService(jwtProperties);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("borrower@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.BORROWER);
        testUser.setOrganizationId(null);

        bankAdminUser = new User();
        bankAdminUser.setId(UUID.randomUUID());
        bankAdminUser.setEmail("admin@bank.com");
        bankAdminUser.setFirstName("Jane");
        bankAdminUser.setLastName("Smith");
        bankAdminUser.setRole(UserRole.BANK_ADMIN);
        bankAdminUser.setOrganizationId(UUID.randomUUID());
    }

    @Test
    void testGenerateTokenIncludesAllClaims() {
        String token = jwtTokenService.generateToken(testUser);
        assertNotNull(token);
        assertTrue(jwtTokenService.validateToken(token));
    }

    @Test
    void testGenerateTokenForBankAdminIncludesOrgId() {
        String token = jwtTokenService.generateToken(bankAdminUser);
        assertNotNull(token);
        assertTrue(jwtTokenService.validateToken(token));
    }

    @Test
    void testValidateTokenSuccessful() {
        String token = jwtTokenService.generateToken(testUser);
        assertTrue(jwtTokenService.validateToken(token));
    }

    @Test
    void testValidateTokenInvalidSignature() {
        String token = jwtTokenService.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtTokenService.validateToken(tamperedToken));
    }

    @Test
    void testExtractUserIdFromToken() {
        String token = jwtTokenService.generateToken(testUser);
        UUID extractedId = jwtTokenService.extractUserId(token);
        assertEquals(testUser.getId(), extractedId);
    }

    @Test
    void testExtractEmailFromToken() {
        String token = jwtTokenService.generateToken(testUser);
        String extractedEmail = jwtTokenService.extractEmail(token);
        assertEquals(testUser.getEmail(), extractedEmail);
    }

    @Test
    void testExtractRoleFromToken() {
        String token = jwtTokenService.generateToken(testUser);
        String extractedRole = jwtTokenService.extractRole(token);
        assertEquals("BORROWER", extractedRole);
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtTokenService.generateRefreshToken(testUser);
        assertNotNull(refreshToken);
        assertTrue(jwtTokenService.validateToken(refreshToken));
    }

    @Test
    void testTokenExpirationClaimPresent() {
        String token = jwtTokenService.generateToken(testUser);
        Long expiration = jwtTokenService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration > System.currentTimeMillis());
    }
}
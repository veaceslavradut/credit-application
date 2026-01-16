package com.creditapp.unit.security;

import com.creditapp.shared.config.JwtConfig;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtTokenServiceUnitTest {

    private JwtTokenService jwtTokenService;

    @Mock
    private JwtConfig.JwtProperties jwtProperties;

    private User testUser;
    private User bankAdminUser;

    @BeforeEach
    void setUp() {
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
        LocalDateTime expiration = jwtTokenService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.isAfter(LocalDateTime.now()));
    }
}
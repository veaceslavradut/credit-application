package com.creditapp.unit.auth;

import com.creditapp.auth.dto.LoginRequest;
import com.creditapp.auth.dto.LoginResponse;
import com.creditapp.auth.exception.InvalidCredentialsException;
import com.creditapp.auth.exception.BankNotActivatedException;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.auth.service.LoginService;
import com.creditapp.shared.exception.LoginRateLimitExceededException;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.JwtTokenService;
import com.creditapp.shared.util.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @Mock
    private PasswordEncoder passwordEncoder;

    private LoginService loginService;
    private User testBorrower;
    private User testBankAdmin;
    private Organization activeBank;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(userRepository, organizationRepository, 
                jwtTokenService, loginRateLimiter, passwordEncoder);

        // Setup test borrower
        testBorrower = new User();
        testBorrower.setId(UUID.randomUUID());
        testBorrower.setEmail("borrower@example.com");
        testBorrower.setPasswordHash("hashedpassword");
        testBorrower.setFirstName("John");
        testBorrower.setLastName("Doe");
        testBorrower.setRole(UserRole.BORROWER);

        // Setup test bank admin
        testBankAdmin = new User();
        testBankAdmin.setId(UUID.randomUUID());
        testBankAdmin.setEmail("admin@bank.com");
        testBankAdmin.setPasswordHash("hashedpassword");
        testBankAdmin.setFirstName("Jane");
        testBankAdmin.setLastName("Smith");
        testBankAdmin.setRole(UserRole.BANK_ADMIN);
        testBankAdmin.setOrganizationId(UUID.randomUUID());

        // Setup active bank
        activeBank = new Organization();
        activeBank.setId(testBankAdmin.getOrganizationId());
        activeBank.setName("Test Bank");
        activeBank.setStatus(BankStatus.ACTIVE);
    }

    @Test
    void testLoginWithValidBorrowerCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testBorrower.getEmail());
        request.setPassword("password123");

        when(loginRateLimiter.checkRateLimit(testBorrower.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(testBorrower.getEmail())).thenReturn(Optional.of(testBorrower));
        when(passwordEncoder.matches(request.getPassword(), testBorrower.getPasswordHash())).thenReturn(true);
        when(jwtTokenService.generateToken(testBorrower)).thenReturn("access_token");
        when(jwtTokenService.generateRefreshToken(testBorrower)).thenReturn("refresh_token");

        LoginResponse response = loginService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        verify(loginRateLimiter).clearFailedAttempts(testBorrower.getEmail());
    }

    @Test
    void testLoginWithInvalidEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(loginRateLimiter.checkRateLimit(request.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            loginService.login(request);
        });

        verify(loginRateLimiter).recordFailedAttempt(request.getEmail());
    }

    @Test
    void testLoginWithInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testBorrower.getEmail());
        request.setPassword("wrongpassword");

        when(loginRateLimiter.checkRateLimit(testBorrower.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(testBorrower.getEmail())).thenReturn(Optional.of(testBorrower));
        when(passwordEncoder.matches(request.getPassword(), testBorrower.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            loginService.login(request);
        });

        verify(loginRateLimiter).recordFailedAttempt(testBorrower.getEmail());
    }

    @Test
    void testLoginBankAdminWithActiveBank() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testBankAdmin.getEmail());
        request.setPassword("password123");

        when(loginRateLimiter.checkRateLimit(testBankAdmin.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(testBankAdmin.getEmail())).thenReturn(Optional.of(testBankAdmin));
        when(passwordEncoder.matches(request.getPassword(), testBankAdmin.getPasswordHash())).thenReturn(true);
        when(organizationRepository.findById(testBankAdmin.getOrganizationId())).thenReturn(Optional.of(activeBank));
        when(jwtTokenService.generateToken(testBankAdmin)).thenReturn("access_token");
        when(jwtTokenService.generateRefreshToken(testBankAdmin)).thenReturn("refresh_token");

        LoginResponse response = loginService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(loginRateLimiter).clearFailedAttempts(testBankAdmin.getEmail());
    }

    @Test
    void testLoginBankAdminWithInactiveBank() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testBankAdmin.getEmail());
        request.setPassword("password123");

        Organization inactiveBank = new Organization();
        inactiveBank.setStatus(BankStatus.PENDING_ACTIVATION);

        when(loginRateLimiter.checkRateLimit(testBankAdmin.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(testBankAdmin.getEmail())).thenReturn(Optional.of(testBankAdmin));
        when(passwordEncoder.matches(request.getPassword(), testBankAdmin.getPasswordHash())).thenReturn(true);
        when(organizationRepository.findById(testBankAdmin.getOrganizationId())).thenReturn(Optional.of(inactiveBank));

        assertThrows(BankNotActivatedException.class, () -> {
            loginService.login(request);
        });
    }

    @Test
    void testLoginRateLimitExceeded() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testBorrower.getEmail());
        request.setPassword("password123");

        when(loginRateLimiter.checkRateLimit(testBorrower.getEmail())).thenReturn(false);

        assertThrows(LoginRateLimitExceededException.class, () -> {
            loginService.login(request);
        });
    }

    @Test
    void testRefreshTokenWithValidToken() {
        String refreshToken = "valid_refresh_token";

        when(jwtTokenService.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenService.extractUserId(refreshToken)).thenReturn(testBorrower.getId());
        when(userRepository.findById(testBorrower.getId())).thenReturn(Optional.of(testBorrower));
        when(jwtTokenService.generateToken(testBorrower)).thenReturn("new_access_token");
        when(jwtTokenService.generateRefreshToken(testBorrower)).thenReturn("new_refresh_token");

        LoginResponse response = loginService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("new_refresh_token", response.getRefreshToken());
    }

    @Test
    void testRefreshTokenWithInvalidToken() {
        String refreshToken = "invalid_refresh_token";

        when(jwtTokenService.validateToken(refreshToken)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            loginService.refreshToken(refreshToken);
        });
    }

    @Test
    void testLogout() {
        assertDoesNotThrow(() -> {
            loginService.logout();
        });
    }
}
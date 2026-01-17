package com.creditapp.auth.service;

import com.creditapp.auth.dto.LoginRequest;
import com.creditapp.auth.dto.LoginResponse;
import com.creditapp.auth.exception.BankNotActivatedException;
import com.creditapp.auth.exception.InvalidCredentialsException;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.exception.LoginRateLimitExceededException;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.JwtTokenService;
import com.creditapp.shared.util.LoginRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final JwtTokenService jwtTokenService;
    private final LoginRateLimiter loginRateLimiter;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepository userRepository, OrganizationRepository organizationRepository,
                       JwtTokenService jwtTokenService, LoginRateLimiter loginRateLimiter, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.jwtTokenService = jwtTokenService;
        this.loginRateLimiter = loginRateLimiter;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();

        if (!loginRateLimiter.checkRateLimit(email)) {
            logger.warn("Login rate limit exceeded for email: {}", email);
            throw new LoginRateLimitExceededException("Too many failed login attempts. Please try again in 1 minute.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            loginRateLimiter.recordFailedAttempt(email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginRateLimiter.recordFailedAttempt(email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getRole() == UserRole.BANK_ADMIN) {
            Optional<Organization> orgOpt = organizationRepository.findById(Objects.requireNonNull(user.getOrganizationId()));
            if (orgOpt.isEmpty() || orgOpt.get().getStatus() != BankStatus.ACTIVE) {
                throw new BankNotActivatedException("Bank account not activated. Please check your email for activation link.");
            }
        }

        loginRateLimiter.clearFailedAttempts(email);
        String accessToken = jwtTokenService.generateToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        logger.info("User logged in successfully: {}", email);

        LoginResponse.UserDTO userDTO = new LoginResponse.UserDTO(
                user.getId(), user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName(), user.getOrganizationId());

        return new LoginResponse(accessToken, refreshToken, "Bearer", 900L, userDTO);
    }

    public LoginResponse refreshToken(String refreshToken) {
        logger.info("Refreshing token");
        if (!jwtTokenService.validateToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        UUID userId = jwtTokenService.extractUserId(refreshToken);
        Optional<User> userOpt = userRepository.findById(Objects.requireNonNull(userId));
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("User not found");
        }

        User user = userOpt.get();
        String newAccessToken = jwtTokenService.generateToken(user);
        String newRefreshToken = jwtTokenService.generateRefreshToken(user);

        LoginResponse.UserDTO userDTO = new LoginResponse.UserDTO(
                user.getId(), user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName(), user.getOrganizationId());

        return new LoginResponse(newAccessToken, newRefreshToken, "Bearer", 900L, userDTO);
    }

    public void logout() {
        logger.info("User logged out");
    }
}
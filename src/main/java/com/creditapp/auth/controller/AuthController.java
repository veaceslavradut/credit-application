package com.creditapp.auth.controller;

import com.creditapp.auth.dto.RegistrationRequest;
import com.creditapp.auth.dto.RegistrationResponse;
import com.creditapp.auth.dto.BankRegistrationRequest;
import com.creditapp.auth.dto.BankRegistrationResponse;
import com.creditapp.auth.dto.LoginRequest;
import com.creditapp.auth.dto.LoginResponse;
import com.creditapp.auth.dto.RefreshTokenRequest;
import com.creditapp.auth.service.UserRegistrationService;
import com.creditapp.auth.service.BankRegistrationService;
import com.creditapp.auth.service.LoginService;
import com.creditapp.shared.service.BankActivationEmailService;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.RequestContextService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRegistrationService userRegistrationService;
    private final BankRegistrationService bankRegistrationService;
    private final LoginService loginService;
    private final BankActivationEmailService bankActivationEmailService;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;
    private final RequestContextService requestContextService;

    public AuthController(UserRegistrationService userRegistrationService,
                          BankRegistrationService bankRegistrationService,
                          LoginService loginService,
                          BankActivationEmailService bankActivationEmailService,
                          OrganizationRepository organizationRepository,
                          AuditService auditService,
                          RequestContextService requestContextService) {
        this.userRegistrationService = userRegistrationService;
        this.bankRegistrationService = bankRegistrationService;
        this.loginService = loginService;
        this.bankActivationEmailService = bankActivationEmailService;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
        this.requestContextService = requestContextService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        logger.info("Registration request received for email: {}", request.getEmail());
        RegistrationResponse response = userRegistrationService.registerBorrower(request);
        
        // Audit user registration
        try {
            auditService.logAction("User", response.getUserId(), AuditAction.USER_REGISTERED,
                    response.getUserId(), "BORROWER");
        } catch (Exception e) {
            logger.error("Failed to audit user registration", e);
        }
        
        logger.info("User registered successfully: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register-bank")
    public ResponseEntity<BankRegistrationResponse> registerBank(@Valid @RequestBody BankRegistrationRequest request) {
        logger.info("Bank registration request received for bank: {} with email: {}", request.getBankName(), request.getContactEmail());
        try {
            BankRegistrationResponse response = bankRegistrationService.registerBank(request);
            
            // Send activation email asynchronously
            Organization savedBank = organizationRepository.findByRegistrationNumber(request.getRegistrationNumber()).orElseThrow();
            bankActivationEmailService.sendActivationEmail(
                    request.getContactEmail(),
                    request.getBankName(),
                    savedBank.getActivationToken()
            );
            
            logger.info("Bank registered successfully: {}", request.getBankName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Bank registration failed for {}: {}", request.getBankName(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateBank(@RequestParam("token") String token) {
        logger.info("Bank activation request received with token");
        
        Optional<Organization> bankOpt = organizationRepository.findByActivationToken(token);
        if (bankOpt.isEmpty()) {
            logger.warn("Invalid activation token provided");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Invalid activation token\"}");
        }

        Organization bank = bankOpt.get();
        
        // Check if already activated
        if (bank.getStatus() == BankStatus.ACTIVE) {
            logger.warn("Bank {} already activated", bank.getName());
            return ResponseEntity.badRequest().body("{\"error\": \"Bank already activated\"}");
        }

        // Check if token expired
        if (bank.getActivationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Activation token expired for bank {}", bank.getName());
            return ResponseEntity.badRequest().body("{\"error\": \"Activation token expired\"}");
        }

        // Activate bank
        bank.setStatus(BankStatus.ACTIVE);
        bank.setActivatedAt(LocalDateTime.now());
        bank.setActivationToken(null);  // One-time use token
        organizationRepository.save(bank);
        
        // Audit bank activation
        try {
            auditService.logAction("Organization", bank.getId(), AuditAction.BANK_ACTIVATED,
                    null, "SYSTEM");
        } catch (Exception e) {
            logger.error("Failed to audit bank activation", e);
        }

        logger.info("Bank {} activated successfully", bank.getName());
        String jsonResponse = String.format("{\"bankId\": \"%s\", \"bankName\": \"%s\", \"status\": \"ACTIVE\", \"message\": \"Bank activated successfully\"}", 
                bank.getId(), bank.getName());
        return ResponseEntity.ok(jsonResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for email: {}", request.getEmail());
        LoginResponse response = loginService.login(request);
        
        // Audit user login
        try {
            UUID userId = requestContextService.getCurrentUserId();
            if (userId != null) {
                auditService.logAction("User", userId, AuditAction.USER_LOGGED_IN,
                        userId, requestContextService.getCurrentUserRole());
            }
        } catch (Exception e) {
            logger.error("Failed to audit user login", e);
        }
        
        logger.info("User logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");
        LoginResponse response = loginService.refreshToken(request.getRefreshToken());
        logger.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout() {
        logger.info("Logout request received");
        
        // Audit user logout
        try {
            UUID userId = requestContextService.getCurrentUserId();
            if (userId != null) {
                auditService.logAction("User", userId, AuditAction.USER_LOGGED_OUT,
                        userId, requestContextService.getCurrentUserRole());
            }
        } catch (Exception e) {
            logger.error("Failed to audit user logout", e);
        }
        
        loginService.logout();
        logger.info("User logged out successfully");
        return ResponseEntity.noContent().build();
    }
}
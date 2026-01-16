package com.creditapp.auth.controller;

import com.creditapp.auth.dto.RegistrationRequest;
import com.creditapp.auth.dto.RegistrationResponse;
import com.creditapp.auth.dto.BankRegistrationRequest;
import com.creditapp.auth.dto.BankRegistrationResponse;
import com.creditapp.auth.service.UserRegistrationService;
import com.creditapp.auth.service.BankRegistrationService;
import com.creditapp.shared.service.BankActivationEmailService;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.repository.OrganizationRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRegistrationService userRegistrationService;
    private final BankRegistrationService bankRegistrationService;
    private final BankActivationEmailService bankActivationEmailService;
    private final OrganizationRepository organizationRepository;

    public AuthController(UserRegistrationService userRegistrationService,
                          BankRegistrationService bankRegistrationService,
                          BankActivationEmailService bankActivationEmailService,
                          OrganizationRepository organizationRepository) {
        this.userRegistrationService = userRegistrationService;
        this.bankRegistrationService = bankRegistrationService;
        this.bankActivationEmailService = bankActivationEmailService;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        logger.info("Registration request received for email: {}", request.getEmail());
        RegistrationResponse response = userRegistrationService.registerBorrower(request);
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

        logger.info("Bank {} activated successfully", bank.getName());
        String jsonResponse = String.format("{\"bankId\": \"%s\", \"bankName\": \"%s\", \"status\": \"ACTIVE\", \"message\": \"Bank activated successfully\"}", 
                bank.getId(), bank.getName());
        return ResponseEntity.ok(jsonResponse);
    }
}
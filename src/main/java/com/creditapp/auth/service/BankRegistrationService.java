package com.creditapp.auth.service;

import com.creditapp.auth.dto.BankRegistrationRequest;
import com.creditapp.auth.dto.BankRegistrationResponse;
import com.creditapp.auth.exception.DuplicateBankRegistrationException;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.service.ActivationTokenService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class BankRegistrationService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final ActivationTokenService activationTokenService;

    public BankRegistrationService(OrganizationRepository organizationRepository, UserRepository userRepository,
                                   PasswordHasher passwordHasher, ActivationTokenService activationTokenService) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.activationTokenService = activationTokenService;
    }

    public BankRegistrationResponse registerBank(BankRegistrationRequest request) {
        // Validate passwords match
        if (!request.getAdminPassword().equals(request.getAdminPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check for duplicate registration number
        if (organizationRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new DuplicateBankRegistrationException(
                    "Bank with registration number " + request.getRegistrationNumber() + " already exists"
            );
        }

        // Check for duplicate admin email
        if (userRepository.findByEmail(request.getContactEmail()).isPresent()) {
            throw new DuplicateBankRegistrationException(
                    "Email " + request.getContactEmail() + " is already registered"
            );
        }

        // Generate activation token
        String activationToken = activationTokenService.generateToken();
        LocalDateTime tokenExpiresAt = LocalDateTime.now().plus(7, ChronoUnit.DAYS);

        // Create Organization
        Organization bank = new Organization();
        bank.setId(UUID.randomUUID());
        bank.setName(request.getBankName());
        bank.setRegistrationNumber(request.getRegistrationNumber());
        bank.setStatus(BankStatus.PENDING_ACTIVATION);
        bank.setActivationToken(activationToken);
        bank.setActivationTokenExpiresAt(tokenExpiresAt);
        bank.setCountryCode("MD"); // Default to Moldova for MVP
        bank.setTaxId(request.getRegistrationNumber()); // Use registration number as tax ID for MVP
        Organization savedBank = organizationRepository.save(bank);

        // Create Admin User
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setEmail(request.getContactEmail());
        admin.setPasswordHash(passwordHasher.hash(request.getAdminPassword()));
        admin.setFirstName(request.getAdminFirstName());
        admin.setLastName(request.getAdminLastName());
        admin.setPhone(request.getAdminPhone());
        admin.setRole("BANK_ADMIN");
        admin.setIsActive(true);
        User savedAdmin = userRepository.save(admin);

        return new BankRegistrationResponse(
                savedBank.getId(),
                savedBank.getName(),
                savedAdmin.getId(),
                BankStatus.PENDING_ACTIVATION.name(),
                "Bank registered successfully. Activation email sent to " + request.getContactEmail()
        );
    }
}
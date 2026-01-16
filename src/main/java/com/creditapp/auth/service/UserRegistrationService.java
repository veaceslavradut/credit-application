package com.creditapp.auth.service;

import com.creditapp.auth.dto.RegistrationRequest;
import com.creditapp.auth.dto.RegistrationResponse;
import com.creditapp.auth.event.UserRegisteredEvent;
import com.creditapp.auth.exception.DuplicateEmailException;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final PasswordHasher passwordHasher;
    private final ApplicationEventPublisher eventPublisher;

    public UserRegistrationService(UserRepository userRepository, PasswordValidator passwordValidator,
                                   PasswordHasher passwordHasher, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordValidator = passwordValidator;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }

    public RegistrationResponse registerBorrower(RegistrationRequest request) {
        passwordValidator.isValid(request.getPassword());
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHasher.hash(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhoneNumber());
        user.setRole("BORROWER");
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName()));
        return new RegistrationResponse(savedUser.getId(), savedUser.getEmail(), "Registration successful. You can now log in.");
    }
}
package com.creditapp.auth.service;

import com.creditapp.auth.exception.PasswordValidationException;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidator {

    private static final int MIN_LENGTH = 12;

    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new PasswordValidationException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new PasswordValidationException("Password must contain at least one uppercase letter (A-Z)");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new PasswordValidationException("Password must contain at least one lowercase letter (a-z)");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new PasswordValidationException("Password must contain at least one digit (0-9)");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new PasswordValidationException("Password must contain at least one special character (!@#$%^&*()_+-=)");
        }
        return true;
    }
}
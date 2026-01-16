package com.creditapp.shared.service;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {
    
    @Data
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
    }
    
    public ValidationResult validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password is required");
        }
        
        if (password.length() < 12) {
            return new ValidationResult(false, "Password must be at least 12 characters long");
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+=\\[\\]{}|;:',.<>?/~`].*");
        
        if (!hasUppercase) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter");
        }
        
        if (!hasLowercase) {
            return new ValidationResult(false, "Password must contain at least one lowercase letter");
        }
        
        if (!hasDigit) {
            return new ValidationResult(false, "Password must contain at least one digit");
        }
        
        if (!hasSpecialChar) {
            return new ValidationResult(false, "Password must contain at least one special character");
        }
        
        return new ValidationResult(true, "Password is strong");
    }
    
    public boolean passwordsDiffer(String password1, String password2) {
        if (password1 == null || password2 == null) {
            return true;
        }
        return !password1.equals(password2);
    }
}
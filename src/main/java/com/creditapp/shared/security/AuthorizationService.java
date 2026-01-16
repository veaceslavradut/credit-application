package com.creditapp.shared.security;

import com.creditapp.auth.filter.JwtAuthenticationToken;
import com.creditapp.shared.model.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {

    public boolean hasRole(String role) {
        return getCurrentUserRole() != null && getCurrentUserRole().name().equals(role);
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getUserId();
        }
        return null;
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getEmail();
        }
        return null;
    }

    public UserRole getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> {
                    try {
                        return UserRole.valueOf(authority.getAuthority());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .orElse(null);
        }
        return null;
    }

    public boolean isBorrower() {
        return hasRole("BORROWER");
    }

    public boolean isBankAdmin() {
        return hasRole("BANK_ADMIN");
    }

    public boolean isComplianceOfficer() {
        return hasRole("COMPLIANCE_OFFICER");
    }
}
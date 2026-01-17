package com.creditapp.shared.security;

import com.creditapp.auth.filter.JwtAuthenticationToken;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.model.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {
    
    private final UserRepository userRepository;
    
    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean hasRole(String role) {
        return getCurrentUserRole() != null && getCurrentUserRole().name().equals(role);
    }
    
    /**
     * Get bank ID from JWT token for BANK_ADMIN users.
     * Throws IllegalStateException if organizationId not found.
     */
    public UUID getBankIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
            UUID organizationId = token.getOrganizationId();
            if (organizationId != null) {
                return organizationId;
            }
        }
        throw new IllegalStateException("Bank ID not found in authentication context");
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getUserId();
        }
        // For test purposes with @WithMockUser or other authentication types,
        // try to extract UUID from principal if it's stored as a custom attribute
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }
        // Fallback for @WithMockUser - look up user by username (email)
        if (authentication != null && authentication.getName() != null) {
            return userRepository.findByEmail(authentication.getName())
                    .map(user -> user.getId())
                    .orElse(null);
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
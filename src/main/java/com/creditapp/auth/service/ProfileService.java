package com.creditapp.auth.service;

import com.creditapp.auth.dto.ChangePasswordRequest;
import com.creditapp.auth.dto.ChangePasswordResponse;
import com.creditapp.auth.dto.UpdateProfileRequest;
import com.creditapp.auth.dto.UserProfileResponse;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.borrower.dto.LoanPreferenceDTO;
import com.creditapp.borrower.model.LoanPreference;
import com.creditapp.borrower.repository.LoanPreferenceRepository;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.RefreshToken;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.repository.RefreshTokenRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.PasswordChangeEmailService;
import com.creditapp.shared.service.PasswordValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileService {
    
    private final UserRepository userRepository;
    private final LoanPreferenceRepository loanPreferenceRepository;
    private final OrganizationRepository organizationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidationService passwordValidationService;
    private final AuditService auditService;
    private final PasswordChangeEmailService passwordChangeEmailService;
    
    public UserProfileResponse getCurrentUserProfile(@org.springframework.lang.NonNull UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        UserProfileResponse profile = new UserProfileResponse();
        profile.setUserId(user.getId());
        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setRole(user.getRole());
        profile.setCreatedAt(user.getCreatedAt());
        profile.setUpdatedAt(user.getUpdatedAt());
        
        if (user.getRole() == UserRole.BORROWER) {
            List<LoanPreference> preferences = loanPreferenceRepository.findByUserIdOrderByPriorityAsc(userId);
            List<LoanPreferenceDTO> preferenceDTOs = preferences.stream()
                    .map(p -> new LoanPreferenceDTO(
                            p.getId(),
                            p.getPreferredAmount(),
                            p.getMinTerm(),
                            p.getMaxTerm(),
                            p.getPurposeCategory(),
                            p.getPriority()
                    ))
                    .collect(Collectors.toList());
            profile.setLoanPreferences(preferenceDTOs);
        } else if (user.getRole() == UserRole.BANK_ADMIN) {
            if (user.getOrganizationId() != null) {
                Organization org = organizationRepository.findById(Objects.requireNonNull(user.getOrganizationId()))
                        .orElse(null);
                if (org != null) {
                    profile.setBankId(org.getId());
                    profile.setBankName(org.getName());
                    profile.setBankStatus(org.getStatus() != null ? org.getStatus().toString() : "UNKNOWN");
                }
            }
        }
        
        return profile;
    }
    
    public UserProfileResponse updateProfile(@org.springframework.lang.NonNull UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        String oldFirstName = user.getFirstName();
        String oldLastName = user.getLastName();
        String oldPhoneNumber = user.getPhoneNumber();
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("firstName", oldFirstName);
        oldValues.put("lastName", oldLastName);
        oldValues.put("phoneNumber", oldPhoneNumber);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("firstName", request.getFirstName());
        newValues.put("lastName", request.getLastName());
        newValues.put("phoneNumber", request.getPhoneNumber());
        
        auditService.logActionWithValues(
                "User", userId, 
                AuditAction.PROFILE_UPDATED, 
                oldValues, newValues
        );
        
        return getCurrentUserProfile(updatedUser.getId());
    }
    
    public ChangePasswordResponse changePassword(@org.springframework.lang.NonNull UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password must differ from current password");
        }
        
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }
        
        PasswordValidationService.ValidationResult validationResult = 
                passwordValidationService.validatePasswordStrength(request.getNewPassword());
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(validationResult.getMessage());
        }
        
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        invalidateAllRefreshTokens(userId);
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("passwordHash", "[REDACTED]");
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("passwordHash", "[REDACTED]");
        
        auditService.logActionWithValues(
                "User", userId,
                AuditAction.PASSWORD_CHANGED,
                oldValues, newValues
        );
        
        passwordChangeEmailService.sendPasswordChangeNotification(user.getEmail(), user.getFirstName());
        
        return new ChangePasswordResponse(
                "Password changed successfully. You have been logged out on all devices.",
                LocalDateTime.now()
        );
    }
    
    public void invalidateAllRefreshTokens(@org.springframework.lang.NonNull UUID userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        }
        log.info("Invalidated {} refresh tokens for user {}", activeTokens.size(), userId);
    }
}
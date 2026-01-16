package com.creditapp.auth.controller;

import com.creditapp.auth.dto.ChangePasswordRequest;
import com.creditapp.auth.dto.ChangePasswordResponse;
import com.creditapp.auth.dto.UpdateProfileRequest;
import com.creditapp.auth.dto.UserProfileResponse;
import com.creditapp.auth.service.ProfileService;
import com.creditapp.shared.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    
    private final ProfileService profileService;
    private final AuthorizationService authorizationService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getProfile() {
        UUID userId = authorizationService.getCurrentUserId();
        log.info("User {} retrieved their profile", userId);
        
        UserProfileResponse profile = profileService.getCurrentUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = authorizationService.getCurrentUserId();
        log.info("User {} updating profile", userId);
        
        UserProfileResponse updatedProfile = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }
    
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = authorizationService.getCurrentUserId();
        log.info("User {} requested password change", userId);
        
        ChangePasswordResponse response = profileService.changePassword(userId, request);
        return ResponseEntity.ok(response);
    }
}
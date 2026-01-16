package com.creditapp.auth.dto;

import com.creditapp.borrower.dto.LoanPreferenceDTO;
import com.creditapp.shared.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // For BORROWER role
    private List<LoanPreferenceDTO> loanPreferences;
    
    // For BANK_ADMIN role
    private UUID bankId;
    private String bankName;
    private String bankStatus;
}
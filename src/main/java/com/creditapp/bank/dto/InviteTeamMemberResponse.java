package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for team member invitation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteTeamMemberResponse {
    private UUID inviteId;
    private String email;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
}

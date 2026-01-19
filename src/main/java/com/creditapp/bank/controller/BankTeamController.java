package com.creditapp.bank.controller;

import com.creditapp.bank.dto.InviteTeamMemberRequest;
import com.creditapp.bank.dto.InviteTeamMemberResponse;
import com.creditapp.bank.dto.TeamMemberDTO;
import com.creditapp.bank.service.BankTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for bank team member management.
 */
@RestController
@RequestMapping("/api/bank/team")
@Slf4j
@PreAuthorize("hasRole('''BANK_ADMIN''')")
public class BankTeamController {
    private final BankTeamService bankTeamService;

    public BankTeamController(BankTeamService bankTeamService) {
        this.bankTeamService = bankTeamService;
    }

    /**
     * GET /api/bank/team - Get all active team members.
     * Response time: <200ms
     */
    @GetMapping
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembers(Authentication authentication) {
        long startTime = System.currentTimeMillis();
        log.debug("Getting team members for user: {}", authentication.getName());

        UUID bankId = extractBankId(authentication);
        List<TeamMemberDTO> members = bankTeamService.getTeamMembers(bankId);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Retrieved {} team members in {}ms", members.size(), duration);
        return ResponseEntity.ok(members);
    }

    /**
     * POST /api/bank/team/invite - Send team member invitation.
     * Response time: <200ms
     */
    @PostMapping("/invite")
    public ResponseEntity<InviteTeamMemberResponse> inviteTeamMember(
            @Valid @RequestBody InviteTeamMemberRequest request,
            Authentication authentication) {
        long startTime = System.currentTimeMillis();
        log.debug("Inviting team member: {} for user: {}", request.getEmail(), authentication.getName());

        UUID bankId = extractBankId(authentication);
        InviteTeamMemberResponse response = bankTeamService.inviteTeamMember(bankId, request);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Sent team member invitation in {}ms", duration);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/bank/team/invite/{token}/accept - Accept team member invitation.
     * Response time: <200ms
     * Note: This endpoint does NOT require BANK_ADMIN role as it's for new team members accepting invites.
     */
    @PostMapping("/invite/{token}/accept")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TeamMemberDTO> acceptInvite(@PathVariable String token) {
        long startTime = System.currentTimeMillis();
        log.debug("Processing invitation acceptance for token: {}", token);

        TeamMemberDTO member = bankTeamService.acceptInvite(token);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Processed team member invitation in {}ms", duration);
        return ResponseEntity.ok(member);
    }

    /**
     * Extract bank ID from authentication context.
     */
    private UUID extractBankId(Authentication authentication) {
        // Implementation depends on how bank ID is stored in authentication
        // This is a placeholder; adjust based on your security implementation
        Object principal = authentication.getPrincipal();
        // For now, assume it can be extracted from a custom principal or claims
        throw new RuntimeException("Bank ID extraction not yet implemented - requires custom security context");
    }
}

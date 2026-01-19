package com.creditapp.bank.service;

import com.creditapp.shared.service.AuditService;
import com.creditapp.bank.dto.InviteTeamMemberRequest;
import com.creditapp.bank.dto.InviteTeamMemberResponse;
import com.creditapp.bank.dto.TeamMemberDTO;
import com.creditapp.bank.repository.BankTeamMemberRepository;
import com.creditapp.bank.repository.TeamMemberInviteRepository;
import com.creditapp.shared.service.NotificationService;
import com.creditapp.shared.model.BankTeamMember;
import com.creditapp.shared.model.TeamMemberInvite;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing bank team members and invitations.
 */
@Service
@Transactional
@Slf4j
public class BankTeamService {
    private final BankTeamMemberRepository teamMemberRepository;
    private final TeamMemberInviteRepository inviteRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    @Value("${app.team-invite-expiration-days:7}")
    private int inviteExpirationDays;

    public BankTeamService(
            BankTeamMemberRepository teamMemberRepository,
            TeamMemberInviteRepository inviteRepository,
            NotificationService notificationService,
            AuditService auditService) {
        this.teamMemberRepository = teamMemberRepository;
        this.inviteRepository = inviteRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Get all active team members for a bank.
     */
    @Transactional(readOnly = true)
    public List<TeamMemberDTO> getTeamMembers(UUID bankId) {
        log.debug("Retrieving team members for bankId: {}", bankId);
        return teamMemberRepository.findByBankIdAndStatus(bankId, "ACTIVE")
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Invite a team member to the bank.
     */
    public InviteTeamMemberResponse inviteTeamMember(UUID bankId, InviteTeamMemberRequest request) {
        log.debug("Inviting team member: {} to bankId: {}", request.getEmail(), bankId);

        // Check if already a member
        if (teamMemberRepository.findByBankIdAndEmail(bankId, request.getEmail()).isPresent()) {
            throw new RuntimeException("User already a team member");
        }

        // Check if already has pending invite
        if (inviteRepository.findByBankIdAndEmail(bankId, request.getEmail()).isPresent()) {
            throw new RuntimeException("Invitation already pending");
        }

        String inviteToken = UUID.randomUUID().toString();
        TeamMemberInvite invite = TeamMemberInvite.builder()
            .bankId(bankId)
            .email(request.getEmail())
            .name(request.getName())
            .inviteToken(inviteToken)
            .status("PENDING")
            .sentAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(inviteExpirationDays))
            .build();

        TeamMemberInvite saved = inviteRepository.save(invite);

        // Send invitation email
        try {
            notificationService.sendTeamInviteEmail(request.getEmail(), request.getName(), inviteToken);
        } catch (Exception e) {
            log.error("Failed to send team invite email to: {}", request.getEmail(), e);
        }

        // TODO: Use proper AuditAction enum once defined for team member invitations
        // auditService.logAction("ORGANIZATION", bankId, AuditAction.TEAM_MEMBER_INVITED);
        log.info("Team member invitation sent to: {} for bankId: {}", request.getEmail(), bankId);

        return InviteTeamMemberResponse.builder()
            .inviteId(saved.getId())
            .email(saved.getEmail())
            .status(saved.getStatus())
            .sentAt(saved.getSentAt())
            .expiresAt(saved.getExpiresAt())
            .build();
    }

    /**
     * Accept team member invitation.
     */
    public TeamMemberDTO acceptInvite(String inviteToken) {
        log.debug("Processing invitation acceptance for token: {}", inviteToken);

        TeamMemberInvite invite = inviteRepository.findByInviteToken(inviteToken)
            .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        if (invite.isExpired()) {
            throw new RuntimeException("Invitation has expired");
        }

        if (!"PENDING".equals(invite.getStatus())) {
            throw new RuntimeException("Invitation already processed");
        }

        // Create team member
        BankTeamMember teamMember = BankTeamMember.builder()
            .bankId(invite.getBankId())
            .email(invite.getEmail())
            .name(invite.getName())
            .status("ACTIVE")
            .joinedDate(LocalDateTime.now())
            .build();

        BankTeamMember saved = teamMemberRepository.save(teamMember);

        // Mark invite as accepted
        invite.setStatus("ACCEPTED");
        invite.setAcceptedAt(LocalDateTime.now());
        inviteRepository.save(invite);

        // TODO: Use proper AuditAction enum once defined for team member invitation acceptance
        // auditService.logAction("ORGANIZATION", invite.getBankId(), AuditAction.TEAM_MEMBER_INVITATION_ACCEPTED);
        log.info("Team member invitation accepted for email: {}", invite.getEmail());

        return mapToDTO(saved);
    }

    /**
     * Map BankTeamMember entity to DTO.
     */
    private TeamMemberDTO mapToDTO(BankTeamMember member) {
        return TeamMemberDTO.builder()
            .id(member.getId())
            .email(member.getEmail())
            .name(member.getName())
            .joinedDate(member.getJoinedDate())
            .lastLogin(member.getLastLogin())
            .status(member.getStatus())
            .build();
    }
}

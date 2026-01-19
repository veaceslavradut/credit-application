package com.creditapp.unit.bank;

import com.creditapp.bank.dto.InviteTeamMemberRequest;
import com.creditapp.bank.dto.InviteTeamMemberResponse;
import com.creditapp.bank.dto.TeamMemberDTO;
import com.creditapp.bank.repository.BankTeamMemberRepository;
import com.creditapp.bank.repository.TeamMemberInviteRepository;
import com.creditapp.bank.service.BankTeamService;
import com.creditapp.shared.model.BankTeamMember;
import com.creditapp.shared.model.TeamMemberInvite;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTeamServiceTest {

    @Mock
    private BankTeamMemberRepository teamMemberRepository;

    @Mock
    private TeamMemberInviteRepository inviteRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    private BankTeamService service;
    private UUID bankId;

    @BeforeEach
    void setUp() {
        service = new BankTeamService(teamMemberRepository, inviteRepository, notificationService, auditService);
        bankId = UUID.randomUUID();
    }

    @Test
    void testGetTeamMembers_Success() {
        BankTeamMember member1 = BankTeamMember.builder()
            .id(UUID.randomUUID())
            .bankId(bankId)
            .email("member1@test.com")
            .name("Member One")
            .status("ACTIVE")
            .joinedDate(LocalDateTime.now())
            .build();

        BankTeamMember member2 = BankTeamMember.builder()
            .id(UUID.randomUUID())
            .bankId(bankId)
            .email("member2@test.com")
            .name("Member Two")
            .status("ACTIVE")
            .joinedDate(LocalDateTime.now())
            .build();

        when(teamMemberRepository.findByBankIdAndStatus(bankId, "ACTIVE"))
            .thenReturn(List.of(member1, member2));

        List<TeamMemberDTO> members = service.getTeamMembers(bankId);

        assertNotNull(members);
        assertEquals(2, members.size());
        assertEquals("member1@test.com", members.get(0).getEmail());
        assertEquals("member2@test.com", members.get(1).getEmail());
    }

    @Test
    void testGetTeamMembers_Empty() {
        when(teamMemberRepository.findByBankIdAndStatus(bankId, "ACTIVE"))
            .thenReturn(List.of());

        List<TeamMemberDTO> members = service.getTeamMembers(bankId);

        assertNotNull(members);
        assertEquals(0, members.size());
    }

    @Test
    void testInviteTeamMember_Success() {
        when(teamMemberRepository.findByBankIdAndEmail(bankId, "newmember@test.com"))
            .thenReturn(Optional.empty());
        when(inviteRepository.findByBankIdAndEmail(bankId, "newmember@test.com"))
            .thenReturn(Optional.empty());
        when(inviteRepository.save(any(TeamMemberInvite.class)))
            .thenAnswer(invocation -> {
                TeamMemberInvite invite = invocation.getArgument(0);
                invite.setId(UUID.randomUUID());
                return invite;
            });

        InviteTeamMemberRequest request = InviteTeamMemberRequest.builder()
            .email("newmember@test.com")
            .name("New Member")
            .role("ADMIN")
            .build();

        InviteTeamMemberResponse response = service.inviteTeamMember(bankId, request);

        assertNotNull(response);
        assertEquals("newmember@test.com", response.getEmail());
        assertEquals("PENDING", response.getStatus());
        assertNotNull(response.getInviteId());
        verify(notificationService, times(1)).sendTeamInviteEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testInviteTeamMember_AlreadyMember() {
        BankTeamMember existing = BankTeamMember.builder()
            .id(UUID.randomUUID())
            .bankId(bankId)
            .email("existing@test.com")
            .name("Existing Member")
            .status("ACTIVE")
            .build();

        when(teamMemberRepository.findByBankIdAndEmail(bankId, "existing@test.com"))
            .thenReturn(Optional.of(existing));

        InviteTeamMemberRequest request = InviteTeamMemberRequest.builder()
            .email("existing@test.com")
            .name("Existing Member")
            .build();

        assertThrows(RuntimeException.class, () -> service.inviteTeamMember(bankId, request));
    }

    @Test
    void testInviteTeamMember_InvitationPending() {
        TeamMemberInvite pending = TeamMemberInvite.builder()
            .id(UUID.randomUUID())
            .bankId(bankId)
            .email("pending@test.com")
            .name("Pending Member")
            .status("PENDING")
            .build();

        when(teamMemberRepository.findByBankIdAndEmail(bankId, "pending@test.com"))
            .thenReturn(Optional.empty());
        when(inviteRepository.findByBankIdAndEmail(bankId, "pending@test.com"))
            .thenReturn(Optional.of(pending));

        InviteTeamMemberRequest request = InviteTeamMemberRequest.builder()
            .email("pending@test.com")
            .name("Pending Member")
            .build();

        assertThrows(RuntimeException.class, () -> service.inviteTeamMember(bankId, request));
    }

    @Test
    void testAcceptInvite_Success() {
        String inviteToken = UUID.randomUUID().toString();
        TeamMemberInvite invite = TeamMemberInvite.builder()
            .id(UUID.randomUUID())
            .bankId(bankId)
            .email("newmember@test.com")
            .name("New Member")
            .inviteToken(inviteToken)
            .status("PENDING")
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        BankTeamMember savedMember = BankTeamMember.builder()
            .id(UUID.randomUUID())
            .bankId(bankId)
            .email("newmember@test.com")
            .name("New Member")
            .status("ACTIVE")
            .build();

        when(inviteRepository.findByInviteToken(inviteToken))
            .thenReturn(Optional.of(invite));
        when(teamMemberRepository.save(any(BankTeamMember.class)))
            .thenReturn(savedMember);

        TeamMemberDTO accepted = service.acceptInvite(inviteToken);

        assertNotNull(accepted);
        assertEquals("newmember@test.com", accepted.getEmail());
        assertEquals("ACTIVE", accepted.getStatus());
        verify(teamMemberRepository, times(1)).save(any(BankTeamMember.class));
        verify(inviteRepository, times(1)).save(any(TeamMemberInvite.class));
    }

    @Test
    void testAcceptInvite_ExpiredToken() {
        String inviteToken = UUID.randomUUID().toString();
        TeamMemberInvite expiredInvite = TeamMemberInvite.builder()
            .id(UUID.randomUUID())
            .bankId(bankId)
            .email("newmember@test.com")
            .name("New Member")
            .inviteToken(inviteToken)
            .status("PENDING")
            .expiresAt(LocalDateTime.now().minusDays(1))
            .build();

        when(inviteRepository.findByInviteToken(inviteToken))
            .thenReturn(Optional.of(expiredInvite));

        assertThrows(RuntimeException.class, () -> service.acceptInvite(inviteToken));
    }

    @Test
    void testAcceptInvite_InvalidToken() {
        String inviteToken = UUID.randomUUID().toString();

        when(inviteRepository.findByInviteToken(inviteToken))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.acceptInvite(inviteToken));
    }
}

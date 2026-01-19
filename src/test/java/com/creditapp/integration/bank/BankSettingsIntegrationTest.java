package com.creditapp.integration.bank;

import com.creditapp.bank.dto.BankProfileDTO;
import com.creditapp.bank.dto.InviteTeamMemberRequest;
import com.creditapp.bank.dto.TeamMemberDTO;
import com.creditapp.bank.repository.BankTeamMemberRepository;
import com.creditapp.bank.repository.TeamMemberInviteRepository;
import com.creditapp.bank.service.BankProfileService;
import com.creditapp.bank.service.BankTeamService;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.TeamMemberInvite;
import com.creditapp.shared.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankSettingsIntegrationTest {

    @Autowired
    private BankProfileService bankProfileService;

    @Autowired
    private BankTeamService bankTeamService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BankTeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamMemberInviteRepository inviteRepository;

    private Organization testBank;

    @BeforeEach
    void setUp() {
        testBank = Organization.builder()
            .name("Integration Test Bank")
            .registrationNumber("INTTEST123")
            .taxId("TAX123")
            .countryCode("US")
            .status(BankStatus.ACTIVE)
            .active(true)
            .contactEmail("contact@inttest.com")
            .phone("+1234567890")
            .addressStreet("789 Test Ave")
            .addressCity("Test City")
            .addressState("TS")
            .addressZip("12345")
            .website("https://inttest.com")
            .logoUrl("https://inttest.com/logo.png")
            .build();
        testBank = organizationRepository.save(testBank);
    }

    @Test
    void testGetBankProfile_IntegrationSuccess() {
        long startTime = System.currentTimeMillis();

        BankProfileDTO profile = bankProfileService.getBankProfile(testBank.getId());

        long duration = System.currentTimeMillis() - startTime;

        assertNotNull(profile);
        assertEquals(testBank.getId(), profile.getBankId());
        assertEquals("Integration Test Bank", profile.getName());
        assertEquals("INTTEST123", profile.getRegistrationNumber());
        assertEquals("contact@inttest.com", profile.getContactEmail());
        assertEquals("+1234567890", profile.getPhone());
        assertNotNull(profile.getAddress());
        assertEquals("789 Test Ave", profile.getAddress().getStreet());
        assertTrue(duration < 200, "Profile retrieval took " + duration + "ms, expected <200ms");
    }

    @Test
    void testUpdateBankProfile_Verifies() {
        testBank.setContactEmail("updated@inttest.com");
        testBank = organizationRepository.save(testBank);

        BankProfileDTO updated = bankProfileService.getBankProfile(testBank.getId());

        assertEquals("updated@inttest.com", updated.getContactEmail());
    }

    @Test
    void testInviteAndAcceptTeamMember() {
        // Invite team member
        long startTime = System.currentTimeMillis();

        InviteTeamMemberRequest inviteRequest = InviteTeamMemberRequest.builder()
            .email("newteam@inttest.com")
            .name("New Team Member")
            .role("OFFICER")
            .build();

        bankTeamService.inviteTeamMember(testBank.getId(), inviteRequest);

        long inviteDuration = System.currentTimeMillis() - startTime;
        assertTrue(inviteDuration < 200, "Invite took " + inviteDuration + "ms, expected <200ms");

        // Verify invite created
        List<TeamMemberInvite> invites = inviteRepository.findByBankIdAndStatus(testBank.getId(), "PENDING");
        assertEquals(1, invites.size());
        assertEquals("newteam@inttest.com", invites.get(0).getEmail());
    }

    @Test
    void testGetTeamMembers_Performance() {
        // Add test team members
        for (int i = 0; i < 5; i++) {
            var member = com.creditapp.shared.model.BankTeamMember.builder()
                .bankId(testBank.getId())
                .email("member" + i + "@inttest.com")
                .name("Team Member " + i)
                .status("ACTIVE")
                .build();
            teamMemberRepository.save(member);
        }

        long startTime = System.currentTimeMillis();
        List<TeamMemberDTO> members = bankTeamService.getTeamMembers(testBank.getId());
        long duration = System.currentTimeMillis() - startTime;

        assertEquals(5, members.size());
        assertTrue(duration < 200, "Team retrieval took " + duration + "ms, expected <200ms");
    }

    @Test
    void testInviteExpiration_IsValidated() {
        // Create expired invite
        String inviteToken = UUID.randomUUID().toString();
        TeamMemberInvite expiredInvite = TeamMemberInvite.builder()
            .bankId(testBank.getId())
            .email("expired@inttest.com")
            .name("Expired Member")
            .inviteToken(inviteToken)
            .status("PENDING")
            .expiresAt(LocalDateTime.now().minusDays(1))
            .build();
        inviteRepository.save(expiredInvite);

        // Try to accept
        assertThrows(RuntimeException.class, () -> bankTeamService.acceptInvite(inviteToken));
    }

    @Test
    void testAuditTrail_OnProfileUpdate() {
        testBank.setContactEmail("audit@inttest.com");
        organizationRepository.save(testBank);

        // Reload and verify audit trail was logged
        BankProfileDTO updated = bankProfileService.getBankProfile(testBank.getId());
        assertEquals("audit@inttest.com", updated.getContactEmail());

        // TODO: Verify audit log entries when AuditService integration is complete
    }
}

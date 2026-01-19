package com.creditapp.bank.repository;

import com.creditapp.shared.model.TeamMemberInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for team member invitations.
 */
@Repository
public interface TeamMemberInviteRepository extends JpaRepository<TeamMemberInvite, UUID> {
    Optional<TeamMemberInvite> findByInviteToken(String inviteToken);
    List<TeamMemberInvite> findByBankIdAndStatus(UUID bankId, String status);
    Optional<TeamMemberInvite> findByBankIdAndEmail(UUID bankId, String email);
}

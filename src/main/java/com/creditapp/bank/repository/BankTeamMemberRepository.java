package com.creditapp.bank.repository;

import com.creditapp.shared.model.BankTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for bank team members.
 */
@Repository
public interface BankTeamMemberRepository extends JpaRepository<BankTeamMember, UUID> {
    List<BankTeamMember> findByBankIdAndStatus(UUID bankId, String status);
    Optional<BankTeamMember> findByBankIdAndEmail(UUID bankId, String email);
    List<BankTeamMember> findByBankId(UUID bankId);
}

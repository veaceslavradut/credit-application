package com.creditapp.shared.repository;

import com.creditapp.shared.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByRegistrationNumber(String registrationNumber);
    Optional<Organization> findByActivationToken(String activationToken);
    Optional<Organization> findByName(String name);
}
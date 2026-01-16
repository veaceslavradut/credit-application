package com.creditapp.borrower.repository;

import com.creditapp.borrower.model.ApplicationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ApplicationDetails entity.
 */
@Repository
public interface ApplicationDetailsRepository extends JpaRepository<ApplicationDetails, UUID> {

    /**
     * Find application details by application ID.
     * @param applicationId the application ID
     * @return optional containing application details if found
     */
    Optional<ApplicationDetails> findByApplicationId(UUID applicationId);
}

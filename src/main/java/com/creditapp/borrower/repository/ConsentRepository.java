package com.creditapp.borrower.repository;

import com.creditapp.borrower.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Consent entity
 */
@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {

    /**
     * Find all consents for a specific application
     */
    List<Consent> findByApplicationId(UUID applicationId);

    /**
     * Find a specific consent by application and consent number
     */
    Optional<Consent> findByApplicationIdAndConsentNumber(UUID applicationId, Integer consentNumber);

    /**
     * Find all consents for a borrower
     */
    List<Consent> findByBorrowerId(UUID borrowerId);

    /**
     * Delete all consents for an application (cleanup on app deletion)
     */
    void deleteByApplicationId(UUID applicationId);

    /**
     * Find signed consents for an application
     */
    List<Consent> findByApplicationIdAndIsSignedTrue(UUID applicationId);
}

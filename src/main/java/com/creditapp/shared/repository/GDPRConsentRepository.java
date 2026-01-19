package com.creditapp.shared.repository;

import com.creditapp.shared.model.GDPRConsent;
import com.creditapp.shared.model.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GDPRConsentRepository extends JpaRepository<GDPRConsent, UUID> {
    
    @Query("SELECT c FROM GDPRConsent c WHERE c.borrowerId = :borrowerId AND c.consentType = :type ORDER BY c.version DESC LIMIT 1")
    Optional<GDPRConsent> findLatestByBorrowerIdAndType(@Param("borrowerId") UUID borrowerId, @Param("type") ConsentType type);
    
    List<GDPRConsent> findAllByBorrowerId(UUID borrowerId);
    
    Optional<GDPRConsent> findByBorrowerIdAndConsentTypeAndWithdrawnAtNull(UUID borrowerId, ConsentType type);
}
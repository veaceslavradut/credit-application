package com.creditapp.bank.repository;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findByApplicationId(UUID applicationId);
    Page<Offer> findByApplicationId(UUID applicationId, Pageable pageable);
    Optional<Offer> findByApplicationIdAndBankId(UUID applicationId, UUID bankId);
    List<Offer> findByExpiresAtBefore(LocalDateTime expirationTime);
    List<Offer> findByApplicationIdOrderByAprAsc(UUID applicationId);
    
    /**
     * Find the currently selected (ACCEPTED) offer for an application.
     * @param applicationId Application UUID
     * @return Optional containing the accepted offer if one exists
     */
    Optional<Offer> findByApplicationIdAndOfferStatus(UUID applicationId, OfferStatus offerStatus);
    
    /**
     * Retrieve all non-expired offers for an application, sorted by APR ascending.
     * This query is optimized to avoid N+1 queries when fetching bank details.
     * Note: We don't use JOIN FETCH here since we're not using @ManyToOne relationships.
     * We query offers and banks separately in the service layer.
     * 
     * @param applicationId Application UUID
     * @return List of offers sorted by APR (lowest first)
     */
    @Query("SELECT o FROM Offer o WHERE o.applicationId = :applicationId AND o.offerStatus != :excludedStatus ORDER BY o.apr ASC")
    List<Offer> findActiveOffersByApplicationIdOrderByApr(
        @Param("applicationId") UUID applicationId,
        @Param("excludedStatus") OfferStatus excludedStatus
    );
}
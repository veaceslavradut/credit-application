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
     * Retrieve offers by bank with pagination for dashboard queues.
     */
    @Query("SELECT o FROM Offer o WHERE o.bankId = :bankId ORDER BY o.createdAt DESC")
    Page<Offer> findByBankId(@Param("bankId") UUID bankId, Pageable pageable);
    
    /**
     * Count distinct applications that have at least one offer from the bank.
     */
    @Query("SELECT COUNT(DISTINCT o.applicationId) FROM Offer o WHERE o.bankId = :bankId")
    Long countDistinctApplicationsByBankId(@Param("bankId") UUID bankId);
    
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
    
    /**
     * Retrieve all offers for a specific borrower with pagination and sorting.
     * Joins Offer with Application to filter by borrowerId.
     * Optimized with explicit database indices on (borrowerId, createdAt DESC).
     * 
     * @param borrowerId Borrower UUID
     * @param pageable Pagination and sorting information
     * @return Page of offers for the borrower
     */
    @Query("SELECT o FROM Offer o JOIN Application a ON o.applicationId = a.id WHERE a.borrowerId = :borrowerId ORDER BY o.createdAt DESC")
    Page<Offer> findOffersByBorrowerId(
        @Param("borrowerId") UUID borrowerId,
        Pageable pageable
    );
    
    /**
     * Find offers by application ID with explicit ordering for offer history.
     * Optimized for large result sets with proper indexing.
     * 
     * @param applicationId Application UUID
     * @param pageable Pagination information
     * @return Page of offers sorted by creation time descending
     */
    @Query("SELECT o FROM Offer o WHERE o.applicationId = :applicationId ORDER BY o.createdAt DESC")
    Page<Offer> findOffersByApplicationIdOrderByCreatedAtDesc(
        @Param("applicationId") UUID applicationId,
        Pageable pageable
    );
    
    /**
     * Count offers for a specific application.
     * Optimized for quick aggregation queries.
     * 
     * @param applicationId Application UUID
     * @return Number of offers for the application
     */
    @Query("SELECT COUNT(o) FROM Offer o WHERE o.applicationId = :applicationId")
    Long countOffersByApplicationId(@Param("applicationId") UUID applicationId);
    
    /**
     * Find the minimum APR (best offer) for an application.
     * Optimized for quick aggregation queries.
     * 
     * @param applicationId Application UUID
     * @return Minimum APR value, or null if no offers exist
     */
    @Query("SELECT MIN(o.apr) FROM Offer o WHERE o.applicationId = :applicationId")
    Optional<java.math.BigDecimal> findMinAprByApplicationId(@Param("applicationId") UUID applicationId);
    
    /**
     * Find offers by bank created after a specific date/time for dashboard metrics.
     * Used to calculate metrics filtered by time period.
     * 
     * @param bankId Bank UUID
     * @param createdAfter Filter start datetime
     * @return List of offers created after the specified time
     */
    @Query("SELECT o FROM Offer o WHERE o.bankId = :bankId AND o.createdAt >= :createdAfter")
    List<Offer> findByBankIdAndCreatedAtAfter(@Param("bankId") UUID bankId, @Param("createdAfter") LocalDateTime createdAfter);
}
package com.creditapp.bank.repository;

import com.creditapp.bank.model.OfferDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for OfferDocument entities.
 */
@Repository
public interface OfferDocumentRepository extends JpaRepository<OfferDocument, UUID> {
    
    /**
     * Find all documents for an offer.
     */
    List<OfferDocument> findByOfferId(UUID offerId);
    
    /**
     * Find documents by offer and document type.
     */
    @Query("SELECT od FROM OfferDocument od WHERE od.offerId = :offerId AND od.documentType = :documentType ORDER BY od.uploadedAt DESC")
    List<OfferDocument> findByOfferIdAndDocumentType(@Param("offerId") UUID offerId, @Param("documentType") String documentType);
    
    /**
     * Find a specific document by ID and offer ID (ownership check).
     */
    Optional<OfferDocument> findByIdAndOfferId(UUID id, UUID offerId);
    
    /**
     * Delete all documents for an offer (cascade).
     */
    void deleteByOfferId(UUID offerId);
}
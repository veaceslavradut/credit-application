package com.creditapp.borrower.repository;

import com.creditapp.borrower.model.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ApplicationDocument entity.
 * Provides methods to query documents with support for soft delete filtering.
 */
@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, UUID> {

    /**
     * Find all documents for an application (including deleted).
     */
    List<ApplicationDocument> findByApplicationId(UUID applicationId);

    /**
     * Find all active (non-deleted) documents for an application.
     */
    List<ApplicationDocument> findByApplicationIdAndDeletedAtIsNull(UUID applicationId);

    /**
     * Find a specific active document by ID and application ID.
     */
    Optional<ApplicationDocument> findByIdAndApplicationIdAndDeletedAtIsNull(UUID id, UUID applicationId);

    /**
     * Calculate total file size for all active documents in an application.
     */
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM ApplicationDocument d WHERE d.applicationId = :applicationId AND d.deletedAt IS NULL")
    Long calculateTotalFileSizeForApplication(@Param("applicationId") UUID applicationId);
}

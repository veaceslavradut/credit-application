package com.creditapp.shared.repository;

import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.model.LegalDocument;
import com.creditapp.shared.model.LegalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for LegalDocument entity with custom queries for version tracking
 */
@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, UUID> {

    /**
     * Find the latest published legal document by type and language
     */
    @Query("SELECT ld FROM LegalDocument ld " +
           "WHERE ld.documentType = :type " +
           "AND ld.status = com.creditapp.shared.model.LegalStatus.PUBLISHED " +
           "AND ld.language = :language " +
           "ORDER BY ld.version DESC " +
           "LIMIT 1")
    Optional<LegalDocument> findLatestPublishedByType(@Param("type") DocumentType type, @Param("language") String language);

    /**
     * Find all versions of a legal document ordered by version descending
     */
    List<LegalDocument> findAllByDocumentTypeOrderByVersionDesc(DocumentType documentType);

    /**
     * Find all published documents by type
     */
    List<LegalDocument> findByDocumentTypeAndStatus(DocumentType documentType, LegalStatus status);
}
package com.creditapp.borrower.repository;

import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Application entity.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    /**
     * Find applications by borrower ID with pagination.
     * @param borrowerId the borrower ID
     * @param pageable pagination info
     * @return page of applications
     */
    Page<Application> findByBorrowerId(UUID borrowerId, Pageable pageable);

    /**
     * Find applications by borrower ID and status with pagination.
     * @param borrowerId the borrower ID
     * @param status the application status
     * @param pageable pagination info
     * @return page of applications
     */
    Page<Application> findByBorrowerIdAndStatus(UUID borrowerId, ApplicationStatus status, Pageable pageable);

    /**
     * Find all applications by borrower ID.
     * @param borrowerId the borrower ID
     * @return list of applications
     */
    List<Application> findByBorrowerId(UUID borrowerId);

    /**
     * Find applications with eager-loaded relationships.
     * @param borrowerId the borrower ID
     * @param pageable pagination info
     * @return page of applications
     */
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.details " +
           "LEFT JOIN FETCH a.history " +
           "WHERE a.borrowerId = :borrowerId " +
           "ORDER BY a.createdAt DESC")
    Page<Application> findByBorrowerIdWithRelations(@Param("borrowerId") UUID borrowerId, Pageable pageable);
    
    /**
     * Find applications by borrower ID with optimized ordering for history retrieval.
     * Indexed on (borrowerId, submittedAt DESC) for efficient sorting.
     * 
     * @param borrowerId the borrower ID
     * @param pageable pagination info
     * @return page of applications sorted by submission date descending
     */
    @Query("SELECT a FROM Application a WHERE a.borrowerId = :borrowerId ORDER BY a.submittedAt DESC")
    Page<Application> findByBorrowerIdOrderBySubmittedAtDesc(
        @Param("borrowerId") UUID borrowerId,
        Pageable pageable
    );
    
    /**
     * Count applications for a specific borrower by status.
     * Optimized for quick aggregation.
     * 
     * @param borrowerId the borrower ID
     * @param status the application status
     * @return count of applications matching criteria
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.borrowerId = :borrowerId AND a.status = :status")
    Long countByBorrowerIdAndStatus(
        @Param("borrowerId") UUID borrowerId,
        @Param("status") ApplicationStatus status
    );
    
    /**
     * Find applications created between two dates for analytics.
     * 
     * @param startDate start date time
     * @param endDate end date time
     * @return list of applications
     */
    List<Application> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}

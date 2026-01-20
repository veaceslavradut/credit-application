package com.creditapp.borrower.repository;

import com.creditapp.borrower.model.ApplicationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ApplicationHistory entity.
 */
@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {

    /**
     * Find all history entries for an application.
     * @param applicationId the application ID
     * @return list of history entries
     */
    List<ApplicationHistory> findByApplicationId(UUID applicationId);

    /**
     * Find all history entries for an application ordered by change date descending.
     * @param applicationId the application ID
     * @return list of history entries ordered by changed_at DESC
     */
    List<ApplicationHistory> findByApplicationIdOrderByChangedAtDesc(UUID applicationId);

    /**
     * Find paginated history entries for an application ordered by change date descending.
     * @param applicationId the application ID
     * @param pageable the pagination parameters
     * @return paginated history entries ordered by changed_at DESC
     */
    Page<ApplicationHistory> findByApplicationIdOrderByChangedAtDesc(UUID applicationId, Pageable pageable);
}

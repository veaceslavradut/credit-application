package com.creditapp.shared.repository;

import com.creditapp.shared.model.DeletionRequest;
import com.creditapp.shared.model.DeletionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, UUID> {

    @Query("SELECT dr FROM DeletionRequest dr WHERE dr.borrowerId = :borrowerId ORDER BY dr.requestedAt DESC LIMIT 1")
    Optional<DeletionRequest> findLatestByBorrowerId(@Param("borrowerId") UUID borrowerId);

    Optional<DeletionRequest> findByConfirmationToken(String token);

    List<DeletionRequest> findByStatusOrderByRequestedAtAsc(DeletionStatus status);

    Optional<DeletionRequest> findByBorrowerIdAndStatus(UUID borrowerId, DeletionStatus status);
}
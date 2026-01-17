package com.creditapp.borrower.repository;

import com.creditapp.borrower.model.BorrowerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BorrowerNotificationRepository extends JpaRepository<BorrowerNotification, UUID> {
    Page<BorrowerNotification> findByBorrowerId(UUID borrowerId, Pageable pageable);
    
    List<BorrowerNotification> findByBorrowerIdOrderBySentAtDesc(UUID borrowerId);
    
    Page<BorrowerNotification> findByBorrowerIdAndReadAtIsNull(UUID borrowerId, Pageable pageable);
    
    Page<BorrowerNotification> findByBorrowerIdAndReadAtIsNotNull(UUID borrowerId, Pageable pageable);
}

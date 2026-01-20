package com.creditapp.shared.repository;

import com.creditapp.shared.model.Notification;
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
 * Repository for in-portal notifications
 * Story 4.6: Offer Expiration Notification - Task 4
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Find all notifications for a bank, ordered by creation date (newest first)
     */
    Page<Notification> findByBankIdOrderByCreatedAtDesc(UUID bankId, Pageable pageable);
    
    /**
     * Find unread notifications for a bank
     */
    @Query("SELECT n FROM Notification n WHERE n.bankId = :bankId AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByBankId(@Param("bankId") UUID bankId);
    
    /**
     * Count unread notifications for a bank
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.bankId = :bankId AND n.readAt IS NULL")
    Long countUnreadByBankId(@Param("bankId") UUID bankId);
    
    /**
     * Find notifications by type for a bank
     */
    List<Notification> findByBankIdAndTypeOrderByCreatedAtDesc(UUID bankId, String type);
    
    /**
     * Delete old notifications (for cleanup)
     */
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
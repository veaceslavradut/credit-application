package com.creditapp.bank.service;

import com.creditapp.shared.model.Notification;
import com.creditapp.shared.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing in-portal notifications
 * Story 4.6: Offer Expiration Notification - Task 4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankNotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * Create a new notification for a bank
     */
    @Transactional
    public Notification createNotification(UUID bankId, String type, String title, String message, String link) {
        log.info("Creating notification for bank {}: type={}, title={}", bankId, type, title);
        
        Notification notification = Notification.builder()
                .bankId(bankId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .build();
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Get all notifications for a bank with pagination
     */
    public Page<Notification> getNotifications(UUID bankId, Pageable pageable) {
        return notificationRepository.findByBankIdOrderByCreatedAtDesc(bankId, pageable);
    }
    
    /**
     * Get unread notifications for a bank
     */
    public List<Notification> getUnreadNotifications(UUID bankId) {
        return notificationRepository.findUnreadByBankId(bankId);
    }
    
    /**
     * Count unread notifications for a bank
     */
    public Long countUnread(UUID bankId) {
        return notificationRepository.countUnreadByBankId(bankId);
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(UUID notificationId, UUID bankId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getBankId().equals(bankId)) {
                notification.markAsRead();
                notificationRepository.save(notification);
                log.info("Marked notification {} as read for bank {}", notificationId, bankId);
            } else {
                log.warn("Bank {} attempted to mark notification {} owned by another bank", 
                        bankId, notificationId);
            }
        });
    }
    
    /**
     * Mark all notifications as read for a bank
     */
    @Transactional
    public int markAllAsRead(UUID bankId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByBankId(bankId);
        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for bank {}", unreadNotifications.size(), bankId);
        return unreadNotifications.size();
    }
    
    /**
     * Delete old notifications (for maintenance)
     */
    @Transactional
    public void deleteOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteOlderThan(cutoffDate);
        log.info("Deleted notifications older than {}", cutoffDate);
    }
}
package com.creditapp.shared.service;

import com.creditapp.borrower.dto.NotificationDTO;
import com.creditapp.borrower.exception.NotificationNotFoundException;
import com.creditapp.borrower.model.BorrowerNotification;
import com.creditapp.borrower.repository.BorrowerNotificationRepository;
import com.creditapp.shared.dto.EmailMetricsDTO;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.DeliveryStatus;
import com.creditapp.shared.model.NotificationChannel;
import com.creditapp.shared.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final BorrowerNotificationRepository notificationRepository;
    private final AuditService auditService;

    @Transactional
    public BorrowerNotification createNotification(UUID borrowerId, UUID applicationId, 
                                                   NotificationType type, String subject, String message) {
        log.info("Creating notification for borrower: {}, type: {}", borrowerId, type);
        
        BorrowerNotification notification = BorrowerNotification.builder()
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .notificationType(type)
                .subject(subject)
                .message(message)
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now())
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();
        
        BorrowerNotification saved = notificationRepository.save(notification);
        
        // Trigger async email sending
        sendNotificationAsync(saved.getId());
        
        log.info("Notification created with id: {}", saved.getId());
        return saved;
    }

    @Async
    public void sendNotificationAsync(UUID notificationId) {
        try {
            BorrowerNotification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotificationNotFoundException(notificationId));
            
            // TODO: Implement actual email sending via SendGrid in future stories
            // For now, just mark as SENT
            notification.setDeliveryStatus(DeliveryStatus.SENT);
            notificationRepository.save(notification);
            
            // Log audit event
            auditService.logAction(
                    "NOTIFICATION",
                    notificationId,
                    AuditAction.NOTIFICATION_SENT,
                    notification.getBorrowerId(),
                    "BORROWER"
            );
            
            log.info("Notification sent successfully: {}", notificationId);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", notificationId, e);
            // Mark as failed
            notificationRepository.findById(notificationId).ifPresent(n -> {
                n.setDeliveryStatus(DeliveryStatus.FAILED);
                notificationRepository.save(n);
            });
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(UUID borrowerId, Pageable pageable) {
        log.info("Fetching notifications for borrower: {}", borrowerId);
        
        Page<BorrowerNotification> notifications = notificationRepository.findByBorrowerId(borrowerId, pageable);
        
        return notifications.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnreadNotifications(UUID borrowerId, Pageable pageable) {
        log.info("Fetching unread notifications for borrower: {}", borrowerId);
        
        Page<BorrowerNotification> notifications = 
                notificationRepository.findByBorrowerIdAndReadAtIsNull(borrowerId, pageable);
        
        return notifications.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getReadNotifications(UUID borrowerId, Pageable pageable) {
        log.info("Fetching read notifications for borrower: {}", borrowerId);
        
        Page<BorrowerNotification> notifications = 
                notificationRepository.findByBorrowerIdAndReadAtIsNotNull(borrowerId, pageable);
        
        return notifications.map(this::toDTO);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID borrowerId) {
        log.info("Marking notification as read: {}", notificationId);
        
        BorrowerNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        
        // Verify ownership
        if (!notification.getBorrowerId().equals(borrowerId)) {
            throw new NotificationNotFoundException("Notification not found or access denied");
        }
        
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Notification marked as read: {}", notificationId);
        }
    }

    @Transactional(readOnly = true)
    public EmailMetricsDTO getEmailMetrics() {
        // Simple placeholder implementation for health check
        // In a full implementation with email tracking, this would query email delivery logs
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long sentLastHour = notificationRepository.findAll().stream()
                .filter(n -> n.getSentAt().isAfter(oneHourAgo))
                .filter(n -> n.getDeliveryStatus() == DeliveryStatus.SENT)
                .count();
        
        long failedLastHour = notificationRepository.findAll().stream()
                .filter(n -> n.getSentAt().isAfter(oneHourAgo))
                .filter(n -> n.getDeliveryStatus() == DeliveryStatus.FAILED)
                .count();
        
        double failureRate = (sentLastHour + failedLastHour) > 0 
                ? (double) failedLastHour / (sentLastHour + failedLastHour) 
                : 0.0;
        
        return EmailMetricsDTO.builder()
                .emailsSent(sentLastHour)
                .emailsFailed(failedLastHour)
                .failureRate(failureRate)
                .build();
    }

    private NotificationDTO toDTO(BorrowerNotification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .read(notification.getReadAt() != null)
                .applicationId(notification.getApplicationId())
                .build();
    }
}

package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.NotificationDTO;
import com.creditapp.borrower.exception.NotificationNotFoundException;
import com.creditapp.borrower.model.BorrowerNotification;
import com.creditapp.borrower.repository.BorrowerNotificationRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.DeliveryStatus;
import com.creditapp.shared.model.NotificationChannel;
import com.creditapp.shared.model.NotificationType;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerNotificationUnitTest {
    
    @Mock
    private BorrowerNotificationRepository notificationRepository;
    
    @Mock
    private AuditService auditService;
    
    private NotificationService notificationService;
    
    private UUID borrowerId;
    private UUID applicationId;
    private UUID notificationId;
    private BorrowerNotification testNotification;
    
    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, auditService);
        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        
        testNotification = BorrowerNotification.builder()
                .id(notificationId)
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .notificationType(NotificationType.APPLICATION_SUBMITTED)
                .subject("Application Submitted")
                .message("Your application has been submitted")
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now())
                .readAt(null)
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();
    }
    
    @Test
    void testCreateNotification_Success() {
        // Given
        when(notificationRepository.save(any(BorrowerNotification.class)))
                .thenReturn(testNotification);
        
        // When
        BorrowerNotification result = notificationService.createNotification(
                borrowerId, applicationId, 
                NotificationType.APPLICATION_SUBMITTED,
                "Application Submitted",
                "Your application has been submitted"
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(notificationId);
        assertThat(result.getBorrowerId()).isEqualTo(borrowerId);
        assertThat(result.getApplicationId()).isEqualTo(applicationId);
        assertThat(result.getNotificationType()).isEqualTo(NotificationType.APPLICATION_SUBMITTED);
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(result.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        
        verify(notificationRepository).save(any(BorrowerNotification.class));
    }
    
    @Test
    void testGetNotifications_ReturnsPageOfNotifications() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        BorrowerNotification notif1 = BorrowerNotification.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .notificationType(NotificationType.APPLICATION_SUBMITTED)
                .subject("Application Submitted")
                .message("Submitted")
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now())
                .deliveryStatus(DeliveryStatus.SENT)
                .build();
        
        Page<BorrowerNotification> page = new PageImpl<>(Arrays.asList(notif1), pageable, 1);
        when(notificationRepository.findByBorrowerId(borrowerId, pageable))
                .thenReturn(page);
        
        // When
        Page<NotificationDTO> result = notificationService.getNotifications(borrowerId, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNotificationType())
                .isEqualTo(NotificationType.APPLICATION_SUBMITTED);
        assertThat(result.getContent().get(0).isRead()).isFalse();
        
        verify(notificationRepository).findByBorrowerId(borrowerId, pageable);
    }
    
    @Test
    void testGetUnreadNotifications_ReturnsOnlyUnreadNotifications() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        BorrowerNotification unreadNotif = BorrowerNotification.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .notificationType(NotificationType.OFFERS_AVAILABLE)
                .subject("Offers Available")
                .message("You have new offers")
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now())
                .readAt(null)
                .deliveryStatus(DeliveryStatus.SENT)
                .build();
        
        Page<BorrowerNotification> page = new PageImpl<>(Arrays.asList(unreadNotif), pageable, 1);
        when(notificationRepository.findByBorrowerIdAndReadAtIsNull(borrowerId, pageable))
                .thenReturn(page);
        
        // When
        Page<NotificationDTO> result = notificationService.getUnreadNotifications(borrowerId, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isRead()).isFalse();
        
        verify(notificationRepository).findByBorrowerIdAndReadAtIsNull(borrowerId, pageable);
    }
    
    @Test
    void testGetReadNotifications_ReturnsOnlyReadNotifications() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime readTime = LocalDateTime.now();
        BorrowerNotification readNotif = BorrowerNotification.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .notificationType(NotificationType.APPLICATION_UNDER_REVIEW)
                .subject("Under Review")
                .message("Your application is under review")
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now().minusHours(1))
                .readAt(readTime)
                .deliveryStatus(DeliveryStatus.SENT)
                .build();
        
        Page<BorrowerNotification> page = new PageImpl<>(Arrays.asList(readNotif), pageable, 1);
        when(notificationRepository.findByBorrowerIdAndReadAtIsNotNull(borrowerId, pageable))
                .thenReturn(page);
        
        // When
        Page<NotificationDTO> result = notificationService.getReadNotifications(borrowerId, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isRead()).isTrue();
        assertThat(result.getContent().get(0).getReadAt()).isNotNull();
        
        verify(notificationRepository).findByBorrowerIdAndReadAtIsNotNull(borrowerId, pageable);
    }
    
    @Test
    void testMarkAsRead_Success() {
        // Given
        BorrowerNotification unreadNotif = BorrowerNotification.builder()
                .id(notificationId)
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .notificationType(NotificationType.APPLICATION_SUBMITTED)
                .subject("Application Submitted")
                .message("Submitted")
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now())
                .readAt(null)
                .deliveryStatus(DeliveryStatus.SENT)
                .build();
        
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(unreadNotif));
        when(notificationRepository.save(any(BorrowerNotification.class)))
                .thenReturn(unreadNotif);
        
        // When
        notificationService.markAsRead(notificationId, borrowerId);
        
        // Then
        ArgumentCaptor<BorrowerNotification> captor = ArgumentCaptor.forClass(BorrowerNotification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getReadAt()).isNotNull();
    }
    
    @Test
    void testMarkAsRead_NotificationNotFound() {
        // Given
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> notificationService.markAsRead(notificationId, borrowerId))
                .isInstanceOf(NotificationNotFoundException.class);
    }
    
    @Test
    void testMarkAsRead_WrongBorrower() {
        // Given
        UUID wrongBorrowerId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(testNotification));
        
        // When & Then
        assertThatThrownBy(() -> notificationService.markAsRead(notificationId, wrongBorrowerId))
                .isInstanceOf(NotificationNotFoundException.class);
    }
    
    @Test
    void testGetEmailMetrics_ReturnsCorrectCounts() {
        // Given
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        BorrowerNotification sentNotif1 = BorrowerNotification.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .notificationType(NotificationType.APPLICATION_SUBMITTED)
                .subject("Test")
                .message("Test")
                .channel(NotificationChannel.EMAIL)
                .sentAt(oneHourAgo.plusMinutes(30))
                .deliveryStatus(DeliveryStatus.SENT)
                .build();
        
        BorrowerNotification failedNotif = BorrowerNotification.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .notificationType(NotificationType.OFFERS_AVAILABLE)
                .subject("Test")
                .message("Test")
                .channel(NotificationChannel.EMAIL)
                .sentAt(oneHourAgo.plusMinutes(20))
                .deliveryStatus(DeliveryStatus.FAILED)
                .build();
        
        when(notificationRepository.findAll())
                .thenReturn(Arrays.asList(sentNotif1, failedNotif));
        
        // When
        var metrics = notificationService.getEmailMetrics();
        
        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getEmailsSent()).isEqualTo(1);
        assertThat(metrics.getEmailsFailed()).isEqualTo(1);
    }
}

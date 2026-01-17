package com.creditapp.integration.notification;

import com.creditapp.borrower.model.BorrowerNotification;
import com.creditapp.borrower.repository.BorrowerNotificationRepository;
import com.creditapp.shared.model.DeliveryStatus;
import com.creditapp.shared.model.NotificationChannel;
import com.creditapp.shared.model.NotificationType;
import com.creditapp.shared.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Integration tests for NotificationService
 * Tests notification creation, async delivery, and status tracking
 */
@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceIntegrationTest {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private BorrowerNotificationRepository notificationRepository;
    
    private UUID borrowerId;
    private UUID applicationId;
    
    @BeforeEach
    void setUp() {
        // Clean up notifications before each test
        notificationRepository.deleteAll();
        
        // Set up test data
        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
    }
    
    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
    }
    
    /**
     * Test 1: Create notification, verify BorrowerNotification record created with PENDING status
     */
    @Test
    void testCreateNotification_Success_CreatesNotificationRecord() {
        // Arrange
        String subject = "Application Submitted";
        String message = "Your application has been submitted successfully";
        
        // Act
        BorrowerNotification result = notificationService.createNotification(
                borrowerId, 
                applicationId, 
                NotificationType.APPLICATION_SUBMITTED, 
                subject, 
                message
        );
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getBorrowerId()).isEqualTo(borrowerId);
        assertThat(result.getApplicationId()).isEqualTo(applicationId);
        assertThat(result.getNotificationType()).isEqualTo(NotificationType.APPLICATION_SUBMITTED);
        assertThat(result.getSubject()).isEqualTo(subject);
        assertThat(result.getMessage()).isEqualTo(message);
        assertThat(result.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        // Initially PENDING, async delivery will update to SENT
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(result.getSentAt()).isNotNull();
    }
    
    /**
     * Test 2: Create notification, verify async delivery eventually completes
     */
    @Test
    void testCreateNotification_AsyncDelivery_CompletesSuccessfully() {
        // Arrange
        String subject = "Application Under Review";
        String message = "Your application is under review";
        
        // Act
        BorrowerNotification notification = notificationService.createNotification(
                borrowerId, 
                applicationId, 
                NotificationType.APPLICATION_UNDER_REVIEW, 
                subject, 
                message
        );
        
        UUID notificationId = notification.getId();
        
        // Assert - wait for async delivery to complete (with timeout)
        await()
                .atMost(5, SECONDS)
                .pollInterval(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    BorrowerNotification deliveredNotification = notificationRepository.findById(notificationId).orElseThrow(
                            () -> new IllegalArgumentException("Notification not found: " + notificationId)
                    );
                    assertThat(deliveredNotification.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
                });
    }
    
    /**
     * Test 3: Get notifications for borrower, verify pagination works
     */
    @Test
    void testGetNotifications_ReturnsBorrowerNotifications() {
        // Arrange - create multiple notifications
        notificationService.createNotification(
                borrowerId, 
                applicationId, 
                NotificationType.APPLICATION_SUBMITTED, 
                "Application Submitted", 
                "Your application has been submitted"
        );
        
        UUID applicationId2 = UUID.randomUUID();
        notificationService.createNotification(
                borrowerId, 
                applicationId2, 
                NotificationType.APPLICATION_UNDER_REVIEW, 
                "Application Under Review", 
                "Your application is under review"
        );
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        var notificationsPage = notificationService.getNotifications(borrowerId, pageable);
        
        // Assert
        assertThat(notificationsPage).isNotNull();
        assertThat(notificationsPage.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(notificationsPage.getContent()).isNotEmpty();
    }
    
    /**
     * Test 4: Create notification with different types, verify type stored correctly
     */
    @Test
    void testCreateNotification_DifferentTypes_StoredCorrectly() {
        // Arrange
        NotificationType[] types = {
                NotificationType.APPLICATION_SUBMITTED,
                NotificationType.APPLICATION_UNDER_REVIEW,
                NotificationType.OFFERS_AVAILABLE,
                NotificationType.OFFER_ACCEPTED
        };
        
        // Act & Assert
        for (int i = 0; i < types.length; i++) {
            UUID appId = UUID.randomUUID();
            BorrowerNotification notification = notificationService.createNotification(
                    borrowerId,
                    appId,
                    types[i],
                    types[i].name(),
                    "Test message for " + types[i].name()
            );
            
            assertThat(notification.getNotificationType()).isEqualTo(types[i]);
            assertThat(notification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        }
        
        // Verify all notifications exist in database
        List<BorrowerNotification> allNotifications = notificationRepository.findAll();
        assertThat(allNotifications.size()).isGreaterThanOrEqualTo(types.length);
    }
    
    /**
     * Test 5: Verify notification channel is always EMAIL in current implementation
     */
    @Test
    void testCreateNotification_ChannelAlwaysEmail() {
        // Arrange
        String subject = "Test Notification";
        String message = "Test message";
        
        // Act
        BorrowerNotification notification = notificationService.createNotification(
                borrowerId,
                applicationId,
                NotificationType.APPLICATION_SUBMITTED,
                subject,
                message
        );
        
        // Assert
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
    }
    
    /**
     * Test 6: Mark notification as read, verify read status updated
     */
    @Test
    void testMarkAsRead_UpdatesReadStatus() {
        // Arrange
        BorrowerNotification notification = notificationService.createNotification(
                borrowerId,
                applicationId,
                NotificationType.APPLICATION_SUBMITTED,
                "Test",
                "Test message"
        );
        
        UUID notificationId = notification.getId();
        
        // Act
        notificationService.markAsRead(notificationId, borrowerId);
        
        // Assert
        await()
                .atMost(2, SECONDS)
                .pollInterval(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    BorrowerNotification updated = notificationRepository.findById(notificationId).orElseThrow(
                            () -> new IllegalArgumentException("Notification not found: " + notificationId)
                    );
                    assertThat(updated.getReadAt()).isNotNull();
                });
    }
}

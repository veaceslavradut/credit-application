package com.creditapp.integration.borrower;

import com.creditapp.borrower.model.BorrowerNotification;
import com.creditapp.borrower.repository.BorrowerNotificationRepository;
import com.creditapp.shared.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BorrowerNotificationIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private BorrowerNotificationRepository notificationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private UUID borrowerId;
    private UUID applicationId;
    private UUID notificationId;
    
    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        
        // Create test notification
        BorrowerNotification notification = BorrowerNotification.builder()
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .notificationType(NotificationType.APPLICATION_SUBMITTED)
                .subject("Application Submitted")
                .message("Your application has been submitted for review")
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now())
                .deliveryStatus(DeliveryStatus.SENT)
                .build();
        
        BorrowerNotification saved = notificationRepository.save(notification);
        notificationId = saved.getId();
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testGetNotifications_ReturnsNotificationsForBorrower() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/borrower/notifications")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testGetNotifications_WithReadFilter_Unread() throws Exception {
        // Create unread notification
        BorrowerNotification unread = BorrowerNotification.builder()
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
        
        notificationRepository.save(unread);
        
        // When & Then
        mockMvc.perform(get("/api/borrower/notifications")
                .param("read", "false")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].isRead").value(false));
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testGetNotifications_WithReadFilter_Read() throws Exception {
        // Notification was already created in setUp and is read (sentAt is set, readAt is null)
        // So we need to mark it as read first
        BorrowerNotification notif = notificationRepository.findById(notificationId).orElse(null);
        assertThat(notif).isNotNull();
        notif.setReadAt(LocalDateTime.now());
        notificationRepository.save(notif);
        
        // When & Then
        mockMvc.perform(get("/api/borrower/notifications")
                .param("read", "true")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].isRead").value(true));
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testMarkNotificationAsRead_Success() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/borrower/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isNoContent());
        
        // Verify notification is marked as read
        BorrowerNotification notif = notificationRepository.findById(notificationId).orElse(null);
        assertThat(notif).isNotNull();
        assertThat(notif.getReadAt()).isNotNull();
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testMarkNotificationAsRead_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        // When & Then
        mockMvc.perform(put("/api/borrower/notifications/{notificationId}/read", nonExistentId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetNotifications_Unauthorized() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/api/borrower/notifications"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BANK_ADMIN"})
    void testGetNotifications_Forbidden_NonBorrowerRole() throws Exception {
        // When & Then - Wrong role
        mockMvc.perform(get("/api/borrower/notifications"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testGetNotifications_ReturnsCorrectFields() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/api/borrower/notifications")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        
        // Then - Verify structure
        assertThat(content).contains("\"notificationType\"");
        assertThat(content).contains("\"subject\"");
        assertThat(content).contains("\"message\"");
        assertThat(content).contains("\"sentAt\"");
        assertThat(content).contains("\"isRead\"");
        assertThat(content).contains("\"applicationId\"");
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testGetNotifications_PaginationWorks() throws Exception {
        // Create multiple notifications
        for (int i = 0; i < 5; i++) {
            BorrowerNotification notif = BorrowerNotification.builder()
                    .borrowerId(borrowerId)
                    .applicationId(applicationId)
                    .notificationType(NotificationType.APPLICATION_UNDER_REVIEW)
                    .subject("Notification " + i)
                    .message("Message " + i)
                    .channel(NotificationChannel.EMAIL)
                    .sentAt(LocalDateTime.now().minusHours(i))
                    .deliveryStatus(DeliveryStatus.SENT)
                    .build();
            notificationRepository.save(notif);
        }
        
        // When & Then - Request first page with size 3
        mockMvc.perform(get("/api/borrower/notifications")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(6)) // 1 from setUp + 5 new
                .andExpect(jsonPath("$.totalPages").value(2));
        
        // Request second page
        mockMvc.perform(get("/api/borrower/notifications")
                .param("page", "1")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testNotificationSentAtTimestampFormatted() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/api/borrower/notifications")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        
        // Then - Verify ISO-8601 format
        assertThat(content).contains("sentAt");
        // Should contain "Z" at end for UTC timezone
        assertThat(content).contains("Z\"");
    }
    
    @Test
    @WithMockUser(username = "test@example.com", roles = {"BORROWER"})
    void testMarkAsRead_UpdatesReadAtTimestamp() throws Exception {
        // Given - Notification starts unread
        BorrowerNotification notif = notificationRepository.findById(notificationId).orElse(null);
        assertThat(notif).isNotNull();
        assertThat(notif.getReadAt()).isNull();
        
        LocalDateTime beforeMark = LocalDateTime.now();
        
        // When
        mockMvc.perform(put("/api/borrower/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isNoContent());
        
        LocalDateTime afterMark = LocalDateTime.now();
        
        // Then
        BorrowerNotification updated = notificationRepository.findById(notificationId).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getReadAt()).isNotNull();
        assertThat(updated.getReadAt()).isAfterOrEqualTo(beforeMark);
        assertThat(updated.getReadAt()).isBeforeOrEqualTo(afterMark);
    }
}

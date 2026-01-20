package com.creditapp.bank.controller;

import com.creditapp.shared.model.Notification;
import com.creditapp.bank.service.BankNotificationService;
import com.creditapp.shared.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for bank in-portal notifications
 * Story 4.6: Offer Expiration Notification - Task 4
 */
@RestController
@RequestMapping("/api/bank/notifications")
@RequiredArgsConstructor
@Slf4j
public class BankNotificationController {
    
    private final BankNotificationService notificationService;
    private final AuthorizationService authorizationService;
    
    /**
     * Get all notifications for the authenticated bank with pagination
     */
    @GetMapping
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        UUID bankId = authorizationService.getBankIdFromContext();
        log.info("GET /api/bank/notifications - Bank {}, page {}, size {}", bankId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getNotifications(bankId, pageable);
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get unread notifications for the authenticated bank
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        UUID bankId = authorizationService.getBankIdFromContext();
        log.info("GET /api/bank/notifications/unread - Bank {}", bankId);
        
        List<Notification> notifications = notificationService.getUnreadNotifications(bankId);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get unread notification count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        UUID bankId = authorizationService.getBankIdFromContext();
        log.info("GET /api/bank/notifications/unread/count - Bank {}", bankId);
        
        Long count = notificationService.countUnread(bankId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * Mark a notification as read
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        UUID bankId = authorizationService.getBankIdFromContext();
        log.info("PUT /api/bank/notifications/{}/read - Bank {}", notificationId, bankId);
        
        notificationService.markAsRead(notificationId, bankId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        UUID bankId = authorizationService.getBankIdFromContext();
        log.info("PUT /api/bank/notifications/read-all - Bank {}", bankId);
        
        int count = notificationService.markAllAsRead(bankId);
        return ResponseEntity.ok(Map.of("markedCount", count));
    }
}
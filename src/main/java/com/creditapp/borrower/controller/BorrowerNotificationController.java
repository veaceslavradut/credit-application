package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.NotificationDTO;
import com.creditapp.shared.security.AuthorizationService;
import com.creditapp.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/borrower/notifications")
@RequiredArgsConstructor
@Slf4j
public class BorrowerNotificationController {
    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean read) {
        
        UUID borrowerId = authorizationService.getCurrentUserId();
        log.info("Fetching notifications for borrower: {}, page: {}, size: {}, read: {}", 
                borrowerId, page, size, read);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        
        Page<NotificationDTO> notifications;
        if (read != null) {
            if (read) {
                notifications = notificationService.getReadNotifications(borrowerId, pageable);
            } else {
                notifications = notificationService.getUnreadNotifications(borrowerId, pageable);
            }
        } else {
            notifications = notificationService.getNotifications(borrowerId, pageable);
        }
        
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        UUID borrowerId = authorizationService.getCurrentUserId();
        log.info("Marking notification as read: {}, borrower: {}", notificationId, borrowerId);
        
        notificationService.markAsRead(notificationId, borrowerId);
        
        return ResponseEntity.noContent().build();
    }
}

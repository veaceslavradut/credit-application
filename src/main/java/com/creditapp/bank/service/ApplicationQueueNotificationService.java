package com.creditapp.bank.service;

import com.creditapp.bank.dto.ApplicationQueueItem;
import com.creditapp.bank.websocket.ApplicationQueueWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationQueueNotificationService {

    private final ApplicationQueueWebSocketHandler webSocketHandler;

    /**
     * Notify all connected officers when a new application is added to their bank queue
     */
    public void notifyNewApplicationInQueue(UUID bankId, ApplicationQueueItem item) {
        try {
            webSocketHandler.broadcastApplicationUpdate(bankId.toString(), item);
            log.info("[QUEUE_NOTIFY] Notified bank {} about new application {}", bankId, item.getApplicationId());
        } catch (Exception e) {
            log.error("[QUEUE_NOTIFY] Failed to notify about new application", e);
        }
    }

    /**
     * Notify all connected officers about status change
     */
    public void notifyStatusChange(UUID bankId, UUID applicationId, String oldStatus, String newStatus) {
        try {
            webSocketHandler.broadcastStatusChange(
                bankId.toString(),
                applicationId.toString(),
                oldStatus,
                newStatus
            );
            log.info("[QUEUE_NOTIFY] Notified bank {} about status change for app {}", bankId, applicationId);
        } catch (Exception e) {
            log.error("[QUEUE_NOTIFY] Failed to notify about status change", e);
        }
    }
}

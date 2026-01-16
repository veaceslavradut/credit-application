package com.creditapp.shared.messaging;

import com.creditapp.shared.config.RabbitMQConfig;
import com.creditapp.shared.dto.NotificationEvent;
import com.creditapp.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * NotificationEventConsumer processes notification events from RabbitMQ
 * Implements retry logic with exponential backoff
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {
    
    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${notification.retry.backoff-minutes:1,5,15}")
    private String backoffMinutesConfig;
    
    private static final int MAX_RETRIES = 3;
    
    /**
     * Listen for notification events from queue
     * Process event and handle retries if needed
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: {} for recipient: {}", 
            event.getEventType(), event.getRecipientEmail());
        
        try {
            // Attempt to send email
            boolean success = notificationService.sendEmail(
                event.getRecipientEmail(),
                event.getTemplateName(),
                event.getVariables()
            );
            
            if (success) {
                log.info("Successfully processed notification event: {}", event.getEventType());
            } else {
                handleFailure(event);
            }
            
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
            handleFailure(event);
        }
    }
    
    /**
     * Handle notification failure with retry logic
     */
    private void handleFailure(NotificationEvent event) {
        int retryCount = event.getRetryCount() != null ? event.getRetryCount() : 0;
        
        if (retryCount < MAX_RETRIES) {
            // Calculate next retry time with exponential backoff
            List<Integer> backoffMinutes = parseBackoffMinutes();
            int backoffMinute = backoffMinutes.get(Math.min(retryCount, backoffMinutes.size() - 1));
            LocalDateTime nextRetryTime = LocalDateTime.now().plusMinutes(backoffMinute);
            
            // Update event for retry
            event.setRetryCount(retryCount + 1);
            event.setNextRetryTime(nextRetryTime);
            
            log.warn("Scheduling retry {} for event {} at {}", 
                event.getRetryCount(), event.getEventType(), nextRetryTime);
            
            // Republish to queue with delay (simplified - actual implementation would use delayed queue)
            try {
                Thread.sleep(backoffMinute * 60 * 1000); // Simple delay (use proper delayed queue in prod)
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    "notification.event",
                    event
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Retry scheduling interrupted: {}", e.getMessage());
            }
            
        } else {
            // Max retries exceeded, move to dead-letter queue
            log.error("Max retries exceeded for event: {}. Moving to DLQ.", event.getEventType());
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_DLQ, event);
        }
    }
    
    /**
     * Parse backoff minutes from config
     */
    private List<Integer> parseBackoffMinutes() {
        return Arrays.stream(backoffMinutesConfig.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .toList();
    }
}
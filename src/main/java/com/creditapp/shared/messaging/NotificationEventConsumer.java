package com.creditapp.shared.messaging;

import com.creditapp.shared.config.RabbitMQConfig;
import com.creditapp.shared.dto.NotificationEvent;
import com.creditapp.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * NotificationEventConsumer processes notification events from RabbitMQ
 * NOTE: Disabled for Story 2.9 - will be fully implemented in Story 1.9
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
     * Temporarily disabled - requires full email service from Story 1.9
     */
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: {} for recipient: {}", 
            event.getEventType(), event.getRecipientEmail());
        
        log.warn("NotificationEventConsumer is disabled - requires Story 1.9 email service");
    }
    
    private List<Integer> parseBackoffMinutes() {
        return Arrays.stream(backoffMinutesConfig.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .toList();
    }
}

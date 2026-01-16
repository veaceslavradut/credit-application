package com.creditapp.shared.messaging;

import com.creditapp.shared.config.RabbitMQConfig;
import com.creditapp.shared.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * NotificationEventPublisher publishes notification events to RabbitMQ
 * Async processing ensures non-blocking notification delivery
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Publish notification event to queue (async, non-blocking)
     * @param event notification event
     */
    @Async
    public void publishNotificationEvent(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.event",
                event
            );
            
            log.info("Published notification event: {} for recipient: {}", 
                event.getEventType(), event.getRecipientEmail());
            
        } catch (Exception e) {
            log.error("Failed to publish notification event: {}", e.getMessage(), e);
        }
    }
}
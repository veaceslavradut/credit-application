package com.creditapp.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for notification events
 * Defines queues, exchanges, and bindings for async notification processing
 */
@Configuration
@Slf4j
public class RabbitMQConfig {
    
    public static final String NOTIFICATION_QUEUE = "notification.events";
    public static final String NOTIFICATION_EXCHANGE = "notifications";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.*";
    public static final String NOTIFICATION_DLQ = "notification.events.dlq";
    
    /**
     * Create notification events queue
     * TTL: 7 days (messages expire if not consumed)
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-message-ttl", 604800000) // 7 days in milliseconds
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
            .build();
    }
    
    /**
     * Create dead-letter queue for failed messages
     */
    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }
    
    /**
     * Create topic exchange for notifications
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }
    
    /**
     * Bind notification queue to exchange
     */
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder
            .bind(notificationQueue)
            .to(notificationExchange)
            .with(NOTIFICATION_ROUTING_KEY);
    }
    
    /**
     * JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * Configure RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
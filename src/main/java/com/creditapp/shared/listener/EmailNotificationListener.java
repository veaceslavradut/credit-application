package com.creditapp.shared.listener;

import com.creditapp.auth.event.UserRegisteredEvent;
import com.creditapp.shared.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationListener.class);

    private final EmailService emailService;

    public EmailNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            logger.info("Handling UserRegistered event for user: {}", event.getEmail());
            emailService.sendRegistrationConfirmation(
                    event.getEmail(),
                    event.getUserName(),
                    event.getUserId()
            );
        } catch (Exception e) {
            logger.error("Error handling user registration event for user: {}", event.getEmail(), e);
        }
    }
}
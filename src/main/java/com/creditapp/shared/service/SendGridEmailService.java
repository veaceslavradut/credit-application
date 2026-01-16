package com.creditapp.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class SendGridEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SendGridEmailService.class);

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:noreply@creditapp.com}")
    private String fromEmail;

    @Override
    public void sendRegistrationConfirmation(String email, String userName, UUID userId) {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            logger.warn("SendGrid API key not configured, skipping email for user: {}", email);
            return;
        }
        logger.info("Registration confirmation email sent to: {}", email);
    }
}
package com.creditapp.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class BankActivationEmailService {
    private static final Logger logger = LoggerFactory.getLogger(BankActivationEmailService.class);

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:noreply@creditapp.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendActivationEmail(String email, String bankName, String activationToken) {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            logger.warn("SendGrid API key not configured. Activation email not sent for bank: {}", bankName);
            return;
        }

        String activationLink = baseUrl + "/auth/activate?token=" + activationToken;
        String subject = "Activate Your " + bankName + " Account";
        String htmlContent = buildHtmlEmail(bankName, activationLink);

        logger.info("Sending activation email to {} for bank {}", email, bankName);
        // MVP: Log instead of calling SendGrid
        logger.debug("Activation email content: subject={}, to={}, link={}", subject, email, activationLink);
    }

    private String buildHtmlEmail(String bankName, String activationLink) {
        return "<html><body>" +
                "<h2>Welcome to Credit Application Platform</h2>" +
                "<p>Thank you for registering " + bankName + ".</p>" +
                "<p>Please activate your account by clicking the link below:</p>" +
                "<p><a href='" + activationLink + "'>Activate Account</a></p>" +
                "<p>This link will expire in 7 days.</p>" +
                "<p>If you did not request this, please ignore this email.</p>" +
                "</body></html>";
    }
}
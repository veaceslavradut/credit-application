package com.creditapp.shared.service;

import com.sendgrid.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Mock email service for development and testing
 * Logs emails to console instead of sending via SendGrid
 */
@Service
@ConditionalOnProperty(name = "sendgrid.enabled", havingValue = "false")
@Slf4j
public class MockEmailService implements EmailService {
    
    private final List<MockEmail> sentEmails = Collections.synchronizedList(new ArrayList<>());
    
    @Override
    public void sendRegistrationConfirmation(String email, String userName, UUID userId) {
        String subject = "Welcome to Credit Application Platform!";
        String htmlContent = String.format(
            "<html><body><h1>Welcome, %s!</h1><p>Your account has been successfully created.</p>" +
            "<p>User ID: %s</p></body></html>",
            userName, userId
        );
        String textContent = String.format(
            "Welcome, %s! Your account has been successfully created. User ID: %s",
            userName, userId
        );
        sendEmail(email, subject, htmlContent, textContent);
    }
    
    @Override
    public Response sendEmail(String toEmail, String subject, String htmlContent, String textContent) {
        log.info("[MOCK EMAIL] To: {}", toEmail);
        log.info("[MOCK EMAIL] Subject: {}", subject);
        log.info("[MOCK EMAIL] Body preview: {}", 
            htmlContent != null && htmlContent.length() > 100 
                ? htmlContent.substring(0, 100) + "..." 
                : htmlContent);
        
        MockEmail mockEmail = new MockEmail(toEmail, subject, htmlContent, textContent);
        sentEmails.add(mockEmail);
        
        // Return mock response with status 202 (Accepted)
        Response response = new Response();
        response.setStatusCode(202);
        response.setBody("");
        
        return response;
    }
    
    /**
     * Get list of sent emails for testing
     */
    public List<MockEmail> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }
    
    /**
     * Clear sent emails list
     */
    public void clearSentEmails() {
        sentEmails.clear();
    }
    
    /**
     * Mock email record
     */
    public record MockEmail(String toEmail, String subject, String htmlContent, String textContent) {
    }
}
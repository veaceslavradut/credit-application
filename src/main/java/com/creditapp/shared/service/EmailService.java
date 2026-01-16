package com.creditapp.shared.service;

import com.sendgrid.Response;

import java.util.UUID;

/**
 * Email service interface for sending emails
 * Implementations can use SendGrid or mock service
 */
public interface EmailService {
    
    /**
     * Send registration confirmation email
     * @param email recipient email
     * @param userName user name
     * @param userId user ID
     */
    void sendRegistrationConfirmation(String email, String userName, UUID userId);
    
    /**
     * Send email with custom content
     * @param toEmail recipient email
     * @param subject email subject
     * @param htmlContent HTML email body
     * @param textContent plain text email body
     * @return SendGrid Response object
     */
    Response sendEmail(String toEmail, String subject, String htmlContent, String textContent);
}
package com.creditapp.shared.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

/**
 * SendGrid email service implementation
 * Sends emails via SendGrid API
 */
@Service
@ConditionalOnProperty(name = "sendgrid.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailService implements EmailService {
    
    private final SendGrid sendGridClient;
    
    @Value("${sendgrid.from-email}")
    private String fromEmail;
    
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
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content htmlContentObj = new Content("text/html", htmlContent);
        Content textContentObj = new Content("text/plain", textContent);
        
        Mail mail = new Mail(from, subject, to, htmlContentObj);
        mail.addContent(textContentObj);
        
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sendGridClient.api(request);
            
            log.info("Email sent to {} via SendGrid. Status: {}", toEmail, response.getStatusCode());
            
            return response;
        } catch (IOException e) {
            log.error("Failed to send email to {} via SendGrid: {}", toEmail, e.getMessage());
            throw new RuntimeException("SendGrid email sending failed", e);
        }
    }
}
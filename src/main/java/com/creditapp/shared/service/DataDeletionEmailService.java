package com.creditapp.shared.service;

import com.creditapp.shared.model.DeletionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataDeletionEmailService {
    
    private final EmailService emailService;
    
    @Value("${app.base-url:https://creditapp.com}")
    private String baseUrl;
    
    @Async
    public void sendDeletionConfirmationEmail(DeletionRequest request, String borrowerEmail, String borrowerName) {
        log.info("Sending deletion confirmation email to {}", borrowerEmail);
        try {
            String confirmationLink = baseUrl + "/api/borrower/data-deletion/confirm?token=" + request.getConfirmationToken();
            String htmlContent = "<h2>Data Deletion Request Confirmation</h2>" +
                    "<p>Dear " + (borrowerName != null ? borrowerName : "Valued Customer") + ",</p>" +
                    "<p>Your request to delete your account and associated data has been received.</p>" +
                    "<p>You have 7 days to confirm this request. Click the link below to confirm:</p>" +
                    "<p><a href=\"" + confirmationLink + "\">Confirm Deletion</a></p>" +
                    "<p>If you did not request this, simply ignore this email.</p>";
            String textContent = "Data Deletion Request Confirmation\n\n" +
                    "You have 7 days to confirm your deletion request at:\n" +
                    confirmationLink;
            emailService.sendEmail(borrowerEmail, "Data Deletion Confirmation", htmlContent, textContent);
            log.info("Deletion confirmation email sent to {}", borrowerEmail);
        } catch (Exception e) {
            log.error("Failed to send deletion confirmation email", e);
        }
    }
    
    @Async
    public void sendDeletionCompletedEmail(String borrowerEmail, String borrowerName) {
        log.info("Sending deletion completed email to {}", borrowerEmail);
        try {
            String htmlContent = "<h2>Your Account Has Been Deleted</h2>" +
                    "<p>Dear " + (borrowerName != null ? borrowerName : "Valued Customer") + ",</p>" +
                    "<p>Your account and personal data have been successfully deleted from our system.</p>" +
                    "<p>If you wish to rejoin us in the future, you can create a new account.</p>";
            String textContent = "Your Account Has Been Deleted\n\n" +
                    "Your account and personal data have been successfully deleted.";
            emailService.sendEmail(borrowerEmail, "Account Deletion Completed", htmlContent, textContent);
            log.info("Deletion completed email sent to {}", borrowerEmail);
        } catch (Exception e) {
            log.error("Failed to send deletion completed email", e);
        }
    }
    
    @Async
    public void sendDeletionCancelledEmail(String borrowerEmail, String borrowerName) {
        log.info("Sending deletion cancelled email to {}", borrowerEmail);
        try {
            String htmlContent = "<h2>Your Deletion Request Has Been Cancelled</h2>" +
                    "<p>Dear " + (borrowerName != null ? borrowerName : "Valued Customer") + ",</p>" +
                    "<p>Your account deletion request has been cancelled. Your account remains active.</p>";
            String textContent = "Your Deletion Request Has Been Cancelled\n\n" +
                    "Your account deletion request has been cancelled and your account is still active.";
            emailService.sendEmail(borrowerEmail, "Deletion Request Cancelled", htmlContent, textContent);
            log.info("Deletion cancelled email sent to {}", borrowerEmail);
        } catch (Exception e) {
            log.error("Failed to send deletion cancelled email", e);
        }
    }
}
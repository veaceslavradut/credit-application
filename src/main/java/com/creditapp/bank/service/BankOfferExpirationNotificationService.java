package com.creditapp.bank.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for sending expiration notifications to banks when offers are about to expire.
 * Part of Story 4.6 - Offer Expiration Notification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankOfferExpirationNotificationService {
    
    private final OrganizationRepository organizationRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Send expiration notification to bank via email.
     * Runs asynchronously to avoid blocking the batch job.
     * 
     * @param offer The offer that is about to expire
     * @return true if notification sent successfully, false otherwise
     */
    @Async
    public void notifyBankOfExpiration(Offer offer) {
        try {
            // Fetch bank organization
            Organization bank = organizationRepository.findById(offer.getBankId())
                    .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + offer.getBankId()));
            
            // Fetch application for borrower details
            Application application = applicationRepository.findById(offer.getApplicationId())
                    .orElseThrow(() -> new IllegalArgumentException("Application not found: " + offer.getApplicationId()));
            
            // Calculate hours remaining until expiration
            long hoursRemaining = Duration.between(LocalDateTime.now(), offer.getExpiresAt()).toHours();
            
            // Build email content
            String subject = String.format("Offer Expiring Soon - Application %s", application.getId());
            
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><body>");
            htmlContent.append("<h2>Offer Expiration Warning</h2>");
            htmlContent.append("<p>Your loan offer is about to expire:</p>");
            htmlContent.append("<ul>");
            htmlContent.append("<li><strong>Application ID:</strong> ").append(application.getId()).append("</li>");
            htmlContent.append("<li><strong>Borrower Loan Amount:</strong> $").append(application.getLoanAmount()).append("</li>");
            htmlContent.append("<li><strong>Your APR:</strong> ").append(offer.getApr()).append("%</li>");
            htmlContent.append("<li><strong>Monthly Payment:</strong> $").append(offer.getMonthlyPayment()).append("</li>");
            htmlContent.append("<li><strong>Hours Remaining:</strong> ").append(hoursRemaining).append(" hours</li>");
            htmlContent.append("</ul>");
            htmlContent.append("<p><strong>Actions:</strong></p>");
            htmlContent.append("<p><a href=\"").append(baseUrl).append("/api/bank/applications/").append(application.getId()).append("\">Review Application</a></p>");
            htmlContent.append("<p><a href=\"").append(baseUrl).append("/api/bank/offers/").append(offer.getId()).append("/resubmit").append("\">Resubmit Offer</a></p>");
            htmlContent.append("<p>If no action is taken, the offer will expire automatically.</p>");
            htmlContent.append("</body></html>");
            
            String textContent = String.format(
                    "Offer Expiration Warning\n\n" +
                            "Your loan offer is about to expire:\n" +
                            "Application ID: %s\n" +
                            "Borrower Loan Amount: $%s\n" +
                            "Your APR: %s%%\n" +
                            "Monthly Payment: $%s\n" +
                            "Hours Remaining: %d hours\n\n" +
                            "Review Application: %s/api/bank/applications/%s\n" +
                            "Resubmit Offer: %s/api/bank/offers/%s/resubmit\n\n" +
                            "If no action is taken, the offer will expire automatically.",
                    application.getId(),
                    application.getLoanAmount(),
                    offer.getApr(),
                    offer.getMonthlyPayment(),
                    hoursRemaining,
                    baseUrl, application.getId(),
                    baseUrl, offer.getId()
            );
            
            // Send email
            emailService.sendEmail(bank.getContactEmail(), subject, htmlContent.toString(), textContent);
            
            // Create audit log
            auditService.logAction(
                    "OFFER",
                    offer.getId(),
                    com.creditapp.shared.model.AuditAction.OFFER_EXPIRATION_NOTIFICATION_SENT,
                    offer.getBankId(),
                    "BANK"
            );
            
            log.info("Offer expiration notification sent to bank {}. OfferId: {}, ApplicationId: {}, Hours remaining: {}",
                    bank.getName(), offer.getId(), application.getId(), hoursRemaining);
                    
        } catch (Exception e) {
            log.error("Failed to send offer expiration notification. OfferId: {}, Error: {}",
                    offer.getId(), e.getMessage(), e);
        }
    }
}

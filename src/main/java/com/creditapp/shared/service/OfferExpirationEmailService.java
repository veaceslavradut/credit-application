package com.creditapp.shared.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.borrower.model.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for sending notifications when offers expire.
 * Handles both borrower and bank notifications.
 */
@Slf4j
@Service
public class OfferExpirationEmailService {

    private final EmailService emailService;

    public OfferExpirationEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Send expiration notification to borrower when their selected offer expires.
     * Non-blocking: runs asynchronously.
     */
    @Async
    public void sendOfferExpiredToBorrower(UUID borrowerId, Offer offer, Application application) {
        try {
            String subject = "Your selected loan offer has expired";
            StringBuilder sb = new StringBuilder();
            sb.append("Dear Borrower,\n\n");
            sb.append("Unfortunately, the loan offer you selected has expired on ").append(offer.getExpiresAt()).append(".\n\n");
            sb.append("Offer Details:\n");
            sb.append("Annual Percentage Rate (APR): ").append(offer.getApr()).append("%\n");
            sb.append("Monthly Payment: ").append(offer.getMonthlyPayment()).append("\n");
            sb.append("Total Cost: ").append(offer.getTotalCost()).append("\n\n");
            sb.append("Next Steps:\n");
            sb.append("You can request new offers by visiting your account dashboard and clicking 'Recalculate Offers'.\n");
            sb.append("This will allow banks to provide you with fresh rates based on current market conditions.\n\n");
            sb.append("If you have any questions, please contact our support team.\n\n");
            sb.append("Best regards,\n");
            sb.append("Credit Application Team");

            String borrowerEmail = "borrower@example.com";
            String textContent = "Your selected offer has expired. Please recalculate offers to get fresh quotes.";
            emailService.sendEmail(borrowerEmail, subject, sb.toString(), textContent);
            log.info("Offer expiration email sent to borrower. BorrowerId: {}, OfferId: {}, ApplicationId: {}", 
                    borrowerId, offer.getId(), application.getId());
        } catch (Exception e) {
            log.error("Failed to send offer expiration email to borrower. BorrowerId: {}, OfferId: {}, Error: {}",
                    borrowerId, offer.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send expiration notification to bank when a borrower's selected offer expires.
     * Non-blocking: runs asynchronously.
     */
    @Async
    public void sendOfferExpiredToBank(UUID bankId, Offer offer, Application application) {
        try {
            String bankEmail = "offers@bank.com";

            String subject = "A borrower's selected offer has expired";
            StringBuilder sb = new StringBuilder();
            sb.append("An alert: A borrower's selected offer has expired.\n\n");
            sb.append("Application ID: ").append(offer.getApplicationId()).append("\n");
            sb.append("Offer ID: ").append(offer.getId()).append("\n");
            sb.append("Offer APR: ").append(offer.getApr()).append("%\n");
            sb.append("Monthly Payment: ").append(offer.getMonthlyPayment()).append("\n");
            sb.append("Expiration Time: ").append(offer.getExpiresAt()).append("\n\n");
            sb.append("What this means:\n");
            sb.append("The borrower's application status has been reverted to 'Submitted'.\n");
            sb.append("You may provide new offers if the borrower requests a recalculation.\n\n");
            sb.append("Best regards,\n");
            sb.append("Credit Application System");

            String textContent = "A borrower's selected offer has expired. Application status reverted to Submitted.";
            emailService.sendEmail(bankEmail, subject, sb.toString(), textContent);
            log.info("Offer expiration notification sent to bank. BankId: {}, OfferId: {}, ApplicationId: {}",
                    bankId, offer.getId(), application.getId());
        } catch (Exception e) {
            log.error("Failed to send offer expiration email to bank. BankId: {}, OfferId: {}, Error: {}",
                    bankId, offer.getId(), e.getMessage(), e);
        }
    }
}
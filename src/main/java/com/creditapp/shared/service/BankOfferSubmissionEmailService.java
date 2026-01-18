package com.creditapp.shared.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.borrower.model.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankOfferSubmissionEmailService {

    private final EmailService emailService;

    @Async
    public void sendOfferSubmittedByBankNotification(
            Offer offer,
            Application application,
            UUID officerId,
            String bankName,
            String officerName) {

        try {
            String subject = String.format("A new offer from %s has been submitted", bankName);

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><body>");
            htmlContent.append("<h2>New Loan Offer Submitted</h2>");
            htmlContent.append("<p>Dear Borrower,</p>");
            htmlContent.append("<p>").append(bankName).append(" has submitted a new offer for your loan application.</p>");

            htmlContent.append("<h3>Offer Details:</h3>");
            htmlContent.append("<ul>");
            htmlContent.append("<li><strong>Annual Percentage Rate (APR):</strong> ").append(offer.getApr()).append("%</li>");
            htmlContent.append("<li><strong>Monthly Payment:</strong> $").append(offer.getMonthlyPayment()).append("</li>");
            htmlContent.append("<li><strong>Total Cost:</strong> $").append(offer.getTotalCost()).append("</li>");
            htmlContent.append("<li><strong>Loan Amount:</strong> $").append(application.getLoanAmount()).append("</li>");
            htmlContent.append("<li><strong>Processing Time:</strong> ").append(offer.getProcessingTimeDays()).append(" days</li>");
            htmlContent.append("</ul>");

            if (offer.getSubmissionNotes() != null && !offer.getSubmissionNotes().isEmpty()) {
                htmlContent.append("<h3>Officer Notes:</h3>");
                htmlContent.append("<p>").append(offer.getSubmissionNotes()).append("</p>");
            }

            htmlContent.append("<h3>Loan Officer Contact:</h3>");
            htmlContent.append("<p><strong>Name:</strong> ").append(officerName).append("</p>");
            htmlContent.append("<p>If you have any questions, please feel free to contact your loan officer.</p>");

            htmlContent.append("<h3>Next Steps:</h3>");
            htmlContent.append("<ol>");
            htmlContent.append("<li>Log in to your account to view and compare all offers</li>");
            htmlContent.append("<li>Review the offer terms carefully</li>");
            htmlContent.append("<li>Select this offer if it meets your needs</li>");
            htmlContent.append("</ol>");

            htmlContent.append("<p><a href=\"https://creditapp.example.com/applications/").append(application.getId()).append("\">View Your Application</a></p>");

            htmlContent.append("<p>This offer expires on <strong>").append(offer.getExpiresAt()).append("</strong>.</p>");

            htmlContent.append("<p>Best regards,<br/>");
            htmlContent.append(bankName).append(" Team</p>");
            htmlContent.append("</body></html>");

            // Plain text version
            StringBuilder textContent = new StringBuilder();
            textContent.append("New Loan Offer Submitted\n\n");
            textContent.append("Dear Borrower,\n\n");
            textContent.append(bankName).append(" has submitted a new offer for your loan application.\n\n");
            textContent.append("Offer Details:\n");
            textContent.append("- Annual Percentage Rate (APR): ").append(offer.getApr()).append("%\n");
            textContent.append("- Monthly Payment: $").append(offer.getMonthlyPayment()).append("\n");
            textContent.append("- Total Cost: $").append(offer.getTotalCost()).append("\n");
            textContent.append("- Loan Amount: $").append(application.getLoanAmount()).append("\n");
            textContent.append("- Processing Time: ").append(offer.getProcessingTimeDays()).append(" days\n\n");

            if (offer.getSubmissionNotes() != null && !offer.getSubmissionNotes().isEmpty()) {
                textContent.append("Officer Notes:\n");
                textContent.append(offer.getSubmissionNotes()).append("\n\n");
            }

            textContent.append("Loan Officer: ").append(officerName).append("\n\n");
            textContent.append("Next Steps:\n");
            textContent.append("1. Log in to your account to view and compare all offers\n");
            textContent.append("2. Review the offer terms carefully\n");
            textContent.append("3. Select this offer if it meets your needs\n\n");
            textContent.append("This offer expires on ").append(offer.getExpiresAt()).append(".\n\n");
            textContent.append("Best regards,\n");
            textContent.append(bankName).append(" Team");

            // Get borrower email from application (assuming it's stored there)
            String borrowerEmail = "borrower@example.com"; // TODO: Get from application

            emailService.sendEmail(borrowerEmail, subject, htmlContent.toString(), textContent.toString());

            log.info("[OFFER_EMAIL] Sent offer submission notification to borrower. OfferId: {}, ApplicationId: {}, BankId: {}, OfficerId: {}",
                    offer.getId(), application.getId(), offer.getBankId(), officerId);

        } catch (Exception e) {
            log.error("[OFFER_EMAIL] Failed to send offer submission notification. OfferId: {}, ApplicationId: {}, Error: {}",
                    offer.getId(), application.getId(), e.getMessage(), e);
            // Don't throw - email failure shouldn't fail the transaction
        }
    }
}
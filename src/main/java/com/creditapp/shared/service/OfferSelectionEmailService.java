package com.creditapp.shared.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.borrower.model.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class OfferSelectionEmailService {

    private final EmailService emailService;

    public OfferSelectionEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    public void sendOfferSelectedToBorrower(UUID borrowerId, Offer offer, Application application) {
        try {
            String subject = "You have selected an offer for your loan application";
            StringBuilder sb = new StringBuilder();
            sb.append("Dear Borrower,\n\n");
            sb.append("You have selected an offer with the following terms:\n");
            sb.append("Annual Percentage Rate (APR): ").append(offer.getApr()).append("%\n");
            sb.append("Monthly Payment: ").append(offer.getMonthlyPayment()).append("\n");
            sb.append("Total Cost: ").append(offer.getTotalCost()).append("\n\n");
            sb.append("Next Steps:\n");
            sb.append("1. Review the offer terms and conditions\n");
            sb.append("2. Prepare required documents\n");
            sb.append("3. Our loan officer will contact you shortly\n\n");
            sb.append("Please log in to your account to view more details and track the progress of your application.\n\n");
            sb.append("Best regards,\n");
            sb.append("Credit Application Team");

            String borrowerEmail = "borrower@example.com";
            String textContent = "You have selected an offer. Please review the details in your account.";
            emailService.sendEmail(borrowerEmail, subject, sb.toString(), textContent);
            log.info("Offer selection email sent to borrower. BorrowerId: {}, OfferId: {}", borrowerId, offer.getId());
        } catch (Exception e) {
            log.error("Failed to send offer selection email to borrower. BorrowerId: {}, OfferId: {}, Error: {}",
                    borrowerId, offer.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendOfferSelectedToBank(UUID bankId, Offer offer, UUID borrowerId) {
        try {
            String bankEmail = "offers@bank.com";

            String subject = "Borrower has selected your loan offer";
            StringBuilder sb = new StringBuilder();
            sb.append("An alert: A borrower has selected the offer you created.\n\n");
            sb.append("Application ID: ").append(offer.getApplicationId()).append("\n");
            sb.append("Offer APR: ").append(offer.getApr()).append("%\n");
            sb.append("Monthly Payment: ").append(offer.getMonthlyPayment()).append("\n\n");
            sb.append("Please reach out to the borrower to proceed with the next steps.\n\n");
            sb.append("Best regards,\n");
            sb.append("Credit Application System");

            String textContent = "A borrower has selected your offer. Please follow up.";
            emailService.sendEmail(bankEmail, subject, sb.toString(), textContent);
            log.info("Offer selection notification sent to bank. BankId: {}, OfferId: {}, BorrowerId: {}",
                    bankId, offer.getId(), borrowerId);
        } catch (Exception e) {
            log.error("Failed to send offer selection email to bank. BankId: {}, OfferId: {}, Error: {}",
                    bankId, offer.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendOfferDeselectedToBank(UUID bankId, Offer offer, UUID borrowerId) {
        try {
            String bankEmail = "offers@bank.com";

            String subject = "Borrower has deselected your loan offer";
            StringBuilder sb = new StringBuilder();
            sb.append("A borrower has deselected the offer you created.\n\n");
            sb.append("Application ID: ").append(offer.getApplicationId()).append("\n");
            sb.append("Deselected Offer APR: ").append(offer.getApr()).append("%\n\n");
            sb.append("The borrower may have selected a competitive offer from another lender.\n\n");
            sb.append("Best regards,\n");
            sb.append("Credit Application System");

            String textContent = "A borrower has deselected your offer.";
            emailService.sendEmail(bankEmail, subject, sb.toString(), textContent);
            log.info("Offer deselection notification sent to bank. BankId: {}, OfferId: {}, BorrowerId: {}",
                    bankId, offer.getId(), borrowerId);
        } catch (Exception e) {
            log.error("Failed to send offer deselection email to bank. BankId: {}, OfferId: {}, Error: {}",
                    bankId, offer.getId(), e.getMessage(), e);
        }
    }
}
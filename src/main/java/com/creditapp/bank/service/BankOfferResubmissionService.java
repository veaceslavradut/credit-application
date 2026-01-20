package com.creditapp.bank.service;

import com.creditapp.bank.dto.ResubmitOfferFormResponse;
import com.creditapp.bank.dto.ResubmitOfferRequest;
import com.creditapp.bank.dto.ResubmitOfferResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.EmailService;
import com.creditapp.shared.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for offer resubmission functionality
 * Story 4.6: Offer Expiration Notification - Tasks 5 & 6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankOfferResubmissionService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    /**
     * Get resubmit form with previous offer details pre-filled
     * Task 5: GET /api/bank/offers/{offerId}/resubmit
     */
    public ResubmitOfferFormResponse getResubmitForm(UUID offerId, UUID bankId) {
        log.info("Getting resubmit form for offer {}", offerId);

        // Fetch offer
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));

        // Verify bank ownership
        if (!offer.getBankId().equals(bankId)) {
            throw new SecurityException("Bank " + bankId + " is not authorized to resubmit offer " + offerId);
        }

        // Fetch application context
        Application application = applicationRepository.findById(offer.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + offer.getApplicationId()));

        // Check expiration status
        LocalDateTime now = LocalDateTime.now();
        boolean isExpired = offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(now);
        
        String warningMessage = null;
        if (isExpired) {
            warningMessage = "This offer has expired. Resubmitting will create a new offer with a fresh expiration date.";
        } else if (offer.getExpiresAt() != null) {
            long hoursRemaining = java.time.Duration.between(now, offer.getExpiresAt()).toHours();
            if (hoursRemaining < 24) {
                warningMessage = String.format("This offer expires in %d hours. Consider resubmitting to extend the validity period.", hoursRemaining);
            }
        }

        return ResubmitOfferFormResponse.builder()
                .offerId(offer.getId())
                .applicationId(offer.getApplicationId())
                .bankId(offer.getBankId())
                .previousApr(offer.getApr())
                .previousOriginationFee(offer.getOriginationFee())
                .previousProcessingTimeDays(offer.getProcessingTimeDays())
                .previousValidityPeriodDays(offer.getValidityPeriodDays())
                .previousMonthlyPayment(offer.getMonthlyPayment())
                .previousTotalCost(offer.getTotalCost())
                .loanAmount(application.getLoanAmount())
                .loanTermMonths(application.getLoanTermMonths())
                .expiresAt(offer.getExpiresAt())
                .isExpired(isExpired)
                .warningMessage(warningMessage)
                .build();
    }

    /**
     * Process offer resubmission with updated values
     * Task 6: POST /api/bank/offers/{offerId}/resubmit
     */
    @Transactional
    public ResubmitOfferResponse resubmitOffer(UUID offerId, UUID bankId, ResubmitOfferRequest request) {
        log.info("Resubmitting offer {} with updated values", offerId);

        // Fetch old offer
        Offer oldOffer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));

        // Verify bank ownership
        if (!oldOffer.getBankId().equals(bankId)) {
            throw new SecurityException("Bank " + bankId + " is not authorized to resubmit offer " + offerId);
        }

        // Fetch application
        Application application = applicationRepository.findById(oldOffer.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + oldOffer.getApplicationId()));

        // Validate APR range
        if (request.getApr().compareTo(new BigDecimal("0.5")) < 0 || 
            request.getApr().compareTo(new BigDecimal("50.0")) > 0) {
            throw new IllegalArgumentException("APR must be between 0.5% and 50%");
        }

        // Recalculate monthly payment and total cost
        BigDecimal monthlyPayment = CalculationUtils.calculateMonthlyPayment(
                application.getLoanAmount(),
                application.getLoanTermMonths(),
                request.getApr()
        );

        BigDecimal totalCost = monthlyPayment
                .multiply(new BigDecimal(application.getLoanTermMonths()))
                .add(request.getOriginationFee() != null ? request.getOriginationFee() : BigDecimal.ZERO);

        // Create new offer
        Offer newOffer = new Offer();
        newOffer.setId(UUID.randomUUID());
        newOffer.setApplicationId(oldOffer.getApplicationId());
        newOffer.setBankId(oldOffer.getBankId());
        newOffer.setOfferStatus(OfferStatus.SUBMITTED);
        newOffer.setApr(request.getApr());
        newOffer.setOriginationFee(request.getOriginationFee() != null ? request.getOriginationFee() : oldOffer.getOriginationFee());
        newOffer.setProcessingTimeDays(request.getProcessingTimeDays() != null ? request.getProcessingTimeDays() : oldOffer.getProcessingTimeDays());
        newOffer.setValidityPeriodDays(request.getValidityPeriodDays() != null ? request.getValidityPeriodDays() : 1);
        newOffer.setMonthlyPayment(monthlyPayment);
        newOffer.setTotalCost(totalCost);
        newOffer.setInsuranceCost(oldOffer.getInsuranceCost());
        newOffer.setNotified(false); // Reset notification flag
        
        // Set new expiration date
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(newOffer.getValidityPeriodDays());
        newOffer.setExpiresAt(newExpiresAt);

        // Save new offer
        offerRepository.save(newOffer);
        log.info("Created new offer {} for resubmission", newOffer.getId());

        // Mark old offer as EXPIRED
        oldOffer.setOfferStatus(OfferStatus.EXPIRED);
        offerRepository.save(oldOffer);
        log.info("Marked old offer {} as EXPIRED", oldOffer.getId());

        // Send email to borrower
        String borrowerNotificationStatus = "SENT";
        try {
            // Get borrower email from User relationship
            application.getBorrower(); // Force lazy load
            String borrowerEmail = application.getBorrower().getEmail();
            String subject = "Updated Loan Offer from Bank"; // In real scenario, fetch bank name
            String htmlBody = buildResubmitEmailHtml(application, newOffer);
            String textBody = buildResubmitEmailText(application, newOffer);

            emailService.sendEmail(borrowerEmail, subject, htmlBody, textBody);
            log.info("Sent resubmit notification email to borrower {}", borrowerEmail);
        } catch (Exception e) {
            log.error("Failed to send resubmit notification email", e);
            borrowerNotificationStatus = "FAILED: " + e.getMessage();
        }

        // Audit log
        auditService.logAction(
                "Offer",
                newOffer.getId(),
                AuditAction.OFFER_RESUBMITTED,
                bankId,
                "BANK_ADMIN"
        );

        return ResubmitOfferResponse.builder()
                .newOfferId(newOffer.getId())
                .oldOfferId(oldOffer.getId())
                .applicationId(newOffer.getApplicationId())
                .apr(newOffer.getApr())
                .originationFee(newOffer.getOriginationFee())
                .processingTimeDays(newOffer.getProcessingTimeDays())
                .validityPeriodDays(newOffer.getValidityPeriodDays())
                .monthlyPayment(newOffer.getMonthlyPayment())
                .totalCost(newOffer.getTotalCost())
                .expiresAt(newOffer.getExpiresAt())
                .status("SUBMITTED")
                .borrowerNotificationStatus(borrowerNotificationStatus)
                .message("Offer successfully resubmitted with updated values")
                .build();
    }

    private String buildResubmitEmailHtml(Application application, Offer newOffer) {
        return String.format("""
                <html>
                <body>
                    <h2>Updated Loan Offer</h2>
                    <p>Good news! A bank has updated their offer for your loan application.</p>
                    
                    <h3>Application Details:</h3>
                    <ul>
                        <li>Loan Amount: $%,.2f</li>
                        <li>Term: %d months</li>
                    </ul>
                    
                    <h3>Updated Offer:</h3>
                    <ul>
                        <li><strong>APR: %.2f%%</strong></li>
                        <li>Monthly Payment: $%,.2f</li>
                        <li>Total Cost: $%,.2f</li>
                        <li>Processing Time: %d days</li>
                        <li>Expires: %s</li>
                    </ul>
                    
                    <p>
                        <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; display: inline-block;">
                            View Offer
                        </a>
                    </p>
                    
                    <p>This offer is valid until %s. Please review and compare it with other offers.</p>
                </body>
                </html>
                """,
                application.getLoanAmount(),
                application.getLoanTermMonths(),
                newOffer.getApr(),
                newOffer.getMonthlyPayment(),
                newOffer.getTotalCost(),
                newOffer.getProcessingTimeDays(),
                newOffer.getExpiresAt(),
                "https://creditapp.com/applications/" + application.getId() + "/offers",
                newOffer.getExpiresAt()
        );
    }

    private String buildResubmitEmailText(Application application, Offer newOffer) {
        return String.format("""
                Updated Loan Offer
                
                Good news! A bank has updated their offer for your loan application.
                
                Application Details:
                - Loan Amount: $%,.2f
                - Term: %d months
                
                Updated Offer:
                - APR: %.2f%%
                - Monthly Payment: $%,.2f
                - Total Cost: $%,.2f
                - Processing Time: %d days
                - Expires: %s
                
                View offer: https://creditapp.com/applications/%s/offers
                
                This offer is valid until %s. Please review and compare it with other offers.
                """,
                application.getLoanAmount(),
                application.getLoanTermMonths(),
                newOffer.getApr(),
                newOffer.getMonthlyPayment(),
                newOffer.getTotalCost(),
                newOffer.getProcessingTimeDays(),
                newOffer.getExpiresAt(),
                application.getId(),
                newOffer.getExpiresAt()
        );
    }
}
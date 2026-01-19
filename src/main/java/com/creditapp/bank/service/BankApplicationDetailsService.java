package com.creditapp.bank.service;

import com.creditapp.bank.dto.*;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.ApplicationBankNotes;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.repository.ApplicationBankNotesRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationDetails;
import com.creditapp.borrower.model.Consent;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.repository.ConsentRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for retrieving complete application details for bank review panel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankApplicationDetailsService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final ApplicationBankNotesRepository notesRepository;
    private final ConsentRepository consentRepository;
    private final AuditService auditService;

    /**
     * Get full application details for bank review panel
     * Includes borrower info, loan details, employment, consents, and offer
     * Marks application as VIEWED with audit logging
     *
     * @param bankId bank admin's organization id
     * @param applicationId the application to retrieve
     * @return complete application details response
     */
    @Cacheable(value = "applicationDetails", key = "#bankId + '-' + #applicationId", unless = "#result == null")
    public ApplicationDetailsResponse getApplicationDetails(UUID bankId, UUID applicationId) {
        long startTime = System.currentTimeMillis();
        
        // Fetch application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Verify bank has an offer for this application
        Offer bankOffer = offerRepository.findByApplicationIdAndBankId(applicationId, bankId)
                .orElseThrow(() -> new IllegalArgumentException("Bank has no offer for this application"));

        // Mark application as VIEWED if not already declined
        markApplicationAsViewed(bankId, application);

        // Fetch borrower
        User borrower = userRepository.findById(application.getBorrowerId())
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found"));

        // Build borrower details
        BorrowerDetailsDTO borrowerDetails = BorrowerDetailsDTO.builder()
                .firstName(borrower.getFirstName())
                .lastName(borrower.getLastName())
                .email(borrower.getEmail())
                .phone(borrower.getPhoneNumber())
                .address("")
                .build();

        // Build loan request details
        LoanRequestDetailsDTO loanRequest = LoanRequestDetailsDTO.builder()
                .loanType(application.getLoanType())
                .amount(application.getLoanAmount())
                .termMonths(application.getLoanTermMonths())
                .purpose("")
                .incomeDocumentationStatus("PENDING")
                .build();

        // Build employment details from ApplicationDetails
        ApplicationDetails appDetails = application.getDetails();
        EmploymentDetailsDTO employment = EmploymentDetailsDTO.builder()
                .employer("")
                .position("")
                .annualIncome(appDetails != null ? appDetails.getAnnualIncome() : null)
                .yearsEmployed(0)
                .build();

        // Build consent details (3 standard consents) from database
        List<ConsentDetailsDTO> consents = buildConsents(applicationId);

        // Build offer DTO from bank's offer
        OfferDTO offerDto = buildOfferDTO(bankOffer);

        // Fetch internal notes (bank staff only)
        String internalNotes = notesRepository.findByApplicationIdAndBankId(applicationId, bankId)
                .map(ApplicationBankNotes::getNotes)
                .orElse("");

        // Build response
        ApplicationDetailsResponse response = ApplicationDetailsResponse.builder()
                .applicationId(applicationId)
                .borrower(borrowerDetails)
                .loanRequest(loanRequest)
                .employment(employment)
                .consents(consents)
                .offer(offerDto)
                .internalNotes(internalNotes)
                .build();

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("Retrieved application details for app {} from bank {} in {}ms", applicationId, bankId, elapsed);

        if (elapsed > 200) {
            log.warn("Application details retrieval exceeded 200ms SLA: {}ms", elapsed);
        }

        return response;
    }

    private List<ConsentDetailsDTO> buildConsents(UUID applicationId) {
        List<ConsentDetailsDTO> consents = new ArrayList<>();
        
        // Fetch consents from database for this application
        List<Consent> databaseConsents = consentRepository.findByApplicationId(applicationId);
        
        // Define standard consent templates
        String[] consentTexts = {
                "I authorize the bank to check my credit report and history",
                "I authorize the bank to share my information with third-party service providers",
                "I agree to the loan service terms and conditions"
        };
        
        // Build DTOs for each of the 3 standard consents
        for (int i = 1; i <= 3; i++) {
            final int consentNum = i;
            
            // Find consent in database, or use defaults
            Consent dbConsent = databaseConsents.stream()
                    .filter(c -> c.getConsentNumber().equals(consentNum))
                    .findFirst()
                    .orElse(null);
            
            ConsentDetailsDTO dto = ConsentDetailsDTO.builder()
                    .consentNumber(consentNum)
                    .consentText(dbConsent != null ? dbConsent.getConsentText() : consentTexts[i - 1])
                    .signed(dbConsent != null && dbConsent.getIsSigned() != null && dbConsent.getIsSigned())
                    .borrowerSignature(dbConsent != null ? dbConsent.getBorrowerSignature() : "")
                    .signedAt(dbConsent != null ? dbConsent.getSignedAt() : null)
                    .build();
            
            consents.add(dto);
        }
        
        return consents;
    }

    private OfferDTO buildOfferDTO(Offer offer) {
        OfferDTO offerDto = new OfferDTO();
        offerDto.id = offer.getId();
        offerDto.applicationId = offer.getApplicationId();
        offerDto.bankId = offer.getBankId();
        offerDto.apr = offer.getApr();
        offerDto.monthlyPayment = offer.getMonthlyPayment();
        offerDto.totalCost = offer.getTotalCost();
        offerDto.originationFee = offer.getOriginationFee();
        offerDto.processingTimeDays = offer.getProcessingTimeDays();
        offerDto.validityPeriodDays = offer.getValidityPeriodDays();
        offerDto.offerStatus = offer.getOfferStatus();
        offerDto.expiresAt = offer.getExpiresAt();
        offerDto.createdAt = offer.getCreatedAt();
        return offerDto;
    }

    /**
     * Mark application as VIEWED by bank with audit logging
     * Only logs once per application per bank
     */
    @CacheEvict(value = "applicationDetails", key = "#bankId + '-' + #application.id")
    private void markApplicationAsViewed(UUID bankId, Application application) {
        try {
            // Log audit event: APPLICATION_VIEWED
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("bankId", bankId);
            auditDetails.put("applicationId", application.getId());
            auditDetails.put("borrowerId", application.getBorrowerId());
            auditDetails.put("viewedAt", LocalDateTime.now());
            
            auditService.logAction(
                    "Application",
                    application.getId(),
                    AuditAction.APPLICATION_VIEWED,
                    bankId,
                    "BANK_ADMIN"
            );
            
            log.info("Application {} marked as VIEWED by bank {}", application.getId(), bankId);
        } catch (Exception e) {
            log.warn("Failed to log APPLICATION_VIEWED audit for app {}: {}", application.getId(), e.getMessage());
            // Non-blocking: don't fail the operation if audit logging fails
        }
    }

    /**
     * Save or update internal notes for an application at the bank level
     * Logs audit event: APPLICATION_NOTES_UPDATED
     *
     * @param bankId bank's organization id
     * @param applicationId the application to update notes for
     * @param notes new notes content
     * @param userId user making the update
     */
    @CacheEvict(value = "applicationDetails", key = "#bankId + '-' + #applicationId")
    public void updateInternalNotes(UUID bankId, UUID applicationId, String notes, UUID userId) {
        try {
            ApplicationBankNotes applicationNotes = notesRepository
                    .findByApplicationIdAndBankId(applicationId, bankId)
                    .orElse(ApplicationBankNotes.builder()
                            .applicationId(applicationId)
                            .bankId(bankId)
                            .createdBy(userId)
                            .build());

            String oldNotes = applicationNotes.getNotes();
            applicationNotes.setNotes(notes);
            applicationNotes.setUpdatedBy(userId);
            notesRepository.save(applicationNotes);

            // Log audit event
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("notes", oldNotes);

            Map<String, Object> newValues = new HashMap<>();
            newValues.put("notes", notes);

            auditService.logActionWithValues(
                    "Application",
                    applicationId,
                    AuditAction.APPLICATION_UPDATED,
                    oldValues,
                    newValues
            );

            log.info("Internal notes updated for application {} by bank {}", applicationId, bankId);
        } catch (Exception e) {
            log.error("Failed to update internal notes for application {}: {}", applicationId, e.getMessage());
            throw new RuntimeException("Failed to update internal notes", e);
        }
    }

    /**
     * Mark a specific consent as signed/accepted
     * Creates or updates the consent in the database
     *
     * @param applicationId the application
     * @param borrowerId the borrower
     * @param consentNumber which consent (1-3)
     * @param signature digital signature or acceptance indicator
     * @param ipAddress request origin IP
     * @param userAgent request user agent
     */
    @CacheEvict(value = "applicationDetails", allEntries = true)
    public void signConsent(UUID applicationId, UUID borrowerId, Integer consentNumber,
                           String signature, String ipAddress, String userAgent) {
        try {
            // Find existing consent or create new
            Consent consent = consentRepository
                    .findByApplicationIdAndConsentNumber(applicationId, consentNumber)
                    .orElse(Consent.builder()
                            .applicationId(applicationId)
                            .borrowerId(borrowerId)
                            .consentNumber(consentNumber)
                            .consentText(getConsentText(consentNumber))
                            .build());

            // Mark as signed
            consent.setIsSigned(true);
            consent.setBorrowerSignature(signature);
            consent.setIpAddress(ipAddress);
            consent.setUserAgent(userAgent);

            consentRepository.save(consent);

            log.info("Consent {} marked as signed for application {}", consentNumber, applicationId);
        } catch (Exception e) {
            log.error("Failed to sign consent {} for app {}: {}", consentNumber, applicationId, e.getMessage());
            throw new RuntimeException("Failed to sign consent", e);
        }
    }

    /**
     * Get standard consent text by number
     */
    private String getConsentText(Integer consentNumber) {
        return switch (consentNumber) {
            case 1 -> "I authorize the bank to check my credit report and history";
            case 2 -> "I authorize the bank to share my information with third-party service providers";
            case 3 -> "I agree to the loan service terms and conditions";
            default -> "Unknown consent";
        };
    }
}

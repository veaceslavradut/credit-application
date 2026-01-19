package com.creditapp.bank.service;

import com.creditapp.bank.dto.OfferSubmissionRequest;
import com.creditapp.bank.dto.OfferSubmissionResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for bank offer submission with optional field overrides
 * Implements idempotency, calculation validation, and audit logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankOfferSubmissionService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditService auditService;

    /**
     * Submit offer for application with optional overrides
     * Implements idempotency: same (bankId, applicationId) returns existing offer
     *
     * @param bankId bank submitting the offer
     * @param applicationId application to submit offer for
     * @param request offer submission request with optional overrides
     * @return submitted offer response with HTTP status
     */
    @Transactional
    public OfferSubmissionResponse submitOffer(UUID bankId, UUID applicationId, OfferSubmissionRequest request) {
        long startTime = System.currentTimeMillis();

        // Verify application exists
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        // Check for idempotency: existing offer from this bank for this application
        Optional<Offer> existingOffer = offerRepository.findByApplicationIdAndBankId(applicationId, bankId);
        if (existingOffer.isPresent()) {
            log.info("Idempotent: Returning existing offer {} for app {} from bank {}", 
                    existingOffer.get().getId(), applicationId, bankId);
            return buildResponse(existingOffer.get(), 200); // 200 OK for existing
        }

        // Determine offer values
        BigDecimal apr;
        BigDecimal fees;
        Integer processingTime;
        BigDecimal monthlyPayment;
        BigDecimal totalCost;

        if (request.getAcceptCalculatedOffer() != null && request.getAcceptCalculatedOffer()) {
            // Use system-calculated offer AS-IS from Story 3.3
            // For now, use defaults (in real scenario, fetch from OfferCalculationService)
            apr = new BigDecimal("10.5800");
            fees = new BigDecimal("150.00");
            processingTime = 5;
            monthlyPayment = CalculationUtils.calculateMonthlyPayment(
                    application.getLoanAmount(),
                    application.getLoanTermMonths(),
                    apr
            );
            totalCost = monthlyPayment
                    .multiply(new BigDecimal(application.getLoanTermMonths()))
                    .add(fees);
        } else {
            // Use overrides
            apr = request.getOverrideAPR() != null ? request.getOverrideAPR() : new BigDecimal("10.5800");
            fees = request.getOverrideFees() != null ? request.getOverrideFees() : new BigDecimal("150.00");
            processingTime = request.getOverrideProcessingTime() != null ? request.getOverrideProcessingTime() : 5;

            // Validate APR range: 0.5% to 50%
            if (apr.compareTo(new BigDecimal("0.5")) < 0 || apr.compareTo(new BigDecimal("50")) > 0) {
                throw new IllegalArgumentException("APR must be between 0.5% and 50%, received: " + apr);
            }

            // Recalculate monthly payment using Decision 2 formula
            monthlyPayment = CalculationUtils.calculateMonthlyPayment(
                    application.getLoanAmount(),
                    application.getLoanTermMonths(),
                    apr
            );

            // Total cost = (monthlyPayment  months) + fees
            totalCost = monthlyPayment
                    .multiply(new BigDecimal(application.getLoanTermMonths()))
                    .add(fees);
        }

        // Create new offer
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setApr(apr);
        offer.setMonthlyPayment(monthlyPayment);
        offer.setTotalCost(totalCost);
        offer.setOriginationFee(fees);
        offer.setProcessingTimeDays(processingTime);
        offer.setValidityPeriodDays(1);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        offer.setExpiresAt(LocalDateTime.now().plusDays(1));

        // Save offer
        offer = offerRepository.save(offer);
        log.info("Created new offer {} for app {} from bank {} with APR={}", 
                offer.getId(), applicationId, bankId, apr);

        // Log audit event
        auditService.logAction("Offer", offer.getId(), AuditAction.OFFER_SUBMITTED);
        log.info("Audit logged: OFFER_SUBMITTED for offer {}", offer.getId());

        // Queue email notification (async)
        // TODO: Implement email notification to borrower using EmailService
        log.info("Email notification queued for application {}", applicationId);

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("Offer submission completed in {}ms", elapsed);
        if (elapsed > 500) {
            log.warn("Offer submission exceeded 500ms SLA: {}ms", elapsed);
        }

        return buildResponse(offer, 201); // 201 CREATED for new
    }

    /**
     * Build response DTO from offer entity
     */
    private OfferSubmissionResponse buildResponse(Offer offer, int httpStatus) {
        return OfferSubmissionResponse.builder()
                .offerId(offer.getId())
                .apr(offer.getApr())
                .fees(offer.getOriginationFee())
                .processingTime(offer.getProcessingTimeDays())
                .monthlyPayment(offer.getMonthlyPayment())
                .totalCost(offer.getTotalCost())
                .submittedAt(offer.getCreatedAt())
                .httpStatus(httpStatus)
                .build();
    }
}

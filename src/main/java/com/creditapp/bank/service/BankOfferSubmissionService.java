package com.creditapp.bank.service;

import com.creditapp.bank.dto.BankOfferSubmissionRequest;
import com.creditapp.bank.dto.BankOfferSubmissionResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.BankOfferSubmissionEmailService;
import com.creditapp.shared.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankOfferSubmissionService {

    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;
    private final AuditService auditService;
    private final BankOfferSubmissionEmailService emailService;

    @Transactional
    public BankOfferSubmissionResponse submitOffer(
            UUID bankId,
            UUID officerId,
            BankOfferSubmissionRequest request) {
        
        long start = System.currentTimeMillis();
        log.debug("[OFFER_SUBMIT] Bank officer {} submitting offer for application {}",
                officerId, request.getApplicationId());

        // Fetch and validate application
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new NotFoundException("Application not found: " + request.getApplicationId()));

        // Validate loan amount matches
        if (application.getLoanAmount() == null) {
            throw new IllegalArgumentException("Application loan amount is not set");
        }

        // Get term months (from request or application)
        Integer termMonths = request.getTermMonths() != null 
                ? request.getTermMonths() 
                : application.getLoanTermMonths();

        if (termMonths == null) {
            throw new IllegalArgumentException("Term months must be provided");
        }

        // Calculate monthly payment if not provided
        BigDecimal monthlyPayment = request.getMonthlyPayment();
        if (monthlyPayment == null) {
            monthlyPayment = CalculationUtils.calculateMonthlyPayment(
                    application.getLoanAmount(),
                    termMonths,
                    request.getApr()
            );
            log.debug("[OFFER_SUBMIT] Calculated monthly payment: {}", monthlyPayment);
        }

        // Calculate total cost if not provided
        BigDecimal totalCost = request.getTotalCost();
        if (totalCost == null) {
            totalCost = monthlyPayment.multiply(BigDecimal.valueOf(termMonths));
            log.debug("[OFFER_SUBMIT] Calculated total cost: {}", totalCost);
        }

        // Calculate origination fee if not provided (default 1.5%)
        BigDecimal originationFee = request.getOriginationFee();
        if (originationFee == null) {
            originationFee = application.getLoanAmount()
                    .multiply(BigDecimal.valueOf(0.015));
            log.debug("[OFFER_SUBMIT] Calculated origination fee: {}", originationFee);
        }

        // Calculate insurance cost if not provided (default 0.5%)
        BigDecimal insuranceCost = request.getInsuranceCost();
        if (insuranceCost == null) {
            insuranceCost = application.getLoanAmount()
                    .multiply(BigDecimal.valueOf(0.005));
            log.debug("[OFFER_SUBMIT] Calculated insurance cost: {}", insuranceCost);
        }

        // Create new offer
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(request.getApplicationId());
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        offer.setApr(request.getApr());
        offer.setMonthlyPayment(monthlyPayment);
        offer.setTotalCost(totalCost);
        offer.setOriginationFee(originationFee);
        offer.setInsuranceCost(insuranceCost);
        offer.setProcessingTimeDays(request.getProcessingTimeDays() != null 
                ? request.getProcessingTimeDays() : 5);
        offer.setValidityPeriodDays(1); // 24 hours
        
        // Set required documents
        String requiredDocs = request.getRequiredDocuments() != null 
                ? String.join(",", request.getRequiredDocuments())
                : "paystubs,tax_returns,bank_statements,government_id";
        offer.setRequiredDocuments(requiredDocs);
        
        // Set expiration (24 hours from now)
        offer.setExpiresAt(LocalDateTime.now().plusDays(1));
        offer.setOfferSubmittedAt(LocalDateTime.now());
        
        // Set officer tracking fields
        offer.setSubmittedByOfficerId(officerId);
        offer.setSubmissionNotes(request.getNotes());

        // Save offer
        offer = offerRepository.save(offer);

        // Send email notification to borrower
        try {
            emailService.sendOfferSubmittedByBankNotification(
                    offer,
                    application,
                    officerId,
                    "Bank Name", // TODO: Get from bank repository
                    "Officer Name" // TODO: Get from user repository
            );
        } catch (Exception e) {
            log.error("[OFFER_SUBMIT] Failed to send email notification", e);
            // Continue - email failure shouldn't fail offer submission
        }

        // Log audit event
        try {
            Map<String, Object> auditData = Map.of(
                    "bankId", bankId.toString(),
                    "officerId", officerId.toString(),
                    "applicationId", request.getApplicationId().toString(),
                    "apr", request.getApr().toString(),
                    "monthlyPayment", monthlyPayment.toString(),
                    "totalCost", totalCost.toString()
            );
            auditService.logActionWithValues(
                    "Offer",
                    offer.getId(),
                    AuditAction.OFFER_SUBMITTED,
                    Map.of(),
                    auditData
            );
        } catch (Exception e) {
            log.error("[OFFER_SUBMIT] Failed to log audit event", e);
        }

        long took = System.currentTimeMillis() - start;
        log.info("[OFFER_SUBMIT] Bank officer {} submitted offer {} for application {} in {}ms",
                officerId, offer.getId(), request.getApplicationId(), took);

        // Build response
        return BankOfferSubmissionResponse.builder()
                .offerId(offer.getId())
                .applicationId(offer.getApplicationId())
                .apr(offer.getApr())
                .monthlyPayment(offer.getMonthlyPayment())
                .totalCost(offer.getTotalCost())
                .status(offer.getOfferStatus().name())
                .submittedAt(offer.getOfferSubmittedAt())
                .expiresAt(offer.getExpiresAt())
                .borrowerNotificationStatus("pending")
                .build();
    }
}
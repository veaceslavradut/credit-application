package com.creditapp.bank.service;

import com.creditapp.bank.dto.OfferDTO;
import com.creditapp.bank.exception.OfferCalculationException;
import com.creditapp.bank.exception.RateCardMissingException;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferCalculationLog;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.bank.repository.OfferCalculationLogRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.model.CalculationType;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for calculating loan offers from bank rate cards.
 * This is a mock/simulated calculation - no external bank API calls.
 */
@Service
@Slf4j
@Transactional
public class OfferCalculationService {

    private final OfferRepository offerRepository;
    private final BankRateCardRepository bankRateCardRepository;
    private final OrganizationRepository organizationRepository;
    private final ApplicationRepository applicationRepository;
    private final OfferCalculationLogRepository calculationLogRepository;
    private final AuditService auditService;

    @Value(${app.offer.validity.period.hours:24})
    private int validityPeriodHours;

    public OfferCalculationService(OfferRepository offerRepository,
                                   BankRateCardRepository bankRateCardRepository,
                                   OrganizationRepository organizationRepository,
                                   ApplicationRepository applicationRepository,
                                   OfferCalculationLogRepository calculationLogRepository,
                                   AuditService auditService) {
        this.offerRepository = offerRepository;
        this.bankRateCardRepository = bankRateCardRepository;
        this.organizationRepository = organizationRepository;
        this.applicationRepository = applicationRepository;
        this.calculationLogRepository = calculationLogRepository;
        this.auditService = auditService;
    }

    /**
     * Main entry point: Calculate offers for an application asynchronously.
     * For each active bank with a matching rate card, calculate and store an offer.
     * 
     * @param applicationId the application ID
     * @return list of created offer IDs
     */
    @Async
    public List<UUID> calculateOffers(UUID applicationId) {
        log.info(""Starting offer calculation for application: {}"", applicationId);
        
        List<UUID> offerIds = new ArrayList<>();
        
        try {
            // Fetch application
            Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException(""Application not found: "" + applicationId));
            
            // Get all active banks
            List<Organization> activeBanks = organizationRepository.findAll().stream()
                .filter(org -> org.getStatus() == BankStatus.ACTIVE)
                .toList();
            
            log.info(""Found {} active banks for offer calculation"", activeBanks.size());
            
            // Calculate offer for each bank
            for (Organization bank : activeBanks) {
                try {
                    Offer offer = calculateOfferForBank(application, bank);
                    if (offer != null) {
                        offerIds.add(offer.getId());
                        log.info(""Created offer {} for bank {} on application {}"", 
                            offer.getId(), bank.getName(), applicationId);
                    }
                } catch (Exception e) {
                    // Log error but continue with other banks
                    log.error(""Failed to calculate offer for bank {}: {}"", bank.getName(), e.getMessage(), e);
                }
            }
            
            log.info(""Completed offer calculation for application: {}. Created {} offers"", 
                applicationId, offerIds.size());
            
        } catch (Exception e) {
            log.error(""Error during offer calculation for application: {}"", applicationId, e);
            throw new OfferCalculationException(""Offer calculation failed"", e);
        }
        
        return offerIds;
    }

    /**
     * Calculate an offer for a specific bank and application.
     * 
     * @param application the application
     * @param bank the bank organization
     * @return created Offer or null if no rate card available
     */
    private Offer calculateOfferForBank(Application application, Organization bank) {
        UUID bankId = bank.getId();
        UUID applicationId = application.getId();
        
        // Convert String loan type and currency to enums
        LoanType loanType = LoanType.valueOf(application.getLoanType());
        Currency currency = Currency.valueOf(application.getCurrency());
        
        // Get rate card for bank/loanType/currency
        BankRateCard rateCard = bankRateCardRepository
            .findByBankIdAndLoanTypeAndCurrencyAndValidToIsNull(bankId, loanType, currency)
            .orElse(null);
        
        if (rateCard == null) {
            log.warn(""No active rate card found for bank {} (loan type: {}, currency: {}). Skipping."", 
                bank.getName(), loanType, currency);
            return null;
        }
        
        // Perform calculations
        OfferCalculationResult result = calculateAmortization(
            application.getLoanAmount(),
            application.getLoanTermMonths(),
            rateCard.getBaseApr(),
            rateCard.getAprAdjustmentRange(),
            rateCard.getOriginationFeePercent(),
            rateCard.getInsurancePercent()
        );
        
        // Create offer
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setApr(result.finalApr);
        offer.setMonthlyPayment(result.monthlyPayment);
        offer.setTotalCost(result.totalCost);
        offer.setOriginationFee(result.originationFee);
        offer.setInsuranceCost(result.insuranceCost);
        offer.setProcessingTimeDays(rateCard.getProcessingTimeDays());
        offer.setValidityPeriodDays(validityPeriodHours / 24); // Convert hours to days
        offer.setRequiredDocuments(""Standard documents required""); // TODO: from rate card template
        offer.setExpiresAt(LocalDateTime.now().plusHours(validityPeriodHours));
        
        // Save offer
        Offer savedOffer = offerRepository.save(offer);
        
        // Log calculation
        logCalculation(applicationId, bankId, application, rateCard, result);
        
        // Audit
        try {
            auditService.logAction(""Offer"", savedOffer.getId(), AuditAction.OFFER_CALCULATED,
                Map.of(""applicationId"", applicationId.toString(), 
                       ""bankId"", bankId.toString(),
                       ""calculationMethod"", ""MOCK_CALCULATION"",
                       ""apr"", result.finalApr.toString(),
                       ""monthlyPayment"", result.monthlyPayment.toString()),
                ""SYSTEM"");
        } catch (Exception e) {
            log.error(""Failed to audit offer calculation"", e);
        }
        
        return savedOffer;
    }

    /**
     * Calculate amortization schedule and costs.
     * 
     * @param principal loan amount
     * @param months loan term in months
     * @param baseApr base APR from rate card
     * @param aprAdjustmentRange adjustment range
     * @param originationFeePercent origination fee percentage
     * @param insurancePercent insurance percentage (can be null)
     * @return calculation result
     */
    private OfferCalculationResult calculateAmortization(
            BigDecimal principal,
            Integer months,
            BigDecimal baseApr,
            BigDecimal aprAdjustmentRange,
            BigDecimal originationFeePercent,
            BigDecimal insurancePercent) {
        
        // Apply APR adjustment (simple logic for MVP: adjust for longer terms)
        BigDecimal adjustment = months > 120 ? aprAdjustmentRange.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP) 
                                             : BigDecimal.ZERO;
        BigDecimal finalApr = baseApr.add(adjustment);
        
        // Calculate monthly rate
        BigDecimal monthlyRate = finalApr
            .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        
        // Calculate monthly payment using amortization formula
        // M = P * [r(1 + r)^n] / [(1 + r)^n - 1]
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRatePowN = onePlusRate.pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowN);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);
        BigDecimal monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        
        // Calculate total cost
        BigDecimal totalPayments = monthlyPayment.multiply(BigDecimal.valueOf(months));
        BigDecimal totalCost = totalPayments.subtract(principal);
        
        // Calculate fees
        BigDecimal originationFee = principal
            .multiply(originationFeePercent)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal insuranceCost = (insurancePercent != null && insurancePercent.compareTo(BigDecimal.ZERO) > 0)
            ? principal.multiply(insurancePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        OfferCalculationResult result = new OfferCalculationResult();
        result.finalApr = finalApr;
        result.monthlyPayment = monthlyPayment;
        result.totalCost = totalCost;
        result.originationFee = originationFee;
        result.insuranceCost = insuranceCost;
        
        return result;
    }

    /**
     * Log calculation to offer_calculation_log table.
     */
    private void logCalculation(UUID applicationId, UUID bankId, Application application,
                               BankRateCard rateCard, OfferCalculationResult result) {
        try {
            OfferCalculationLog log = new OfferCalculationLog();
            log.setApplicationId(applicationId);
            log.setBankId(bankId);
            log.setCalculationMethod(""MOCK_CALCULATION"");
            log.setCalculationType(CalculationType.MOCK_CALCULATION);
            log.setTimestamp(LocalDateTime.now());
            
            // Input parameters as JSON string
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put(""loanAmount"", application.getLoanAmount());
            inputParams.put(""loanTermMonths"", application.getLoanTermMonths());
            inputParams.put(""loanType"", application.getLoanType());
            inputParams.put(""currency"", application.getCurrency());
            inputParams.put(""rateCardId"", rateCard.getId());
            inputParams.put(""baseApr"", rateCard.getBaseApr());
            inputParams.put(""aprAdjustmentRange"", rateCard.getAprAdjustmentRange());
            log.setInputParameters(convertMapToJson(inputParams));
            
            // Calculated values as JSON string
            Map<String, Object> calculatedValues = new HashMap<>();
            calculatedValues.put(""finalApr"", result.finalApr);
            calculatedValues.put(""monthlyPayment"", result.monthlyPayment);
            calculatedValues.put(""totalCost"", result.totalCost);
            calculatedValues.put(""originationFee"", result.originationFee);
            calculatedValues.put(""insuranceCost"", result.insuranceCost);
            log.setCalculatedValues(convertMapToJson(calculatedValues));
            
            calculationLogRepository.save(log);
        } catch (Exception e) {
            log.error(""Failed to log calculation"", e);
        }
    }

    /**
     * Simple JSON converter for map.
     */
    private String convertMapToJson(Map<String, Object> map) {
        // Simple JSON representation
        StringBuilder json = new StringBuilder(""{"");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (i++ > 0) json.append("","");
            json.append(String.format(""\""""%s\"""": \""""%s\"""""", entry.getKey(), entry.getValue()));
        }
        json.append(""}"");
        return json.toString();
    }

    /**
     * Inner class to hold calculation results.
     */
    private static class OfferCalculationResult {
        BigDecimal finalApr;
        BigDecimal monthlyPayment;
        BigDecimal totalCost;
        BigDecimal originationFee;
        BigDecimal insuranceCost;
    }
}
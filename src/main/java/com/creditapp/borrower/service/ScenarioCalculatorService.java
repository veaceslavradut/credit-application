package com.creditapp.borrower.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.borrower.dto.CalculateScenarioRequest;
import com.creditapp.borrower.dto.CalculateScenarioResponse;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.service.RateCardLookupService;
import com.creditapp.shared.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioCalculatorService {
    
    private final RateCardLookupService rateCardLookupService;
    private final BankRateCardRepository bankRateCardRepository;
    
    @Cacheable(
        value = "scenarioCalculations",
        key = "#request.loanAmount.toPlainString() + '-' + #request.termMonths + '-' + (#request.bankId != null ? #request.bankId.toString() : 'default')",
        unless = "#result == null"
    )
    public CalculateScenarioResponse calculateScenario(CalculateScenarioRequest request) {
        log.debug("Calculating scenario for loan amount: {}, term: {} months, bank: {}", 
            request.getLoanAmount(), request.getTermMonths(), request.getBankId());
        
        BankRateCard rateCard;
        if (request.getBankId() != null) {
            List<BankRateCard> rateCards = bankRateCardRepository.findByBankIdAndValidToIsNull(request.getBankId());
            if (rateCards.isEmpty()) {
                throw new NotFoundException("Rate card not found for bank: " + request.getBankId());
            }
            rateCard = rateCards.get(0); // Get first active rate card for bank
        } else {
            rateCard = rateCardLookupService.getDefaultRateCard();
        }
        
        return calculateScenarioWithRateCard(request.getLoanAmount(), request.getTermMonths(), rateCard, request.getBankId());
    }
    
    private CalculateScenarioResponse calculateScenarioWithRateCard(
            BigDecimal loanAmount, int termMonths, BankRateCard rateCard, UUID bankId) {
        
        BigDecimal monthlyPayment = CalculationUtils.calculateMonthlyPayment(
            loanAmount, termMonths, rateCard.getBaseApr()
        );
        
        BigDecimal originationFee = CalculationUtils.calculateOriginationFee(
            loanAmount, rateCard.getOriginationFeePercent()
        );
        
        BigDecimal insuranceCost = CalculationUtils.calculateInsuranceCost(
            loanAmount, termMonths, rateCard.getInsurancePercent()
        );
        
        BigDecimal totalCost = CalculationUtils.calculateTotalCost(
            monthlyPayment, termMonths, loanAmount
        );
        
        CalculateScenarioResponse response = CalculateScenarioResponse.builder()
            .loanAmount(loanAmount)
            .termMonths(termMonths)
            .apr(rateCard.getBaseApr())
            .monthlyPayment(monthlyPayment)
            .totalCost(totalCost)
            .originationFee(originationFee)
            .insuranceCost(insuranceCost)
            .bankId(bankId)
            .bankName(bankId != null ? "Bank: " + bankId.toString().substring(0, 8) : null)
            .calculatedAt(LocalDateTime.now(ZoneOffset.UTC))
            .build();
        
        log.info("Scenario calculated: {} @ {}/month with {}", 
            loanAmount, monthlyPayment, rateCard.getBaseApr());
        
        return response;
    }
}
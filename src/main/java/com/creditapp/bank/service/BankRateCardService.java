package com.creditapp.bank.service;

import com.creditapp.bank.dto.BankRateCardRequest;
import com.creditapp.bank.dto.BankRateCardResponse;
import com.creditapp.bank.exception.DuplicateRateCardException;
import com.creditapp.bank.exception.InvalidRateCardException;
import com.creditapp.bank.exception.RateCardNotFoundException;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BankRateCardService {
    
    private final BankRateCardRepository bankRateCardRepository;
    private final AuditService auditService;
    
    public BankRateCardService(BankRateCardRepository bankRateCardRepository, AuditService auditService) {
        this.bankRateCardRepository = bankRateCardRepository;
        this.auditService = auditService;
    }
    
    /**
     * Creates a new rate card. If an active card exists for the same loan type and currency,
     * marks it as inactive (versioning).
     */
    public BankRateCardResponse createRateCard(UUID bankId, BankRateCardRequest request) {
        validateRateCardRequest(request);
        
        // Check for duplicate: existing active rate card for same loan type and currency
        Optional<BankRateCard> existingCard = bankRateCardRepository
            .findByBankIdAndLoanTypeAndCurrencyAndValidToIsNull(
                bankId, request.getLoanType(), request.getCurrency());
        
        if (existingCard.isPresent()) {
            // Mark existing active card as inactive
            BankRateCard oldCard = existingCard.get();
            oldCard.setValidTo(LocalDateTime.now());
            bankRateCardRepository.save(oldCard);
        }
        
        // Create new rate card
        BankRateCard newCard = new BankRateCard();
        newCard.setId(UUID.randomUUID());
        newCard.setBankId(bankId);
        newCard.setLoanType(request.getLoanType());
        newCard.setCurrency(request.getCurrency());
        newCard.setMinLoanAmount(request.getMinLoanAmount());
        newCard.setMaxLoanAmount(request.getMaxLoanAmount());
        newCard.setBaseApr(request.getBaseApr());
        newCard.setAprAdjustmentRange(request.getAprAdjustmentRange());
        newCard.setOriginationFeePercent(request.getOriginationFeePercent());
        newCard.setInsurancePercent(request.getInsurancePercent());
        newCard.setValidFrom(LocalDateTime.now());
        newCard.setValidTo(null); // Active
        
        BankRateCard savedCard = bankRateCardRepository.save(newCard);
        auditService.logAction("RateCard", savedCard.getId(), AuditAction.RATE_CARD_CREATED);
        
        return mapToResponse(savedCard);
    }
    
    /**
     * Updates a rate card by creating a new version. Marks the old card as inactive.
     * This implements an immutable history pattern for audit trail.
     */
    public BankRateCardResponse updateRateCard(UUID bankId, UUID rateCardId, BankRateCardRequest request) {
        validateRateCardRequest(request);
        
        // Find the existing card
        BankRateCard existingCard = bankRateCardRepository.findById(rateCardId)
            .orElseThrow(() -> new RateCardNotFoundException(
                String.format("Rate card with ID %s not found", rateCardId)));
        
        // Verify bank ownership
        if (!existingCard.getBankId().equals(bankId)) {
            throw new InvalidRateCardException(
                "Rate card does not belong to the requesting bank");
        }
        
        // Mark old card as inactive
        existingCard.setValidTo(LocalDateTime.now());
        bankRateCardRepository.save(existingCard);
        
        // Create new version
        BankRateCard newCard = new BankRateCard();
        newCard.setId(UUID.randomUUID());
        newCard.setBankId(bankId);
        newCard.setLoanType(request.getLoanType());
        newCard.setCurrency(request.getCurrency());
        newCard.setMinLoanAmount(request.getMinLoanAmount());
        newCard.setMaxLoanAmount(request.getMaxLoanAmount());
        newCard.setBaseApr(request.getBaseApr());
        newCard.setAprAdjustmentRange(request.getAprAdjustmentRange());
        newCard.setOriginationFeePercent(request.getOriginationFeePercent());
        newCard.setInsurancePercent(request.getInsurancePercent());
        newCard.setValidFrom(LocalDateTime.now());
        newCard.setValidTo(null); // Active
        
        BankRateCard savedCard = bankRateCardRepository.save(newCard);
        auditService.logAction("RateCard", savedCard.getId(), AuditAction.RATE_CARD_UPDATED);
        
        return mapToResponse(savedCard);
    }
    
    /**
     * Retrieves all active rate cards for a given bank.
     */
    public List<BankRateCardResponse> getActiveRateCards(UUID bankId) {
        List<BankRateCard> activeCards = bankRateCardRepository.findByBankIdAndValidToIsNull(bankId);
        return activeCards.stream()
            .map(this::mapToResponse)
            .toList();
    }
    
    /**
     * Retrieves a specific active rate card by loan type and currency.
     */
    public BankRateCardResponse getRateCardByLoanTypeAndCurrency(
            UUID bankId, LoanType loanType, Currency currency) {
        BankRateCard card = bankRateCardRepository
            .findByBankIdAndLoanTypeAndCurrencyAndValidToIsNull(bankId, loanType, currency)
            .orElseThrow(() -> new RateCardNotFoundException(
                String.format("No active rate card found for loan type %s and currency %s", 
                    loanType, currency)));
        
        return mapToResponse(card);
    }
    
    /**
     * Validates all rate card request fields.
     */
    private void validateRateCardRequest(BankRateCardRequest request) {
        if (request == null) {
            throw new InvalidRateCardException("Rate card request cannot be null");
        }
        
        if (request.getLoanType() == null) {
            throw new InvalidRateCardException("Loan type is required");
        }
        
        if (request.getCurrency() == null) {
            throw new InvalidRateCardException("Currency is required");
        }
        
        if (request.getMinLoanAmount() == null || request.getMinLoanAmount().signum() <= 0) {
            throw new InvalidRateCardException("Minimum loan amount must be greater than zero");
        }
        
        if (request.getMaxLoanAmount() == null || request.getMaxLoanAmount().signum() <= 0) {
            throw new InvalidRateCardException("Maximum loan amount must be greater than zero");
        }
        
        if (request.getMinLoanAmount().compareTo(request.getMaxLoanAmount()) > 0) {
            throw new InvalidRateCardException(
                "Minimum loan amount cannot exceed maximum loan amount");
        }
        
        if (request.getBaseApr() == null || request.getBaseApr().signum() < 0) {
            throw new InvalidRateCardException("Base APR cannot be negative");
        }
        
        if (request.getAprAdjustmentRange() == null || request.getAprAdjustmentRange().signum() < 0) {
            throw new InvalidRateCardException("APR adjustment range cannot be negative");
        }
        
        if (request.getOriginationFeePercent() == null || 
            request.getOriginationFeePercent().signum() < 0) {
            throw new InvalidRateCardException("Origination fee percentage cannot be negative");
        }
        
        if (request.getInsurancePercent() == null || request.getInsurancePercent().signum() < 0) {
            throw new InvalidRateCardException("Insurance percentage cannot be negative");
        }
    }
    
    /**
     * Converts BankRateCard entity to BankRateCardResponse DTO.
     */
    private BankRateCardResponse mapToResponse(BankRateCard card) {
        return new BankRateCardResponse(
            card.getId(),
            card.getLoanType(),
            card.getCurrency(),
            card.getMinLoanAmount(),
            card.getMaxLoanAmount(),
            card.getBaseApr(),
            card.getAprAdjustmentRange(),
            card.getOriginationFeePercent(),
            card.getInsurancePercent(),
            card.getProcessingTimeDays(),
            card.getValidFrom(),
            card.getValidTo()
        );
    }
}
package com.creditapp.bank.service;

import com.creditapp.bank.dto.OfferDTO;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    /**
     * Get all offers for a given application, sorted by APR ascending.
     */
    public List<OfferDTO> getOffersByApplication(UUID applicationId) {
        return offerRepository.findByApplicationId(applicationId)
                .stream()
                .sorted(Comparator.comparing(Offer::getApr))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific offer by ID.
     */
    public OfferDTO getOfferById(UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        return toDTO(offer);
    }

    /**
     * Check if the specified offer is expired.
     */
    public boolean isOfferExpired(UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        return offer.isExpired();
    }

    private OfferDTO toDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.id = offer.getId();
        dto.applicationId = offer.getApplicationId();
        dto.bankId = offer.getBankId();
        dto.offerStatus = offer.getOfferStatus();
        dto.apr = offer.getApr();
        dto.monthlyPayment = offer.getMonthlyPayment();
        dto.totalCost = offer.getTotalCost();
        dto.originationFee = offer.getOriginationFee();
        dto.insuranceCost = offer.getInsuranceCost();
        dto.processingTimeDays = offer.getProcessingTimeDays();
        dto.validityPeriodDays = offer.getValidityPeriodDays();
        dto.requiredDocuments = offer.getRequiredDocuments();
        dto.createdAt = offer.getCreatedAt();
        dto.expiresAt = offer.getExpiresAt();
        dto.offerSubmittedAt = offer.getOfferSubmittedAt();
        return dto;
    }
}
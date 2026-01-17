package com.creditapp.borrower.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.OfferComparisonDTO;
import com.creditapp.borrower.exception.ApplicationNotSubmittedException;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.exception.UnauthorizedException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.Organization;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;

@Service
public class OfferRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(OfferRetrievalService.class);

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;

    public OfferRetrievalService(OfferRepository offerRepository,
                                 ApplicationRepository applicationRepository,
                                 OrganizationRepository organizationRepository,
                                 AuditService auditService) {
        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OfferComparisonDTO> getOffersForApplication(UUID applicationId, UUID borrowerId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            log.warn("Unauthorized offer retrieval attempt: borrower {} tried to access application {} belonging to {}", 
                borrowerId, applicationId, application.getBorrowerId());
            throw new UnauthorizedException("Borrower does not own this application");
        }

        if (application.getStatus().compareTo(ApplicationStatus.SUBMITTED) < 0) {
            log.warn("Attempt to retrieve offers for non-submitted application {}: status {}", 
                applicationId, application.getStatus());
            throw new ApplicationNotSubmittedException("Application must be in SUBMITTED status or later to view offers");
        }

        List<Offer> offers = offerRepository.findActiveOffersByApplicationIdOrderByApr(applicationId, OfferStatus.EXPIRED);

        log.info("Retrieved {} offers for application {} (borrower {})", 
            offers.size(), applicationId, borrowerId);

        if (offers.isEmpty()) {
            auditService.logAction("Application", applicationId, AuditAction.APPLICATION_VIEWED);
            return new ArrayList<>();
        }

        List<UUID> bankIds = offers.stream()
            .map(Offer::getBankId)
            .distinct()
            .collect(Collectors.toList());

        List<Organization> banks = organizationRepository.findAllById(bankIds);
        Map<UUID, Organization> bankMap = new HashMap<>();
        for (Organization bank : banks) {
            bankMap.put(bank.getId(), bank);
        }

        List<OfferComparisonDTO> result = offers.stream()
            .map(offer -> convertToDTO(offer, bankMap.get(offer.getBankId())))
            .collect(Collectors.toList());

        log.debug("Converted {} offers to DTOs with bank information", result.size());
        auditService.logAction("Application", applicationId, AuditAction.APPLICATION_VIEWED);

        return result;
    }

    private OfferComparisonDTO convertToDTO(Offer offer, Organization bank) {
        List<String> documents = new ArrayList<>();
        if (offer.getRequiredDocuments() != null && !offer.getRequiredDocuments().isEmpty()) {
            documents = Arrays.stream(offer.getRequiredDocuments().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        }

        String logoUrl = (bank != null && bank.getLogoUrl() != null)
            ? bank.getLogoUrl()
            : "https://api.creditapp.com/images/default-bank-logo.png";

        return new OfferComparisonDTO(
            offer.getId(),
            offer.getBankId(),
            bank != null ? bank.getName() : "Unknown Bank",
            logoUrl,
            offer.getApr(),
            offer.getMonthlyPayment(),
            offer.getTotalCost(),
            offer.getOriginationFee(),
            offer.getInsuranceCost(),
            offer.getProcessingTimeDays(),
            offer.getValidityPeriodDays(),
            documents,
            offer.getExpiresAt(),
            offer.getOfferStatus().toString()
        );
    }
}
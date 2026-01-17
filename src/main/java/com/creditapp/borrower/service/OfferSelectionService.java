package com.creditapp.borrower.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.SelectOfferResponse;
import com.creditapp.borrower.exception.InvalidOfferException;
import com.creditapp.borrower.exception.OfferExpiredException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.OfferSelectionEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class OfferSelectionService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;
    private final OfferSelectionEmailService emailService;
    private final AuditService auditService;
    private final NextStepsService nextStepsService;

    public OfferSelectionService(OfferRepository offerRepository,
                                ApplicationRepository applicationRepository,
                                OrganizationRepository organizationRepository,
                                OfferSelectionEmailService emailService,
                                AuditService auditService,
                                NextStepsService nextStepsService) {
        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
        this.organizationRepository = organizationRepository;
        this.emailService = emailService;
        this.auditService = auditService;
        this.nextStepsService = nextStepsService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SelectOfferResponse selectOffer(UUID applicationId, UUID borrowerId, UUID offerId) {
        log.debug("Offer selection started. ApplicationId: {}, BorrowerId: {}, OfferId: {}", 
                applicationId, borrowerId, offerId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new InvalidOfferException("Application not found: " + applicationId));

        if (!application.getBorrowerId().equals(borrowerId)) {
            log.warn("Unauthorized offer selection attempt. ApplicationId: {}, BorrowerId: {}", 
                    applicationId, borrowerId);
            throw new InvalidOfferException("Unauthorized access to application");
        }

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new InvalidOfferException("Offer not found: " + offerId));

        if (!offer.getApplicationId().equals(applicationId)) {
            log.warn("Offer does not belong to application. ApplicationId: {}, OfferId: {}", 
                    applicationId, offerId);
            throw new InvalidOfferException("Offer does not belong to this application");
        }

        if (offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Offer expired. OfferId: {}, ExpiresAt: {}", offerId, offer.getExpiresAt());
            // Log audit event for failed selection attempt
            auditService.logAction("Offer", offerId, AuditAction.OFFER_SELECTION_FAILED,
                    borrowerId, "BORROWER");
            throw new OfferExpiredException("Offer has expired", offer.getExpiresAt());
        }

        Optional<Offer> previousOfferOpt = offerRepository.findByApplicationIdAndOfferStatus(applicationId, OfferStatus.ACCEPTED);
        if (previousOfferOpt.isPresent()) {
            Offer prevOffer = previousOfferOpt.get();
            prevOffer.setOfferStatus(OfferStatus.CALCULATED);
            prevOffer.setBorrowerSelectedAt(null);
            offerRepository.save(prevOffer);
            log.info("Previous offer deselected. OfferId: {}, Status: CALCULATED", prevOffer.getId());
            
            // Log audit event for deselection
            Map<String, Object> deselectionDetails = new HashMap<>();
            deselectionDetails.put("applicationId", applicationId);
            deselectionDetails.put("deselectedOfferId", prevOffer.getId());
            deselectionDetails.put("bankId", prevOffer.getBankId());
            deselectionDetails.put("previousStatus", OfferStatus.ACCEPTED.toString());
            deselectionDetails.put("newStatus", OfferStatus.CALCULATED.toString());
            auditService.logActionWithValues("Offer", prevOffer.getId(), AuditAction.OFFER_DESELECTED,
                    new HashMap<>(), deselectionDetails);
        }

        offer.setOfferStatus(OfferStatus.ACCEPTED);
        offer.setBorrowerSelectedAt(LocalDateTime.now());
        Offer savedOffer = offerRepository.save(offer);
        log.info("Offer selected by borrower. OfferId: {}, Status: ACCEPTED, SelectedAt: {}", 
                offerId, savedOffer.getBorrowerSelectedAt());

        application.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);
        log.info("Application status updated to ACCEPTED. ApplicationId: {}", applicationId);

        // Log comprehensive audit event for offer selection
        Map<String, Object> selectionDetails = new HashMap<>();
        selectionDetails.put("borrowerId", borrowerId);
        selectionDetails.put("applicationId", applicationId);
        selectionDetails.put("offerId", offerId);
        selectionDetails.put("bankId", offer.getBankId());
        selectionDetails.put("apr", savedOffer.getApr());
        selectionDetails.put("selectedAt", savedOffer.getBorrowerSelectedAt());
        selectionDetails.put("applicationStatus", ApplicationStatus.ACCEPTED.toString());
        auditService.logActionWithValues("Offer", offerId, AuditAction.OFFER_SELECTED,
                new HashMap<>(), selectionDetails);

        emailService.sendOfferSelectedToBorrower(borrowerId, savedOffer, application);
        emailService.sendOfferSelectedToBank(offer.getBankId(), savedOffer, borrowerId);

        List<String> nextSteps = nextStepsService.generateNextSteps(savedOffer, application.getLoanType());

        Organization bank = organizationRepository.findById(offer.getBankId())
                .orElseThrow(() -> new InvalidOfferException("Bank not found: " + offer.getBankId()));

        return new SelectOfferResponse(
                savedOffer.getId(),
                bank.getName(),
                savedOffer.getApr(),
                savedOffer.getMonthlyPayment(),
                savedOffer.getTotalCost(),
                savedOffer.getExpiresAt(),
                nextSteps,
                "Offer selected successfully. Application status updated to ACCEPTED."
        );
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SelectOfferResponse changeOfferSelection(UUID applicationId, UUID borrowerId, UUID newOfferId) {
        log.debug("Offer change requested. ApplicationId: {}, BorrowerId: {}, NewOfferId: {}", 
                applicationId, borrowerId, newOfferId);
        return selectOffer(applicationId, borrowerId, newOfferId);
    }
}
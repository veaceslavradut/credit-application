package com.creditapp.bank.service;

import com.creditapp.bank.dto.WithdrawOfferResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BankOfferWithdrawalService {

    private final OfferRepository offerRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public WithdrawOfferResponse withdrawOffer(UUID bankId, UUID offerId) {
        log.info("Withdrawing offer {} for bank {}", offerId, bankId);

        // Fetch Offer
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));

        // Verify bank submitted this offer
        if (!offer.getBankId().equals(bankId)) {
            throw new IllegalArgumentException("Bank " + bankId + " is not authorized to withdraw this offer");
        }

        // Verify offer status != ACCEPTED
        if (offer.getOfferStatus() == OfferStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Cannot withdraw accepted offer. Offer status: " + offer.getOfferStatus());
        }

        // Update offer status
        offer.setOfferStatus(OfferStatus.WITHDRAWN);
        Offer updatedOffer = offerRepository.save(offer);

        // Create audit log
        auditService.logAction(
                "OFFER",
                offerId,
                com.creditapp.shared.model.AuditAction.OFFER_WITHDRAWN
        );

        // Queue email notification
        try {
            notificationService.createNotification(
                    offer.getApplicationId(),
                    offer.getApplicationId(),
                    com.creditapp.shared.model.NotificationType.APPLICATION_WITHDRAWN,
                    "Offer Update",
                    "The offer has been withdrawn by the bank"
            );
        } catch (Exception e) {
            log.error("Failed to send withdrawal notification", e);
        }

        LocalDateTime withdrawnAt = LocalDateTime.now();
        log.info("Offer {} withdrawn successfully at {}", offerId, withdrawnAt);

        return WithdrawOfferResponse.builder()
                .offerId(offerId)
                .status(updatedOffer.getOfferStatus())
                .withdrawnAt(withdrawnAt)
                .build();
    }
}
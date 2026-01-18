package com.creditapp.bank.service;

import com.creditapp.bank.dto.ApplicationStatusUpdateRequest;
import com.creditapp.bank.dto.ApplicationStatusUpdateResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.validator.ApplicationStatusValidator;
import com.creditapp.bank.websocket.ApplicationQueueWebSocketHandler;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.service.AuditService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatusUpdateService {

    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;
    private final AuditService auditService;
    private final ApplicationQueueWebSocketHandler webSocketHandler;

    @Transactional
    public ApplicationStatusUpdateResponse updateApplicationStatus(
        UUID applicationId,
        UUID bankId,
        ApplicationStatusUpdateRequest request
    ) {
        long start = System.currentTimeMillis();
        log.debug("[QUEUE] Updating application {} status to {} for bank {}", applicationId, request.getStatus(), bankId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));

        Optional<Offer> bankOffer = offerRepository.findByApplicationIdAndBankId(applicationId, bankId);
        if (bankOffer.isEmpty()) {
            log.warn("[QUEUE] Bank {} has no offer for application {}", bankId, applicationId);
            throw new NotFoundException("Bank has no offer for this application");
        }

        Offer offer = bankOffer.get();
        if (!OfferStatus.ACCEPTED.equals(offer.getOfferStatus())) {
            log.warn("[QUEUE] Offer status is {} (not ACCEPTED) for application {} from bank {}",
                offer.getOfferStatus(), applicationId, bankId);
            throw new IllegalArgumentException("Offer has not been accepted by borrower");
        }

        ApplicationStatus targetStatus;
        try {
            targetStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[QUEUE] Invalid status provided: {}", request.getStatus());
            throw new IllegalArgumentException("Invalid application status: " + request.getStatus());
        }

        if (!ApplicationStatusValidator.isValidTransition(application.getStatus(), targetStatus)) {
            String msg = ApplicationStatusValidator.getTransitionErrorMessage(application.getStatus(), targetStatus);
            log.warn("[QUEUE] {}", msg);
            throw new IllegalArgumentException(msg);
        }

        ApplicationStatus previousStatus = application.getStatus();
        application.setStatus(targetStatus);
        application.setUpdatedAt(LocalDateTime.now());
        Application updated = applicationRepository.save(application);
        applicationRepository.flush();

        try {
            Map<String, Object> oldValues = Map.of(
                "status", previousStatus.name(),
                "reason", request.getReason(),
                "comments", request.getComments() != null ? request.getComments() : ""
            );
            Map<String, Object> newValues = Map.of(
                "status", targetStatus.name(),
                "reason", request.getReason(),
                "comments", request.getComments() != null ? request.getComments() : ""
            );
            auditService.logActionWithValues(
                "Application",
                applicationId,
                com.creditapp.shared.model.AuditAction.APPLICATION_STATUS_CHANGED,
                oldValues,
                newValues
            );
        } catch (Exception e) {
            log.error("[QUEUE] Failed to log audit event for application {} status change", applicationId, e);
        }

        // Broadcast status change to connected WebSocket clients
        try {
            webSocketHandler.broadcastStatusChange(
                bankId.toString(),
                applicationId.toString(),
                previousStatus.name(),
                targetStatus.name()
            );
        } catch (Exception e) {
            log.error("[QUEUE] Failed to broadcast status change via WebSocket", e);
        }

        long took = System.currentTimeMillis() - start;
        log.info("[QUEUE] Updated application {} status from {} to {} in {}ms",
            applicationId, previousStatus, targetStatus, took);
        if (took > 200) {
            log.warn("[QUEUE] Performance warning: status update took {}ms (>200ms)", took);
        }

        return ApplicationStatusUpdateResponse.builder()
            .applicationId(applicationId)
            .previousStatus(previousStatus.name())
            .newStatus(targetStatus.name())
            .changedAt(updated.getUpdatedAt())
            .build();
    }
}
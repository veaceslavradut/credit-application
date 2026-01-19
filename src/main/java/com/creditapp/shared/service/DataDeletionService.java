package com.creditapp.shared.service;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.borrower.dto.DeletionRequestResponse;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.DeletionRequest;
import com.creditapp.shared.model.DeletionStatus;
import com.creditapp.shared.model.User;
import com.creditapp.shared.repository.DeletionRequestRepository;
import com.creditapp.shared.repository.GDPRConsentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataDeletionService {
    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRY_DAYS = 7;
    
    private final DeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final GDPRConsentRepository consentRepository;
    private final AuditService auditService;
    private final DataDeletionEmailService dataDeletionEmailService;
    private final DataAnonymizationService anonymizationService;
    
    @Transactional
    public DeletionRequestResponse requestDeletion(UUID borrowerId, String reason) {
        log.info("Initiating deletion request for borrower: {}", borrowerId);
        
        Optional<DeletionRequest> existingPending = deletionRequestRepository.findByBorrowerIdAndStatus(borrowerId, DeletionStatus.PENDING);
        if (existingPending.isPresent()) {
            log.warn("Pending deletion already exists for borrower: {}", borrowerId);
            return DeletionRequestResponse.builder()
                .message("Deletion request already pending. Check your email for confirmation.")
                .status("PENDING")
                .build();
        }
        
        User user = userRepository.findById(borrowerId)
            .orElseThrow(() -> new RuntimeException("User not found: " + borrowerId));
        
        String confirmationToken = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(EXPIRY_DAYS);
        
        DeletionRequest request = DeletionRequest.builder()
            .borrowerId(borrowerId)
            .status(DeletionStatus.PENDING)
            .confirmationToken(confirmationToken)
            .confirmationTokenExpiresAt(expiresAt)
            .requestedAt(LocalDateTime.now())
            .reason(reason)
            .build();
        
        DeletionRequest saved = deletionRequestRepository.save(request);
        dataDeletionEmailService.sendDeletionConfirmationEmail(saved, user.getEmail(), user.getFirstName());
        
        auditService.logActionWithValues(
            "DeletionRequest",
            borrowerId,
            AuditAction.DATA_DELETION_REQUESTED,
            null,
            java.util.Map.of("deletionRequestId", saved.getId().toString(), "expiresAt", expiresAt.toString())
        );
        
        log.info("Deletion request created for borrower: {}", borrowerId);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return DeletionRequestResponse.builder()
            .message("Deletion request submitted. Check your email for confirmation link.")
            .status("PENDING")
            .confirmationLink("/api/borrower/data-deletion/confirm?token=" + confirmationToken)
            .expiresAt(expiresAt.format(formatter))
            .build();
    }
    
    @Transactional
    public DeletionRequestResponse confirmDeletion(String confirmationToken, UUID borrowerId) {
        log.info("Confirming deletion for borrower: {} with token", borrowerId);
        
        DeletionRequest request = deletionRequestRepository.findByConfirmationToken(confirmationToken)
            .orElseThrow(() -> new RuntimeException("Invalid confirmation token"));
        
        if (!request.getBorrowerId().equals(borrowerId)) {
            log.warn("Token does not belong to borrower: {}", borrowerId);
            throw new RuntimeException("Unauthorized");
        }
        
        if (LocalDateTime.now().isAfter(request.getConfirmationTokenExpiresAt())) {
            log.warn("Confirmation token expired for borrower: {}", borrowerId);
            throw new RuntimeException("Confirmation token has expired");
        }
        
        request.setStatus(DeletionStatus.CONFIRMED);
        request.setConfirmedAt(LocalDateTime.now());
        request.setConfirmationToken(null);
        DeletionRequest updated = deletionRequestRepository.save(request);
        
        auditService.logActionWithValues(
            "DeletionRequest",
            borrowerId,
            AuditAction.DATA_DELETION_CONFIRMED,
            null,
            java.util.Map.of("deletionRequestId", updated.getId().toString())
        );
        
        log.info("Deletion confirmed for borrower: {}", borrowerId);
        return DeletionRequestResponse.builder()
            .message("Deletion confirmed. Your data will be erased shortly.")
            .status("CONFIRMED")
            .build();
    }
    
    @Transactional
    @Async
    public void executeDataDeletion(UUID deletionRequestId) {
        log.info("Executing data deletion for deletion request: {}", deletionRequestId);
        
        try {
            DeletionRequest request = deletionRequestRepository.findById(deletionRequestId)
                .orElseThrow(() -> new RuntimeException("Deletion request not found: " + deletionRequestId));
            
            UUID borrowerId = request.getBorrowerId();
            User user = userRepository.findById(borrowerId)
                .orElseThrow(() -> new RuntimeException("User not found: " + borrowerId));
            
            String anonymizedName = anonymizationService.anonymizeName(user.getFirstName());
            String anonymizedEmail = anonymizationService.anonymizeEmail(user.getEmail());
            
            user.setFirstName(anonymizedName);
            user.setLastName(null);
            user.setEmail(anonymizedEmail);
            user.setPhone(null);
            user.setPhoneNumber(null);
            user.setDeletedAt(LocalDateTime.now());
            user.setEnabled(false);
            userRepository.save(user);
            
            consentRepository.findAllByBorrowerId(borrowerId).forEach(consent -> {
                consent.setWithdrawnAt(LocalDateTime.now());
                consentRepository.save(consent);
            });
            
            request.setStatus(DeletionStatus.COMPLETED);
            request.setCompletedAt(LocalDateTime.now());
            deletionRequestRepository.save(request);
            
            auditService.logActionWithValues(
                "DeletionRequest",
                borrowerId,
                AuditAction.DATA_DELETION_COMPLETED,
                null,
                java.util.Map.of("deletionRequestId", deletionRequestId.toString(), "completedAt", LocalDateTime.now().toString())
            );
            
            log.info("Data deletion completed for borrower: {}", borrowerId);
        } catch (Exception e) {
            log.error("Error executing data deletion for deletion request: {}", deletionRequestId, e);
            throw new RuntimeException("Error executing data deletion", e);
        }
    }
    
    @Transactional
    public DeletionRequestResponse cancelDeletion(UUID borrowerId) {
        log.info("Cancelling deletion request for borrower: {}", borrowerId);
        
        DeletionRequest request = deletionRequestRepository.findLatestByBorrowerId(borrowerId)
            .orElseThrow(() -> new RuntimeException("No pending deletion request found"));
        
        if (request.getStatus() != DeletionStatus.PENDING && request.getStatus() != DeletionStatus.CONFIRMED) {
            log.warn("Cannot cancel deletion request with status: {}", request.getStatus());
            throw new RuntimeException("Cannot cancel deletion request with status: " + request.getStatus());
        }
        
        request.setStatus(DeletionStatus.CANCELLED);
        DeletionRequest updated = deletionRequestRepository.save(request);
        
        User user = userRepository.findById(borrowerId).orElse(null);
        if (user != null) {
            dataDeletionEmailService.sendDeletionCancelledEmail(user.getEmail(), user.getFirstName());
        }
        
        auditService.logActionWithValues(
            "DeletionRequest",
            borrowerId,
            AuditAction.DATA_DELETION_CANCELLED,
            null,
            java.util.Map.of("deletionRequestId", updated.getId().toString())
        );
        
        log.info("Deletion request cancelled for borrower: {}", borrowerId);
        return DeletionRequestResponse.builder()
            .message("Deletion request cancelled. Your data is safe.")
            .status("CANCELLED")
            .build();
    }
    
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
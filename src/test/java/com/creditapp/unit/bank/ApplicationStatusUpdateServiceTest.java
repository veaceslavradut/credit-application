package com.creditapp.unit.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.creditapp.bank.dto.ApplicationStatusUpdateRequest;
import com.creditapp.bank.dto.ApplicationStatusUpdateResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.ApplicationStatusUpdateService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ApplicationStatusUpdateServiceTest {

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private OfferRepository offerRepository;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ApplicationStatusUpdateService applicationStatusUpdateService;

    private UUID applicationId;
    private UUID bankId;
    private UUID borrowerId;
    private Application application;
    private Offer acceptedOffer;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        bankId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();

        // Create application
        application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        // Create accepted offer
        acceptedOffer = new Offer(
            UUID.randomUUID(),
            applicationId,
            bankId,
            OfferStatus.ACCEPTED,
            BigDecimal.valueOf(5.5),
            BigDecimal.valueOf(2500),
            BigDecimal.valueOf(90000),
            BigDecimal.valueOf(500),
            BigDecimal.valueOf(100),
            14,
            7,
            "ID,Income",
            LocalDateTime.now().plusDays(7)
        );
    }

    @Test
    void testUpdateApplicationStatusSuccessfully() {
        // Arrange
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("ACCEPTED");
        request.setReason("Approved by bank officer");
        request.setComments("Good credit score and income verification passed");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId))
            .thenReturn(Optional.of(acceptedOffer));

        // Act
        ApplicationStatusUpdateResponse response = applicationStatusUpdateService.updateApplicationStatus(
            applicationId,
            bankId,
            request
        );

        // Assert
        assertThat(response.getApplicationId()).isEqualTo(applicationId);
        assertThat(response.getPreviousStatus()).isEqualTo("OFFERS_AVAILABLE");
        assertThat(response.getNewStatus()).isEqualTo("ACCEPTED");
        assertThat(response.getChangedAt()).isNotNull();

        verify(applicationRepository).save(any(Application.class));
        verify(auditService).logActionWithValues(
            eq("Application"),
            eq(applicationId),
            eq(AuditAction.APPLICATION_STATUS_CHANGED),
            any(Map.class),
            any(Map.class)
        );
    }

    @Test
    void testUpdateApplicationStatusApplicationNotFound() {
        // Arrange
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("ACCEPTED");
        request.setReason("Approved");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            applicationStatusUpdateService.updateApplicationStatus(applicationId, bankId, request);
        });
    }

    @Test
    void testUpdateApplicationStatusOfferNotFound() {
        // Arrange
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("ACCEPTED");
        request.setReason("Approved");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            applicationStatusUpdateService.updateApplicationStatus(applicationId, bankId, request);
        });
    }

    @Test
    void testUpdateApplicationStatusOfferNotAccepted() {
        // Arrange
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("ACCEPTED");
        request.setReason("Approved");

        Offer pendingOffer = new Offer(
            UUID.randomUUID(),
            applicationId,
            bankId,
            OfferStatus.SUBMITTED,
            BigDecimal.valueOf(5.5),
            BigDecimal.valueOf(2500),
            BigDecimal.valueOf(90000),
            BigDecimal.valueOf(500),
            BigDecimal.valueOf(100),
            14,
            7,
            "ID,Income",
            LocalDateTime.now().plusDays(7)
        );

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId))
            .thenReturn(Optional.of(pendingOffer));

        // Act & Assert - Offer must be ACCEPTED
        assertThrows(RuntimeException.class, () -> {
            applicationStatusUpdateService.updateApplicationStatus(applicationId, bankId, request);
        });
    }

    @Test
    void testUpdateApplicationStatusInvalidTransition() {
        // Arrange
        Application rejectedApp = new Application();
        rejectedApp.setId(applicationId);
        rejectedApp.setBorrowerId(borrowerId);
        rejectedApp.setStatus(ApplicationStatus.REJECTED);
        rejectedApp.setCreatedAt(LocalDateTime.now());
        rejectedApp.setUpdatedAt(LocalDateTime.now());

        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("OFFERS_AVAILABLE");
        request.setReason("Reopen application");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(rejectedApp));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId))
            .thenReturn(Optional.of(acceptedOffer));

        // Act & Assert - Cannot transition from REJECTED to OFFERS_AVAILABLE
        assertThrows(RuntimeException.class, () -> {
            applicationStatusUpdateService.updateApplicationStatus(applicationId, bankId, request);
        });
    }

    @Test
    void testUpdateApplicationStatusUpdatesTimestamp() {
        // Arrange
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("ACCEPTED");
        request.setReason("Approved");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId))
            .thenReturn(Optional.of(acceptedOffer));

        LocalDateTime beforeUpdate = LocalDateTime.now();

        // Act
        ApplicationStatusUpdateResponse response = applicationStatusUpdateService.updateApplicationStatus(
            applicationId,
            bankId,
            request
        );

        LocalDateTime afterUpdate = LocalDateTime.now();

        // Assert
        assertThat(response.getChangedAt()).isNotNull();
        assertThat(response.getChangedAt()).isAfterOrEqualTo(beforeUpdate);
        assertThat(response.getChangedAt()).isBeforeOrEqualTo(afterUpdate);
    }
}
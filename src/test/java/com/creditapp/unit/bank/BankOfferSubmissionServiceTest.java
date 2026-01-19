package com.creditapp.unit.bank;

import com.creditapp.bank.dto.OfferSubmissionRequest;
import com.creditapp.bank.dto.OfferSubmissionResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankOfferSubmissionService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankOfferSubmissionServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BankOfferSubmissionService service;

    private UUID bankId;
    private UUID applicationId;
    private Application application;
    private OfferSubmissionRequest request;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        applicationId = UUID.randomUUID();

        application = new Application();
        application.setId(applicationId);
        application.setLoanAmount(new BigDecimal("10000"));
        application.setLoanTermMonths(12);
        application.setBorrowerId(UUID.randomUUID());
    }

    @Test
    void submitOffer_withAcceptCalculatedOffer_shouldCreateNewOffer() {
        // Given
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(true)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId)).thenReturn(Optional.empty());
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OfferSubmissionResponse response = service.submitOffer(bankId, applicationId, request);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getHttpStatus());
        assertNotNull(response.getOfferId());
        assertNotNull(response.getMonthlyPayment());
        assertNotNull(response.getApr());
        verify(offerRepository).save(any(Offer.class));
        verify(auditService).logAction(eq("Offer"), any(UUID.class), any());
    }

    @Test
    void submitOffer_withOverrideAPR_shouldRecalculateMonthlyPayment() {
        // Given
        BigDecimal customAPR = new BigDecimal("15.0");
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(false)
                .overrideAPR(customAPR)
                .overrideFees(new BigDecimal("200.00"))
                .overrideProcessingTime(7)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId)).thenReturn(Optional.empty());
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OfferSubmissionResponse response = service.submitOffer(bankId, applicationId, request);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getHttpStatus());
        assertEquals(customAPR, response.getApr());
        assertEquals(new BigDecimal("200.00"), response.getFees());
        assertEquals(7, response.getProcessingTime());
        assertNotNull(response.getMonthlyPayment());
        assertTrue(response.getMonthlyPayment().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void submitOffer_withAPRBelowMinimum_shouldThrowException() {
        // Given
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(false)
                .overrideAPR(new BigDecimal("0.3"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> service.submitOffer(bankId, applicationId, request));
        assertTrue(exception.getMessage().contains("APR must be between 0.5% and 50%"));
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void submitOffer_withAPRAboveMaximum_shouldThrowException() {
        // Given
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(false)
                .overrideAPR(new BigDecimal("55.0"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> service.submitOffer(bankId, applicationId, request));
        assertTrue(exception.getMessage().contains("APR must be between 0.5% and 50%"));
    }

    @Test
    void submitOffer_withIdempotency_shouldReturn200WithExistingOffer() {
        // Given
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(true)
                .build();

        Offer existingOffer = new Offer();
        existingOffer.setId(UUID.randomUUID());
        existingOffer.setApplicationId(applicationId);
        existingOffer.setBankId(bankId);
        existingOffer.setApr(new BigDecimal("10.5800"));
        existingOffer.setMonthlyPayment(new BigDecimal("879.53"));
        existingOffer.setTotalCost(new BigDecimal("10554.36"));
        existingOffer.setOriginationFee(new BigDecimal("150.00"));
        existingOffer.setProcessingTimeDays(5);
        // createdAt is set by @CreationTimestamp annotation

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId))
                .thenReturn(Optional.of(existingOffer));

        // When
        OfferSubmissionResponse response = service.submitOffer(bankId, applicationId, request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getHttpStatus()); // 200 OK for idempotent request
        assertEquals(existingOffer.getId(), response.getOfferId());
        assertEquals(existingOffer.getApr(), response.getApr());
        verify(offerRepository, never()).save(any(Offer.class)); // Should NOT save again
    }

    @Test
    void submitOffer_withApplicationNotFound_shouldThrowException() {
        // Given
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(true)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> service.submitOffer(bankId, applicationId, request));
        assertTrue(exception.getMessage().contains("Application not found"));
    }

    @Test
    void submitOffer_withValidOverrides_shouldUseProvidedValues() {
        // Given
        BigDecimal customAPR = new BigDecimal("12.5");
        BigDecimal customFees = new BigDecimal("300.00");
        Integer customProcessingTime = 10;

        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(false)
                .overrideAPR(customAPR)
                .overrideFees(customFees)
                .overrideProcessingTime(customProcessingTime)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId)).thenReturn(Optional.empty());
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OfferSubmissionResponse response = service.submitOffer(bankId, applicationId, request);

        // Then
        assertNotNull(response);
        assertEquals(customAPR, response.getApr());
        assertEquals(customFees, response.getFees());
        assertEquals(customProcessingTime, response.getProcessingTime());
    }

    @Test
    void submitOffer_shouldCalculateTotalCostCorrectly() {
        // Given
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(false)
                .overrideAPR(new BigDecimal("10.0"))
                .overrideFees(new BigDecimal("150.00"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId)).thenReturn(Optional.empty());
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OfferSubmissionResponse response = service.submitOffer(bankId, applicationId, request);

        // Then
        assertNotNull(response);
        // Total cost = (monthlyPayment  12 months) + fees
        BigDecimal expectedTotalCost = response.getMonthlyPayment()
                .multiply(new BigDecimal(12))
                .add(new BigDecimal("150.00"));
        assertEquals(expectedTotalCost.setScale(2), response.getTotalCost().setScale(2));
    }

    @Test
    void submitOffer_shouldLogAuditEvent() {
        // Given
        request = OfferSubmissionRequest.builder()
                .applicationId(applicationId)
                .acceptCalculatedOffer(true)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationIdAndBankId(applicationId, bankId)).thenReturn(Optional.empty());
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> {
            Offer offer = invocation.getArgument(0);
            offer.setId(UUID.randomUUID());
            return offer;
        });

        // When
        service.submitOffer(bankId, applicationId, request);

        // Then
        verify(auditService, times(1)).logAction(eq("Offer"), any(UUID.class), any());
    }
}

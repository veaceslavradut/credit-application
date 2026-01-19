package com.creditapp.unit.bank;

import com.creditapp.bank.dto.DeclineApplicationRequest;
import com.creditapp.bank.dto.DeclineApplicationResponse;
import com.creditapp.bank.dto.WithdrawOfferResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankApplicationDeclineService;
import com.creditapp.bank.service.BankOfferWithdrawalService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankApplicationDeclineWithdrawalTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BankApplicationDeclineService declineService;

    @InjectMocks
    private BankOfferWithdrawalService withdrawalService;

    private UUID bankId;
    private UUID applicationId;
    private UUID offerId;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        offerId = UUID.randomUUID();
    }

    @Test
    void testDeclineApplicationWithValidState() {
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setBorrowerId(UUID.randomUUID());

        DeclineApplicationRequest request = DeclineApplicationRequest.builder()
                .reason("Credit score too low")
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any())).thenReturn(application);

        DeclineApplicationResponse response = declineService.declineApplication(bankId, applicationId, request);

        assertNotNull(response);
        assertEquals(applicationId, response.getApplicationId());
        assertEquals(ApplicationStatus.REJECTED, response.getStatus());
        assertEquals("Credit score too low", response.getReason());
        
        verify(auditService).logAction(any(), any(), any());
    }

    @Test
    void testDeclineApplicationWithoutReason() {
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setBorrowerId(UUID.randomUUID());

        DeclineApplicationRequest request = DeclineApplicationRequest.builder()
                .reason(null)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any())).thenReturn(application);

        DeclineApplicationResponse response = declineService.declineApplication(bankId, applicationId, request);

        assertNotNull(response);
        assertNull(response.getReason());
    }

    @Test
    void testDeclineApplicationRejectWhenOffersAvailable() {
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        DeclineApplicationRequest request = DeclineApplicationRequest.builder()
                .reason("Loan amount too high")
                .build();

        assertThrows(IllegalStateException.class, 
                () -> declineService.declineApplication(bankId, applicationId, request));
    }

    @Test
    void testDeclineApplicationWithInvalidReasonLength() {
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ApplicationStatus.SUBMITTED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        String longReason = "a".repeat(501);
        DeclineApplicationRequest request = DeclineApplicationRequest.builder()
                .reason(longReason)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> declineService.declineApplication(bankId, applicationId, request));
    }

    @Test
    void testDeclineApplicationNotFound() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        DeclineApplicationRequest request = DeclineApplicationRequest.builder()
                .reason("Some reason")
                .build();

        assertThrows(NotFoundException.class,
                () -> declineService.declineApplication(bankId, applicationId, request));
    }

    @Test
    void testWithdrawOfferWithValidState() {
        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        offer.setApplicationId(applicationId);

        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any())).thenReturn(offer);

        WithdrawOfferResponse response = withdrawalService.withdrawOffer(bankId, offerId);

        assertNotNull(response);
        assertEquals(offerId, response.getOfferId());
        assertEquals(OfferStatus.WITHDRAWN, response.getStatus());
        assertNotNull(response.getWithdrawnAt());
        
        verify(auditService).logAction(any(), any(), any());
    }

    @Test
    void testWithdrawOfferRejectWhenAccepted() {
        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.ACCEPTED);

        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        assertThrows(IllegalStateException.class,
                () -> withdrawalService.withdrawOffer(bankId, offerId));
    }

    @Test
    void testWithdrawOfferUnauthorizedBank() {
        UUID differentBankId = UUID.randomUUID();
        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setBankId(differentBankId);
        offer.setOfferStatus(OfferStatus.SUBMITTED);

        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class,
                () -> withdrawalService.withdrawOffer(bankId, offerId));
    }

    @Test
    void testWithdrawOfferNotFound() {
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> withdrawalService.withdrawOffer(bankId, offerId));
    }
}
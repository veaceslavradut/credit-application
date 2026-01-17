package com.creditapp.unit.borrower;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.SelectOfferResponse;
import com.creditapp.borrower.exception.InvalidOfferException;
import com.creditapp.borrower.exception.OfferExpiredException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.NextStepsService;
import com.creditapp.borrower.service.OfferSelectionService;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.OfferSelectionEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferSelectionServiceTest {

    private OfferSelectionService offerSelectionService;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OfferSelectionEmailService emailService;

    @Mock
    private AuditService auditService;

    @Mock
    private NextStepsService nextStepsService;

    private UUID applicationId;
    private UUID borrowerId;
    private UUID offerId;
    private UUID bankId;

    @BeforeEach
    public void setUp() {
        offerSelectionService = new OfferSelectionService(
                offerRepository,
                applicationRepository,
                organizationRepository,
                emailService,
                auditService,
                nextStepsService
        );

        applicationId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
        offerId = UUID.randomUUID();
        bankId = UUID.randomUUID();
    }

    @Test
    public void testSelectOfferSuccessfully() {
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
        application.setLoanType("HOME");

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setApr(BigDecimal.valueOf(3.5));
        offer.setMonthlyPayment(BigDecimal.valueOf(1500));
        offer.setTotalCost(BigDecimal.valueOf(540000));
        offer.setExpiresAt(LocalDateTime.now().plusDays(7));

        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Example Bank");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
        when(offerRepository.findByApplicationIdAndOfferStatus(applicationId, OfferStatus.ACCEPTED)).thenReturn(Optional.empty());
        when(offerRepository.save(offer)).thenReturn(offer);
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(bank));
        when(nextStepsService.generateNextSteps(offer, "HOME")).thenReturn(
                java.util.Arrays.asList("Review terms", "Submit docs", "Sign")
        );

        SelectOfferResponse response = offerSelectionService.selectOffer(applicationId, borrowerId, offerId);

        assertNotNull(response);
        assertEquals(offerId, response.getSelectedOfferId());
        assertEquals("Example Bank", response.getBankName());
        assertEquals(OfferStatus.ACCEPTED, offer.getOfferStatus());
        assertNotNull(offer.getBorrowerSelectedAt());

        verify(offerRepository).save(offer);
        verify(applicationRepository).save(application);
        verify(auditService).logActionWithValues(eq("Offer"), eq(offerId), eq(AuditAction.OFFER_SELECTED), any(), any());
        verify(emailService).sendOfferSelectedToBorrower(borrowerId, offer, application);
        verify(emailService).sendOfferSelectedToBank(bankId, offer, borrowerId);
    }

    @Test
    public void testSelectOfferExpired() {
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setApplicationId(applicationId);
        offer.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        assertThrows(OfferExpiredException.class, () ->
                offerSelectionService.selectOffer(applicationId, borrowerId, offerId)
        );
        
        // Verify audit event logged for failed selection attempt
        verify(auditService).logAction(eq("Offer"), eq(offerId), eq(AuditAction.OFFER_SELECTION_FAILED), eq(borrowerId), eq("BORROWER"));
    }

    @Test
    public void testSelectOfferUnauthorized() {
        UUID differentBorrowerId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        assertThrows(InvalidOfferException.class, () ->
                offerSelectionService.selectOffer(applicationId, differentBorrowerId, offerId)
        );
    }

    @Test
    public void testSelectOfferNotBelongingToApplication() {
        UUID differentApplicationId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setApplicationId(differentApplicationId);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        assertThrows(InvalidOfferException.class, () ->
                offerSelectionService.selectOffer(applicationId, borrowerId, offerId)
        );
    }

    @Test
    public void testSelectOfferApplicationNotFound() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(InvalidOfferException.class, () ->
                offerSelectionService.selectOffer(applicationId, borrowerId, offerId)
        );
    }

    @Test
    public void testChangeOfferSelection() {
        UUID newOfferId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setLoanType("HOME");

        Offer oldOffer = new Offer();
        oldOffer.setId(offerId);
        oldOffer.setApplicationId(applicationId);
        oldOffer.setOfferStatus(OfferStatus.ACCEPTED);
        oldOffer.setBankId(bankId);

        Offer newOffer = new Offer();
        newOffer.setId(newOfferId);
        newOffer.setApplicationId(applicationId);
        newOffer.setBankId(bankId);
        newOffer.setOfferStatus(OfferStatus.CALCULATED);
        newOffer.setExpiresAt(LocalDateTime.now().plusDays(7));

        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Example Bank");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findById(newOfferId)).thenReturn(Optional.of(newOffer));
        when(offerRepository.findByApplicationIdAndOfferStatus(applicationId, OfferStatus.ACCEPTED)).thenReturn(Optional.of(oldOffer));
        when(offerRepository.save(oldOffer)).thenReturn(oldOffer);
        when(offerRepository.save(newOffer)).thenReturn(newOffer);
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(bank));
        when(nextStepsService.generateNextSteps(newOffer, "HOME")).thenReturn(
                java.util.Arrays.asList("Review", "Sign")
        );

        SelectOfferResponse response = offerSelectionService.changeOfferSelection(applicationId, borrowerId, newOfferId);

        assertNotNull(response);
        assertEquals(newOfferId, response.getSelectedOfferId());
        assertEquals(OfferStatus.CALCULATED, oldOffer.getOfferStatus());
        assertEquals(OfferStatus.ACCEPTED, newOffer.getOfferStatus());
        
        // Verify deselection and selection audit events logged
        verify(auditService).logActionWithValues(eq("Offer"), eq(offerId), eq(AuditAction.OFFER_DESELECTED), any(), any());
        verify(auditService).logActionWithValues(eq("Offer"), eq(newOfferId), eq(AuditAction.OFFER_SELECTED), any(), any());
    }

    @Test
    public void testBorrowerSelectedAtTimestampSet() {
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setLoanType("AUTO");

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setExpiresAt(LocalDateTime.now().plusDays(7));

        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Bank");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
        when(offerRepository.findByApplicationIdAndOfferStatus(applicationId, OfferStatus.ACCEPTED)).thenReturn(Optional.empty());
        when(offerRepository.save(offer)).thenReturn(offer);
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(bank));
        when(nextStepsService.generateNextSteps(offer, "AUTO")).thenReturn(java.util.Collections.emptyList());

        offerSelectionService.selectOffer(applicationId, borrowerId, offerId);

        assertNotNull(offer.getBorrowerSelectedAt());
    }

    @Test
    public void testApplicationStatusUpdatedToAccepted() {
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
        application.setLoanType("PERSONAL");

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setExpiresAt(LocalDateTime.now().plusDays(7));

        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Bank");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
        when(offerRepository.findByApplicationIdAndOfferStatus(applicationId, OfferStatus.ACCEPTED)).thenReturn(Optional.empty());
        when(offerRepository.save(offer)).thenReturn(offer);
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(bank));
        when(nextStepsService.generateNextSteps(offer, "PERSONAL")).thenReturn(java.util.Collections.emptyList());

        offerSelectionService.selectOffer(applicationId, borrowerId, offerId);

        assertEquals(ApplicationStatus.ACCEPTED, application.getStatus());
        verify(applicationRepository).save(application);
    }
}
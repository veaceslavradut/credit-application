package com.creditapp.integration.bank;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankApplicationDeclineService;
import com.creditapp.bank.service.BankOfferWithdrawalService;
import com.creditapp.bank.dto.DeclineApplicationRequest;
import com.creditapp.bank.dto.DeclineApplicationResponse;
import com.creditapp.bank.dto.WithdrawOfferResponse;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankApplicationDeclineWithdrawalIntegrationTest {

    @Autowired
    private BankApplicationDeclineService declineService;

    @Autowired
    private BankOfferWithdrawalService withdrawalService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID bankId;
    private User borrower;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        
        // Create test borrower
        borrower = new User();
        borrower.setEmail("borrower@test.com");
        borrower.setPasswordHash("hashed");
        borrower.setRole(UserRole.BORROWER);
        borrower.setFirstName("Test");
        borrower.setLastName("Borrower");
        borrower = userRepository.save(borrower);
    }

    @Test
    void testDeclineApplicationBeforeOffersAvailable() {
        // Create application
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setBorrowerId(borrower.getId());
        application.setLoanType("PERSONAL");
        application.setLoanAmount(new BigDecimal("25000"));
        application.setLoanTermMonths(60);
        application.setCurrency("USD");
        application.setStatus(ApplicationStatus.SUBMITTED);
        application = applicationRepository.save(application);

        // Decline application
        DeclineApplicationRequest request = DeclineApplicationRequest.builder()
                .reason("Credit score too low")
                .build();

        DeclineApplicationResponse response = declineService.declineApplication(bankId, application.getId(), request);

        assertNotNull(response);
        assertEquals(application.getId(), response.getApplicationId());
        assertEquals(ApplicationStatus.REJECTED, response.getStatus());
        assertEquals("Credit score too low", response.getReason());
        assertNotNull(response.getDeclinedAt());

        // Verify application status updated
        Application updated = applicationRepository.findById(application.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(ApplicationStatus.REJECTED, updated.getStatus());
    }

    @Test
    void testWithdrawOfferBeforeBorrowerAccepts() {
        // Create application
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setBorrowerId(borrower.getId());
        application.setLoanType("HOME");
        application.setLoanAmount(new BigDecimal("300000"));
        application.setLoanTermMonths(360);
        application.setCurrency("USD");
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
        application = applicationRepository.save(application);

        // Create offer
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(application.getId());
        offer.setBankId(bankId);
        offer.setApr(new BigDecimal("4.5"));
        offer.setMonthlyPayment(new BigDecimal("1520"));
        offer.setTotalCost(new BigDecimal("547200"));
        offer.setOriginationFee(new BigDecimal("3000"));
        offer.setProcessingTimeDays(5);
        offer.setValidityPeriodDays(30);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        offer.setOfferSubmittedAt(LocalDateTime.now());
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        offer = offerRepository.save(offer);

        // Withdraw offer
        WithdrawOfferResponse response = withdrawalService.withdrawOffer(bankId, offer.getId());

        assertNotNull(response);
        assertEquals(offer.getId(), response.getOfferId());
        assertEquals(OfferStatus.WITHDRAWN, response.getStatus());
        assertNotNull(response.getWithdrawnAt());

        // Verify offer status updated
        Offer updated = offerRepository.findById(offer.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(OfferStatus.WITHDRAWN, updated.getOfferStatus());
    }

    @Test
    void testCannotWithdrawAcceptedOffer() {
        // Create application
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setBorrowerId(borrower.getId());
        application.setLoanType("AUTO");
        application.setLoanAmount(new BigDecimal("35000"));
        application.setLoanTermMonths(72);
        application.setCurrency("USD");
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
        application = applicationRepository.save(application);

        // Create accepted offer
        Offer acceptedOffer = new Offer();
        acceptedOffer.setId(UUID.randomUUID());
        acceptedOffer.setApplicationId(application.getId());
        acceptedOffer.setBankId(bankId);
        acceptedOffer.setApr(new BigDecimal("6.0"));
        acceptedOffer.setMonthlyPayment(new BigDecimal("560"));
        acceptedOffer.setTotalCost(new BigDecimal("40320"));
        acceptedOffer.setOriginationFee(new BigDecimal("1000"));
        acceptedOffer.setProcessingTimeDays(3);
        acceptedOffer.setValidityPeriodDays(14);
        acceptedOffer.setOfferStatus(OfferStatus.ACCEPTED);
        acceptedOffer.setOfferSubmittedAt(LocalDateTime.now());
        acceptedOffer.setExpiresAt(LocalDateTime.now().plusDays(14));
        acceptedOffer = offerRepository.save(acceptedOffer);

        // Try to withdraw - should fail
        UUID offerId = acceptedOffer.getId();
        assertThrows(IllegalStateException.class,
                () -> withdrawalService.withdrawOffer(bankId, offerId));
    }

    @Test
    void testDeclineApplicationWithEmptyReason() {
        // Create application
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setBorrowerId(borrower.getId());
        application.setLoanType("BUSINESS");
        application.setLoanAmount(new BigDecimal("100000"));
        application.setLoanTermMonths(84);
        application.setCurrency("USD");
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application = applicationRepository.save(application);

        // Decline without reason
        DeclineApplicationRequest request = DeclineApplicationRequest.builder()
                .reason(null)
                .build();

        DeclineApplicationResponse response = declineService.declineApplication(bankId, application.getId(), request);

        assertNotNull(response);
        assertNull(response.getReason());
        assertEquals(ApplicationStatus.REJECTED, response.getStatus());
    }
}
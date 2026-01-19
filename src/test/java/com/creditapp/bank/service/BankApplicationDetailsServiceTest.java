package com.creditapp.bank.service;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.bank.dto.ApplicationDetailsResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BankApplicationDetailsService Tests")
class BankApplicationDetailsServiceTest {

    @Autowired
    private BankApplicationDetailsService service;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    private UUID bankId;
    private UUID borrowerId;
    private UUID applicationId;
    private UUID offerId;

    @BeforeEach
    void setup() {
        bankId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        offerId = UUID.randomUUID();

        // Create borrower user
        User borrower = new User();
        borrower.setId(borrowerId);
        borrower.setEmail("borrower@example.com");
        borrower.setFirstName("John");
        borrower.setLastName("Doe");
        borrower.setRole(UserRole.BORROWER);
        borrower.setPasswordHash("hashed_password");
        borrower.setIsActive(true);
        userRepository.save(borrower);

        // Create application
        Application application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setLoanType("PERSONAL");
        application.setLoanAmount(BigDecimal.valueOf(50000));
        application.setLoanTermMonths(60);
        application.setCurrency("EUR");
        application.setStatus(ApplicationStatus.SUBMITTED);
        applicationRepository.save(application);

        // Create offer for bank
        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.CALCULATED);
        offer.setApr(BigDecimal.valueOf(8.5));
        offer.setMonthlyPayment(BigDecimal.valueOf(1200));
        offer.setTotalCost(BigDecimal.valueOf(72000));
        offer.setOriginationFee(BigDecimal.valueOf(1500));
        offer.setProcessingTimeDays(5);
        offer.setValidityPeriodDays(30);
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        offerRepository.save(offer);
    }

    @Test
    @DisplayName("Should retrieve application details successfully")
    void testGetApplicationDetails_Success() {
        ApplicationDetailsResponse response = service.getApplicationDetails(bankId, applicationId);
        assertThat(response).isNotNull();
        assertThat(response.getApplicationId()).isEqualTo(applicationId);
    }

    @Test
    @DisplayName("Should include borrower details")
    void testGetApplicationDetails_BorrowerDetails() {
        ApplicationDetailsResponse response = service.getApplicationDetails(bankId, applicationId);
        assertThat(response.getBorrower()).isNotNull();
        assertThat(response.getBorrower().getFirstName()).isEqualTo("John");
        assertThat(response.getBorrower().getLastName()).isEqualTo("Doe");
        assertThat(response.getBorrower().getEmail()).isEqualTo("borrower@example.com");
    }

    @Test
    @DisplayName("Should include loan request details")
    void testGetApplicationDetails_LoanDetails() {
        ApplicationDetailsResponse response = service.getApplicationDetails(bankId, applicationId);
        assertThat(response.getLoanRequest()).isNotNull();
        assertThat(response.getLoanRequest().getLoanType()).isEqualTo("PERSONAL");
        assertThat(response.getLoanRequest().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(response.getLoanRequest().getTermMonths()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should include consent details with 3 consents")
    void testGetApplicationDetails_Consents() {
        ApplicationDetailsResponse response = service.getApplicationDetails(bankId, applicationId);
        assertThat(response.getConsents()).hasSize(3);
        assertThat(response.getConsents().get(0).getConsentNumber()).isEqualTo(1);
        assertThat(response.getConsents().get(1).getConsentNumber()).isEqualTo(2);
        assertThat(response.getConsents().get(2).getConsentNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should include offer details")
    void testGetApplicationDetails_Offer() {
        ApplicationDetailsResponse response = service.getApplicationDetails(bankId, applicationId);
        assertThat(response.getOffer()).isNotNull();
        assertThat(response.getOffer().id).isEqualTo(offerId);
        assertThat(response.getOffer().apr).isEqualByComparingTo(BigDecimal.valueOf(8.5));
    }

    @Test
    @DisplayName("Should throw when bank has no offer")
    void testGetApplicationDetails_NoOffer() {
        UUID differentBankId = UUID.randomUUID();
        assertThatThrownBy(() -> service.getApplicationDetails(differentBankId, applicationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bank has no offer");
    }

    @Test
    @DisplayName("Should throw when application not found")
    void testGetApplicationDetails_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        assertThatThrownBy(() -> service.getApplicationDetails(bankId, nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Application not found");
    }
}
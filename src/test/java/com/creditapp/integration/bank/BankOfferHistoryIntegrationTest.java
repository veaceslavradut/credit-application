package com.creditapp.integration.bank;

import com.creditapp.bank.dto.OfferHistoryFilter;
import com.creditapp.bank.dto.OfferHistoryResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankOfferHistoryService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BankOfferHistoryIntegrationTest {
    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private BankOfferHistoryService offerHistoryService;
    private UUID bankId;
    private List<UUID> applicationIds;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        applicationIds = new ArrayList<>();
        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Test Bank");
        bank.setCountryCode("MD");
        bank.setTaxId("123");
        bank.setRegistrationNumber("REG-001");
        bank.setStatus(BankStatus.ACTIVE);
        organizationRepository.save(bank);

        for (int i = 0; i < 50; i++) {
            User borrower = new User();
            borrower.setEmail("borrower" + i + "@test.com");
            borrower.setPasswordHash("hashed");
            borrower.setFirstName("B");
            borrower.setLastName("T");
            borrower.setRole(UserRole.BORROWER);
            borrower = userRepository.save(borrower);
            Application app = new Application();
            app.setBorrowerId(borrower.getId());
            app.setLoanType("PERSONAL");
            app.setLoanAmount(new BigDecimal("50000"));
            app.setLoanTermMonths(60);
            app.setCurrency("EUR");
            app.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
            app = applicationRepository.save(app);
            applicationIds.add(app.getId());
        }
    }

    @Test
    void testPagination() {
        for (int i = 0; i < 40; i++) {
            Offer offer = new Offer();
            offer.setId(UUID.randomUUID());
            offer.setApplicationId(applicationIds.get(i));
            offer.setBankId(bankId);
            offer.setApr(new BigDecimal("5.5"));
            offer.setMonthlyPayment(new BigDecimal("1000"));
            offer.setTotalCost(new BigDecimal("60000"));
            offer.setOriginationFee(new BigDecimal("500"));
            offer.setValidityPeriodDays(30);
            offer.setExpiresAt(LocalDateTime.now().plusDays(30));
            offer.setProcessingTimeDays(5);
            offer.setOfferStatus(OfferStatus.SUBMITTED);
            offer.setOfferSubmittedAt(LocalDateTime.now().minusDays(i));
            offerRepository.save(offer);
        }
        OfferHistoryFilter filter = new OfferHistoryFilter(null,null,null,null,null,null,null,null);
        OfferHistoryResponse response = offerHistoryService.getOfferHistory(bankId,filter,0,20);
        assertThat(response.getItems()).hasSize(20);
        assertThat(response.getTotalCount()).isEqualTo(40);
    }

    @Test
    void testSorting() {
        for (int i = 0; i < 10; i++) {
            Offer offer = new Offer();
            offer.setId(UUID.randomUUID());
            offer.setApplicationId(applicationIds.get(i));
            offer.setBankId(bankId);
            offer.setApr(new BigDecimal("10.0").subtract(new BigDecimal(i)));
            offer.setMonthlyPayment(new BigDecimal("1000"));
            offer.setTotalCost(new BigDecimal("60000"));
            offer.setOriginationFee(new BigDecimal("500"));
            offer.setValidityPeriodDays(30);
            offer.setExpiresAt(LocalDateTime.now().plusDays(30));
            offer.setProcessingTimeDays(5);
            offer.setOfferStatus(OfferStatus.SUBMITTED);
            offer.setOfferSubmittedAt(LocalDateTime.now());
            offerRepository.save(offer);
        }
        OfferHistoryFilter filter = new OfferHistoryFilter(null,null,null,null,null,null,null,"apr_ASC");
        OfferHistoryResponse response = offerHistoryService.getOfferHistory(bankId,filter,0,100);
        assertThat(response.getItems()).hasSize(10);
        for (int i = 1; i < response.getItems().size(); i++) {
            assertThat(response.getItems().get(i-1).getApr())
                .isLessThanOrEqualTo(response.getItems().get(i).getApr());
        }
    }

    @Test
    void testPerformance() {
        for (int i = 0; i < 300; i++) {
            Offer offer = new Offer();
            offer.setId(UUID.randomUUID());
            offer.setApplicationId(applicationIds.get(i % applicationIds.size()));
            offer.setBankId(bankId);
            offer.setApr(new BigDecimal("5.5"));
            offer.setMonthlyPayment(new BigDecimal("1000"));
            offer.setTotalCost(new BigDecimal("60000"));
            offer.setOriginationFee(new BigDecimal("500"));
            offer.setValidityPeriodDays(30);
            offer.setExpiresAt(LocalDateTime.now().plusDays(30));
            offer.setProcessingTimeDays(5);
            offer.setOfferStatus(OfferStatus.SUBMITTED);
            offer.setOfferSubmittedAt(LocalDateTime.now());
            offerRepository.save(offer);
        }
        OfferHistoryFilter filter = new OfferHistoryFilter(null,null,null,null,null,null,null,null);
        long start = System.currentTimeMillis();
        OfferHistoryResponse response = offerHistoryService.getOfferHistory(bankId,filter,0,20);
        long duration = System.currentTimeMillis() - start;
        assertThat(response.getTotalCount()).isEqualTo(300);
        assertThat(duration).isLessThan(200);
    }
}
package com.creditapp.integration.bank;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.bank.dto.AnalyticsRequest;
import com.creditapp.bank.dto.AnalyticsResponseDTO;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankAnalyticsService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankAnalyticsIntegrationTest {

    @Autowired
    private BankAnalyticsService analyticsService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID bankId;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        applicationRepository.deleteAll();
        offerRepository.deleteAll();
    }

    @Test
    void testGetAnalytics_WithData() {
        createTestData(50);

        AnalyticsRequest request = AnalyticsRequest.builder()
                .dateFrom(LocalDate.now().minusDays(30))
                .dateTo(LocalDate.now())
                .preset(AnalyticsRequest.DatePreset.CUSTOM)
                .build();

        AnalyticsResponseDTO response = analyticsService.getAnalytics(bankId, request);

        assertNotNull(response);
        assertNotNull(response.getMetrics());
        assertTrue(response.getMetrics().getApplicationsReceived() > 0);
        assertTrue(response.getMetrics().getOffersSubmitted() > 0);
        assertNotNull(response.getMetrics().getConversionRate());
        assertNotNull(response.getTrends());
        assertNotNull(response.getLoanTypeBreakdown());
        assertNotNull(response.getAmountRangeBreakdown());
    }

    @Test
    void testGetAnalytics_WithPreset() {
        createTestData(20);

        AnalyticsRequest request = AnalyticsRequest.builder()
                .preset(AnalyticsRequest.DatePreset.LAST_7)
                .build();

        AnalyticsResponseDTO response = analyticsService.getAnalytics(bankId, request);

        assertNotNull(response);
        assertNotNull(response.getMetrics());
        assertNotNull(response.getReportGeneratedAt());
    }

    @Test
    void testGetLoanTypeBreakdown() {
        createTestData(30);

        var breakdown = analyticsService.getAcceptanceByLoanType(
                bankId,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        assertNotNull(breakdown);
        assertTrue(breakdown.containsKey("PERSONAL"));
        assertTrue(breakdown.get("PERSONAL").getCount() > 0);
        assertNotNull(breakdown.get("PERSONAL").getAcceptanceRate());
    }

    @Test
    void testGetAmountRangeBreakdown() {
        createTestData(40);

        var breakdown = analyticsService.getAcceptanceByAmountRange(
                bankId,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        assertNotNull(breakdown);
        assertTrue(breakdown.containsKey("0-10k"));
        assertTrue(breakdown.containsKey("10k-25k"));
        assertTrue(breakdown.containsKey("25k-50k"));
    }

    @Test
    void testGetTrends_Daily() {
        createTestData(25);

        var trends = analyticsService.getTrendData(
                bankId,
                LocalDate.now().minusDays(6),
                LocalDate.now(),
                "DAILY"
        );

        assertNotNull(trends);
        assertEquals(7, trends.size());
        assertNotNull(trends.get(0).getDate());
        assertNotNull(trends.get(0).getApplicationsCount());
    }

    @Test
    void testPerformance_LargeDataset() {
        createTestData(1000);

        long startTime = System.currentTimeMillis();

        AnalyticsRequest request = AnalyticsRequest.builder()
                .preset(AnalyticsRequest.DatePreset.LAST_30)
                .build();

        AnalyticsResponseDTO response = analyticsService.getAnalytics(bankId, request);

        long duration = System.currentTimeMillis() - startTime;

        assertNotNull(response);
        assertTrue(duration < 2000, "Analytics should respond in less than 2 seconds for 1000 records, took: " + duration + "ms");
    }

    private void createTestData(int count) {
        String[] loanTypes = {"PERSONAL", "HOME", "AUTO", "BUSINESS"};
        BigDecimal[] amounts = {
                new BigDecimal("5000"), 
                new BigDecimal("15000"), 
                new BigDecimal("30000"), 
                new BigDecimal("75000"),
                new BigDecimal("150000")
        };

        for (int i = 0; i < count; i++) {
            // Create User first to satisfy foreign key constraint
            User borrower = new User();
            borrower.setEmail("borrower" + i + "@test.com");
            borrower.setPasswordHash("hashedpassword");
            borrower.setRole(UserRole.BORROWER);
            borrower.setFirstName("Test");
            borrower.setLastName("Borrower" + i);
            borrower = userRepository.save(borrower);

            Application application = new Application();
            application.setId(UUID.randomUUID());
            application.setBorrowerId(borrower.getId());
            application.setLoanType(loanTypes[i % loanTypes.length]);
            application.setLoanAmount(amounts[i % amounts.length]);
            application.setLoanTermMonths(60);
            application.setCurrency("USD");
            application.setStatus(ApplicationStatus.SUBMITTED);
            application.setCreatedAt(LocalDateTime.now().minusDays(i % 30));
            application = applicationRepository.save(application);

            if (i % 2 == 0) {
                Offer offer = new Offer();
                offer.setId(UUID.randomUUID());
                offer.setApplicationId(application.getId());
                offer.setBankId(bankId);
                offer.setApr(new BigDecimal("8.5").add(new BigDecimal(i % 5)));
                offer.setMonthlyPayment(new BigDecimal("500"));
                offer.setTotalCost(application.getLoanAmount().multiply(new BigDecimal("1.1")));
                offer.setOriginationFee(new BigDecimal("100"));
                offer.setProcessingTimeDays(5);
                offer.setValidityPeriodDays(30);
                offer.setOfferStatus(OfferStatus.SUBMITTED);
                offer.setOfferSubmittedAt(application.getCreatedAt().plusHours(2));
                offer.setExpiresAt(offer.getOfferSubmittedAt().plusDays(30));
                
                if (i % 3 == 0) {
                    offer.setBorrowerSelectedAt(offer.getOfferSubmittedAt().plusDays(1));
                }
                
                offerRepository.save(offer);
            }
        }
    }
}
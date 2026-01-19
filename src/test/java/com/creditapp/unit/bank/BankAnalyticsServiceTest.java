package com.creditapp.unit.bank;

import com.creditapp.bank.dto.*;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankAnalyticsService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAnalyticsServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private BankAnalyticsService analyticsService;

    private UUID bankId;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        dateFrom = LocalDate.now().minusDays(30);
        dateTo = LocalDate.now();
    }

    @Test
    void testCalculateMetrics_ConversionRate() {
        List<Application> applications = createMockApplications(10);
        List<Offer> offers = createMockOffers(7, applications);

        when(applicationRepository.findByCreatedAtBetween(any(), any())).thenReturn(applications);
        when(offerRepository.findByBankIdAndOfferSubmittedAtBetween(any(), any(), any())).thenReturn(offers);

        AnalyticsRequest request = AnalyticsRequest.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .preset(AnalyticsRequest.DatePreset.CUSTOM)
                .build();

        AnalyticsResponseDTO response = analyticsService.getAnalytics(bankId, request);

        assertNotNull(response);
        assertNotNull(response.getMetrics());
        assertEquals(10L, response.getMetrics().getApplicationsReceived());
        assertEquals(7L, response.getMetrics().getOffersSubmitted());
        assertEquals(new BigDecimal("70.00"), response.getMetrics().getConversionRate());
    }

    @Test
    void testCalculateMetrics_AverageAPR() {
        List<Application> applications = createMockApplications(5);
        List<Offer> offers = Arrays.asList(
                createOffer(applications.get(0), new BigDecimal("8.5")),
                createOffer(applications.get(1), new BigDecimal("9.0")),
                createOffer(applications.get(2), new BigDecimal("7.5"))
        );

        when(applicationRepository.findByCreatedAtBetween(any(), any())).thenReturn(applications);
        when(offerRepository.findByBankIdAndOfferSubmittedAtBetween(any(), any(), any())).thenReturn(offers);

        AnalyticsRequest request = AnalyticsRequest.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .preset(AnalyticsRequest.DatePreset.CUSTOM)
                .build();

        AnalyticsResponseDTO response = analyticsService.getAnalytics(bankId, request);

        assertNotNull(response.getMetrics());
        assertEquals(new BigDecimal("8.33"), response.getMetrics().getAvgAPR());
    }

    @Test
    void testLoanTypeBreakdown() {
        List<Application> applications = Arrays.asList(
                createApplication("PERSONAL"),
                createApplication("PERSONAL"),
                createApplication("HOME"),
                createApplication("AUTO")
        );
        
        List<Offer> offers = createMockOffers(4, applications);
        offers.get(0).setBorrowerSelectedAt(LocalDateTime.now());
        offers.get(1).setBorrowerSelectedAt(LocalDateTime.now());

        when(offerRepository.findByBankIdAndOfferSubmittedAtBetween(any(), any(), any())).thenReturn(offers);
        when(applicationRepository.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return applications.stream().filter(a -> a.getId().equals(id)).findFirst();
        });

        Map<String, LoanTypeBreakdownDTO> breakdown = analyticsService.getAcceptanceByLoanType(bankId, dateFrom, dateTo);

        assertNotNull(breakdown);
        assertTrue(breakdown.containsKey("PERSONAL"));
        assertEquals(2L, breakdown.get("PERSONAL").getCount());
        assertEquals(2L, breakdown.get("PERSONAL").getAccepted());
        assertEquals(new BigDecimal("100.00"), breakdown.get("PERSONAL").getAcceptanceRate());
    }

    @Test
    void testAmountRangeBreakdown() {
        List<Application> applications = Arrays.asList(
                createApplicationWithAmount(new BigDecimal("5000")),
                createApplicationWithAmount(new BigDecimal("15000")),
                createApplicationWithAmount(new BigDecimal("30000")),
                createApplicationWithAmount(new BigDecimal("75000")),
                createApplicationWithAmount(new BigDecimal("150000"))
        );
        
        List<Offer> offers = createMockOffers(5, applications);

        when(offerRepository.findByBankIdAndOfferSubmittedAtBetween(any(), any(), any())).thenReturn(offers);
        when(applicationRepository.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return applications.stream().filter(a -> a.getId().equals(id)).findFirst();
        });

        Map<String, AmountRangeBreakdownDTO> breakdown = analyticsService.getAcceptanceByAmountRange(bankId, dateFrom, dateTo);

        assertNotNull(breakdown);
        assertTrue(breakdown.containsKey("0-10k"));
        assertTrue(breakdown.containsKey("10k-25k"));
        assertTrue(breakdown.containsKey("25k-50k"));
        assertTrue(breakdown.containsKey("50k-100k"));
        assertTrue(breakdown.containsKey("100k+"));
        assertEquals(1L, breakdown.get("0-10k").getCount());
        assertEquals(1L, breakdown.get("10k-25k").getCount());
    }

    @Test
    void testTrendDataDaily() {
        LocalDate start = LocalDate.now().minusDays(6);
        LocalDate end = LocalDate.now();
        
        List<Application> applications = createMockApplications(14);
        List<Offer> offers = createMockOffers(10, applications);

        when(applicationRepository.findByCreatedAtBetween(any(), any())).thenReturn(applications);
        when(offerRepository.findByBankIdAndOfferSubmittedAtBetween(any(), any(), any())).thenReturn(offers);

        List<AnalyticsTrendDTO> trends = analyticsService.getTrendData(bankId, start, end, "DAILY");

        assertNotNull(trends);
        assertEquals(7, trends.size());
        assertNotNull(trends.get(0).getDate());
        assertNotNull(trends.get(0).getApplicationsCount());
        assertNotNull(trends.get(0).getOffersCount());
    }

    @Test
    void testPresetDateRanges() {
        AnalyticsRequest todayRequest = AnalyticsRequest.builder()
                .preset(AnalyticsRequest.DatePreset.TODAY)
                .build();
        
        AnalyticsRequest last7Request = AnalyticsRequest.builder()
                .preset(AnalyticsRequest.DatePreset.LAST_7)
                .build();

        when(applicationRepository.findByCreatedAtBetween(any(), any())).thenReturn(Collections.emptyList());
        when(offerRepository.findByBankIdAndOfferSubmittedAtBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        AnalyticsResponseDTO todayResponse = analyticsService.getAnalytics(bankId, todayRequest);
        AnalyticsResponseDTO last7Response = analyticsService.getAnalytics(bankId, last7Request);

        assertNotNull(todayResponse);
        assertNotNull(last7Response);
        assertEquals(LocalDate.now(), todayResponse.getReportGeneratedAt());
    }

    private List<Application> createMockApplications(int count) {
        List<Application> applications = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Application app = new Application();
            app.setId(UUID.randomUUID());
            app.setBorrowerId(UUID.randomUUID());
            app.setLoanType("PERSONAL");
            app.setLoanAmount(new BigDecimal("10000"));
            app.setCreatedAt(LocalDateTime.now().minusDays(i));
            applications.add(app);
        }
        return applications;
    }

    private List<Offer> createMockOffers(int count, List<Application> applications) {
        List<Offer> offers = new ArrayList<>();
        for (int i = 0; i < count && i < applications.size(); i++) {
            offers.add(createOffer(applications.get(i), new BigDecimal("8.5")));
        }
        return offers;
    }

    private Offer createOffer(Application app, BigDecimal apr) {
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(app.getId());
        offer.setBankId(bankId);
        offer.setApr(apr);
        offer.setOfferSubmittedAt(app.getCreatedAt().plusHours(2));
        return offer;
    }

    private Application createApplication(String loanType) {
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setBorrowerId(UUID.randomUUID());
        app.setLoanType(loanType);
        app.setLoanAmount(new BigDecimal("10000"));
        app.setCreatedAt(LocalDateTime.now().minusDays(1));
        return app;
    }

    private Application createApplicationWithAmount(BigDecimal amount) {
        Application app = createApplication("PERSONAL");
        app.setLoanAmount(amount);
        return app;
    }
}
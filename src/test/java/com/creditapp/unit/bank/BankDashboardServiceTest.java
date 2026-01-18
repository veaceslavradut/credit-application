package com.creditapp.unit.bank;

import com.creditapp.bank.dto.BankDashboardMetrics;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankDashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankDashboardServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private BankDashboardService dashboardService;

    @Test
    void testGetDashboardMetrics_EmptyData_ReturnsZeroMetrics() {
        // Arrange
        UUID bankId = UUID.randomUUID();
        when(offerRepository.findByBankIdAndCreatedAtAfter(eq(bankId), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // Act
        BankDashboardMetrics metrics = dashboardService.getDashboardMetrics(bankId, "TODAY");

        // Assert
        assertThat(metrics.applicationsReceivedToday()).isEqualTo(0);
        assertThat(metrics.applicationsReceivedAll()).isEqualTo(0);
        assertThat(metrics.offersSubmitted()).isEqualTo(0);
        assertThat(metrics.offersAccepted()).isEqualTo(0);
        assertThat(metrics.conversionRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.averageTimeToOfferDays()).isEqualTo(0);
        assertThat(metrics.lastUpdated()).isNotNull();
    }

    @Test
    void testGetDashboardMetrics_WithData_CalculatesCorrectly() {
        // Arrange
        UUID bankId = UUID.randomUUID();
        UUID app1 = UUID.randomUUID();
        UUID app2 = UUID.randomUUID();
        UUID app3 = UUID.randomUUID();

        LocalDateTime base = LocalDateTime.now().minusDays(2);

        List<Offer> offers = List.of(
            createOffer(app1, OfferStatus.SUBMITTED, base, base.plusHours(1)),
            createOffer(app1, OfferStatus.ACCEPTED, base, base.plusHours(2)),
            createOffer(app2, OfferStatus.SUBMITTED, base.minusDays(1), base.minusDays(1).plusDays(1)),
            createOffer(app3, OfferStatus.REJECTED, base.minusDays(1), base.minusDays(1).plusDays(1))
        );

        when(offerRepository.findByBankIdAndCreatedAtAfter(eq(bankId), any(LocalDateTime.class)))
            .thenReturn(offers);

        // Act
        BankDashboardMetrics metrics = dashboardService.getDashboardMetrics(bankId, "LAST_7_DAYS");

        // Assert
        assertThat(metrics.applicationsReceivedAll()).isEqualTo(3); // 3 unique applications
        assertThat(metrics.offersSubmitted()).isEqualTo(4); // All 4 offers (SUBMITTED, ACCEPTED, REJECTED are counted)
        assertThat(metrics.offersAccepted()).isEqualTo(1); // Only 1 ACCEPTED
        assertThat(metrics.conversionRate()).isGreaterThan(BigDecimal.ZERO);
        assertThat(metrics.conversionRate()).isLessThanOrEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void testGetDashboardMetrics_ConversionRate_CalculatesCorrectly() {
        // Arrange
        UUID bankId = UUID.randomUUID();
        UUID app1 = UUID.randomUUID();
        UUID app2 = UUID.randomUUID();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        List<Offer> offers = List.of(
            createOffer(app1, OfferStatus.SUBMITTED, yesterday, yesterday.plusHours(1)),
            createOffer(app1, OfferStatus.ACCEPTED, yesterday, yesterday.plusHours(2)),
            createOffer(app2, OfferStatus.SUBMITTED, yesterday, yesterday.plusDays(1)),
            createOffer(app2, OfferStatus.REJECTED, yesterday, yesterday.plusDays(1))
        );

        when(offerRepository.findByBankIdAndCreatedAtAfter(eq(bankId), any(LocalDateTime.class)))
            .thenReturn(offers);

        // Act
        BankDashboardMetrics metrics = dashboardService.getDashboardMetrics(bankId, "TODAY");

        // Assert
        // 1 accepted out of 4 submitted = 25%
        assertThat(metrics.conversionRate()).isEqualByComparingTo(BigDecimal.valueOf(25.00));
    }

    @Test
    void testGetDashboardMetrics_AverageTimeToOffer_CalculatesCorrectly() {
        // Arrange
        UUID bankId = UUID.randomUUID();
        UUID app1 = UUID.randomUUID();
        UUID app2 = UUID.randomUUID();

        LocalDateTime base = LocalDateTime.now().minusDays(10);

        List<Offer> offers = List.of(
            createOffer(app1, OfferStatus.SUBMITTED, base, base.plusDays(1)),      // 1 day
            createOffer(app2, OfferStatus.SUBMITTED, base, base.plusDays(3))       // 3 days
        );

        when(offerRepository.findByBankIdAndCreatedAtAfter(eq(bankId), any(LocalDateTime.class)))
            .thenReturn(offers);

        // Act
        BankDashboardMetrics metrics = dashboardService.getDashboardMetrics(bankId, "LAST_30_DAYS");

        // Assert
        // Average of 1 and 3 days = 2 days
        assertThat(metrics.averageTimeToOfferDays()).isEqualTo(2);
    }

    @Test
    void testGetDashboardMetrics_ResponseTime_LessThan500ms() {
        // Arrange
        UUID bankId = UUID.randomUUID();
        List<Offer> offers = createLargeOfferList(100);  // 100 offers
        
        when(offerRepository.findByBankIdAndCreatedAtAfter(eq(bankId), any(LocalDateTime.class)))
            .thenReturn(offers);

        // Act
        long startTime = System.currentTimeMillis();
        BankDashboardMetrics metrics = dashboardService.getDashboardMetrics(bankId, "TODAY");
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertThat(duration).isLessThan(500);  // Should complete in <500ms
        assertThat(metrics).isNotNull();
    }

    private Offer createOffer(UUID applicationId, OfferStatus status, LocalDateTime createdAt, LocalDateTime submittedAt) {
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(applicationId);
        offer.setBankId(UUID.randomUUID());
        offer.setOfferStatus(status);
        offer.setApr(BigDecimal.valueOf(8.5));
        offer.setMonthlyPayment(BigDecimal.valueOf(500));
        offer.setTotalCost(BigDecimal.valueOf(30000));
        offer.setOriginationFee(BigDecimal.valueOf(1000));
        offer.setProcessingTimeDays(5);
        offer.setValidityPeriodDays(30);
        offer.setExpiresAt(LocalDateTime.now().plusDays(30));
        // Use reflection to set createdAt since it's @CreationTimestamp
        try {
            var field = Offer.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(offer, createdAt);
        } catch (Exception e) {
            // Ignore
        }
        offer.setOfferSubmittedAt(submittedAt);
        return offer;
    }

    private List<Offer> createLargeOfferList(int count) {
        List<Offer> offers = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().minusDays(7);
        
        for (int i = 0; i < count; i++) {
            UUID appId = UUID.randomUUID();
            OfferStatus status = i % 3 == 0 ? OfferStatus.ACCEPTED : OfferStatus.SUBMITTED;
            offers.add(createOffer(appId, status, base.plusHours(i), base.plusHours(i).plusDays(1)));
        }
        
        return offers;
    }
}
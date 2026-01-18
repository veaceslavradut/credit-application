package com.creditapp.unit.borrower.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.OfferInsightsDTO;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.OfferInsightsService;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferInsightsServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OfferInsightsService offerInsightsService;

    private UUID applicationId;
    private UUID borrowerId;
    private Application application;
    private List<Offer> offers;
    private Map<UUID, Organization> bankMap;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();

        application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);

        // Create 3 offers from different banks
        UUID bank1Id = UUID.randomUUID();
        UUID bank2Id = UUID.randomUUID();
        UUID bank3Id = UUID.randomUUID();

        Offer offer1 = createOffer(UUID.randomUUID(), applicationId, bank1Id,
                new BigDecimal("5.5"), new BigDecimal("450.00"), new BigDecimal("52000"), 7);

        Offer offer2 = createOffer(UUID.randomUUID(), applicationId, bank2Id,
                new BigDecimal("6.0"), new BigDecimal("420.00"), new BigDecimal("53000"), 5);

        Offer offer3 = createOffer(UUID.randomUUID(), applicationId, bank3Id,
                new BigDecimal("5.8"), new BigDecimal("440.00"), new BigDecimal("51000"), 10);

        offers = Arrays.asList(offer1, offer2, offer3);

        // Create bank organizations
        Organization bank1 = new Organization();
        bank1.setId(bank1Id);
        bank1.setName("Bank A");

        Organization bank2 = new Organization();
        bank2.setId(bank2Id);
        bank2.setName("Bank B");

        Organization bank3 = new Organization();
        bank3.setId(bank3Id);
        bank3.setName("Bank C");

        bankMap = Map.of(bank1Id, bank1, bank2Id, bank2, bank3Id, bank3);
    }

    private Offer createOffer(UUID id, UUID appId, UUID bankId, BigDecimal apr, 
                               BigDecimal monthlyPayment, BigDecimal totalCost, int processingDays) {
        Offer offer = new Offer();
        offer.setId(id);
        offer.setApplicationId(appId);
        offer.setBankId(bankId);
        offer.setApr(apr);
        offer.setMonthlyPayment(monthlyPayment);
        offer.setTotalCost(totalCost);
        offer.setProcessingTimeDays(processingDays);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        return offer;
    }

    @Test
    void calculateInsights_Success() {
        // Arrange
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(offers);
        when(organizationRepository.findAllById(any())).thenReturn(new ArrayList<>(bankMap.values()));

        // Act
        OfferInsightsDTO result = offerInsightsService.calculateInsights(applicationId, borrowerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.bestAprOffer().apr()).isEqualByComparingTo(new BigDecimal("5.5"));
        assertThat(result.lowestMonthlyPaymentOffer().monthlyPayment()).isEqualByComparingTo(new BigDecimal("420.00"));
        assertThat(result.lowestTotalCostOffer().totalCost()).isEqualByComparingTo(new BigDecimal("51000"));
        assertThat(result.averageApr()).isEqualByComparingTo(new BigDecimal("5.77"));
        assertThat(result.aprSpread()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(result.recommendedOfferId()).isNotNull();
        assertThat(result.savingsAnalysis()).isNotNull();
        assertThat(result.savingsAnalysis().comparedToWorstOffer()).isEqualByComparingTo(new BigDecimal("2000"));

        verify(applicationRepository).findById(applicationId);
        verify(offerRepository).findByApplicationId(applicationId);
    }

    @Test
    void calculateInsights_ApplicationNotFound() {
        // Arrange
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> offerInsightsService.calculateInsights(applicationId, borrowerId))
                .isInstanceOf(ApplicationNotFoundException.class)
                .hasMessageContaining("Application not found");

        verify(applicationRepository).findById(applicationId);
        verifyNoInteractions(offerRepository);
    }

    @Test
    void calculateInsights_UnauthorizedBorrower() {
        // Arrange
        UUID otherBorrowerId = UUID.randomUUID();
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // Act & Assert
        assertThatThrownBy(() -> offerInsightsService.calculateInsights(applicationId, otherBorrowerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot access another borrower");

        verify(applicationRepository).findById(applicationId);
        verifyNoInteractions(offerRepository);
    }

    @Test
    void calculateInsights_LessThanTwoOffers_ReturnsNull() {
        // Arrange
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(Collections.singletonList(offers.get(0)));

        // Act
        OfferInsightsDTO result = offerInsightsService.calculateInsights(applicationId, borrowerId);

        // Assert
        assertThat(result).isNull();

        verify(applicationRepository).findById(applicationId);
        verify(offerRepository).findByApplicationId(applicationId);
    }

    @Test
    void calculateRecommendedOffer_ReturnsHighestScore() {
        // Arrange - offer3 has lowest total cost
        List<Offer> testOffers = Arrays.asList(offers.get(0), offers.get(2));

        // Act
        UUID recommendedId = offerInsightsService.calculateRecommendedOffer(testOffers);

        // Assert
        assertThat(recommendedId).isNotNull();
    }

    @Test
    void calculateSavings_CorrectDifference() {
        // Arrange
        Map<UUID, String> bankNames = Map.of(
                offers.get(0).getBankId(), "Bank A",
                offers.get(1).getBankId(), "Bank B",
                offers.get(2).getBankId(), "Bank C"
        );

        // Act
        var savings = offerInsightsService.calculateSavings(offers, bankNames);

        // Assert
        assertThat(savings).isNotNull();
        assertThat(savings.bestOfferId()).isEqualTo(offers.get(2).getId());
        assertThat(savings.comparedToWorstOffer()).isEqualByComparingTo(new BigDecimal("2000"));
        assertThat(savings.savingsMessage()).contains("Bank C");
    }

    @Test
    void calculateInsights_IdenticalOffers() {
        // Arrange
        UUID bank1Id = UUID.randomUUID();
        Offer offer1 = createOffer(UUID.randomUUID(), applicationId, bank1Id,
                new BigDecimal("6.0"), new BigDecimal("500.00"), new BigDecimal("60000"), 7);
        Offer offer2 = createOffer(UUID.randomUUID(), applicationId, bank1Id,
                new BigDecimal("6.0"), new BigDecimal("500.00"), new BigDecimal("60000"), 7);

        Organization bank1 = new Organization();
        bank1.setId(bank1Id);
        bank1.setName("Bank A");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(applicationId)).thenReturn(Arrays.asList(offer1, offer2));
        when(organizationRepository.findAllById(any())).thenReturn(Collections.singletonList(bank1));

        // Act
        OfferInsightsDTO result = offerInsightsService.calculateInsights(applicationId, borrowerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.aprSpread()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.savingsAnalysis().comparedToWorstOffer()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
package com.creditapp.unit.bank;

import com.creditapp.bank.dto.MarketAnalysisDTO;
import com.creditapp.bank.dto.MarketAverageDTO;
import com.creditapp.bank.dto.MyBankRateCardDTO;
import com.creditapp.bank.dto.CompetitivePosition;
import com.creditapp.bank.exception.InsufficientMarketDataException;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.bank.service.BankMarketAnalysisService;
import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankMarketAnalysisServiceTest {

    @Mock
    private BankRateCardRepository rateCardRepository;

    @InjectMocks
    private BankMarketAnalysisService service;

    private UUID bankA;
    private UUID bankB;
    private UUID bankC;
    private UUID bankD;
    
    @BeforeEach
    void init() {
        bankA = UUID.randomUUID();
        bankB = UUID.randomUUID();
        bankC = UUID.randomUUID();
        bankD = UUID.randomUUID();
    }

    private BankRateCard card(UUID bankId, LoanType type, Currency currency,
                               double apr, double origFee, double insurance, int days) {
        BankRateCard c = new BankRateCard();
        c.setId(UUID.randomUUID());
        c.setBankId(bankId);
        c.setLoanType(type);
        c.setCurrency(currency);
        c.setBaseApr(BigDecimal.valueOf(apr));
        c.setOriginationFeePercent(BigDecimal.valueOf(origFee));
        c.setInsurancePercent(BigDecimal.valueOf(insurance));
        c.setProcessingTimeDays(days);
        c.setValidFrom(LocalDateTime.now().minusDays(10));
        c.setValidTo(null); // active
        return c;
    }

    @Test
    void calculatePercentileRanking_BoundsAndOrdering() {
        List<BigDecimal> aprs = Arrays.asList(
                BigDecimal.valueOf(7.5),
                BigDecimal.valueOf(8.0),
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(11.0)
        );

        assertEquals(100, service.calculatePercentileRanking(BigDecimal.valueOf(7.5), aprs));
        assertEquals(20, service.calculatePercentileRanking(BigDecimal.valueOf(11.0), aprs));
        int mid = service.calculatePercentileRanking(BigDecimal.valueOf(8.5), aprs);
        assertTrue(mid >= 40 && mid <= 80);
    }

    @Test
    void calculateMarketAverage_ComputesStatsAndUniqueBankCount() {
        LoanType type = LoanType.HOME;
        Currency curr = Currency.USD;

        List<BankRateCard> active = Arrays.asList(
                card(bankA, type, curr, 7.5, 1.0, 0.5, 5),
                card(bankB, type, curr, 8.0, 1.2, 0.4, 6),
                card(bankC, type, curr, 9.0, 0.8, 0.3, 4)
        );

        when(rateCardRepository.findByValidToIsNull()).thenReturn(active);

        MarketAverageDTO avg = service.calculateMarketAverage(type, curr);
        assertNotNull(avg);
        assertEquals(type, avg.loanType());
        assertEquals(curr, avg.currency());
        assertEquals(BigDecimal.valueOf(8.17).setScale(2), avg.averageApr()); // (7.5+8.0+9.0)/3 = 8.1667 → 8.17
        assertEquals(0, avg.medianApr().compareTo(BigDecimal.valueOf(8.0)));
        assertEquals(BigDecimal.valueOf(7.5).setScale(2), avg.minApr().setScale(2));
        assertEquals(BigDecimal.valueOf(9.0).setScale(2), avg.maxApr().setScale(2));
        assertEquals(BigDecimal.valueOf(1.00).setScale(2), avg.averageOriginationFee().setScale(2)); // (1.0+1.2+0.8)/3 = 1.0
        assertEquals(BigDecimal.valueOf(0.40).setScale(2), avg.averageInsuranceCost().setScale(2)); // (0.5+0.4+0.3)/3 = 0.4
        assertEquals(5, avg.averageProcessingTime()); // (5+6+4)/3 → 5
        assertEquals(3, avg.bankCount());
    }

    @Test
    void analyzeMarket_PrivacyEnforced_ThrowsWhenLessThanThreeBanks() {
        LoanType type = LoanType.HOME;
        Currency curr = Currency.USD;
        UUID myBank = bankA;

        // My bank has one active card
        List<BankRateCard> myCards = Collections.singletonList(card(myBank, type, curr, 7.5, 1.0, 0.5, 5));
        when(rateCardRepository.findByBankIdAndValidToIsNull(myBank)).thenReturn(myCards);

        // Market has only two unique banks
        List<BankRateCard> market = Arrays.asList(
                card(bankA, type, curr, 7.5, 1.0, 0.5, 5),
                card(bankB, type, curr, 8.0, 1.2, 0.4, 6)
        );
        when(rateCardRepository.findByValidToIsNull()).thenReturn(market);

        assertThrows(InsufficientMarketDataException.class, () -> service.analyzeMarket(myBank));
    }

    @Test
    void analyzeMarket_Success_ComputesPercentilesAndOverallPosition() {
        LoanType type = LoanType.HOME;
        Currency curr = Currency.USD;
        UUID myBank = bankA;

        // My bank has two active cards (HOME USD and AUTO EUR)
        List<BankRateCard> myCards = Arrays.asList(
                card(myBank, type, curr, 7.5, 1.0, 0.5, 5),
                card(myBank, LoanType.AUTO, Currency.EUR, 6.0, 0.9, 0.2, 7)
        );
        when(rateCardRepository.findByBankIdAndValidToIsNull(myBank)).thenReturn(myCards);

        // Market active cards include at least 3 banks for each subset
        List<BankRateCard> market = Arrays.asList(
                // HOME USD
                card(bankA, type, curr, 7.5, 1.0, 0.5, 5),
                card(bankB, type, curr, 8.0, 1.2, 0.4, 6),
                card(bankC, type, curr, 9.0, 0.8, 0.3, 4),
                card(bankD, type, curr, 11.0, 1.1, 0.6, 6),
                // AUTO EUR
                card(bankA, LoanType.AUTO, Currency.EUR, 6.0, 0.9, 0.2, 7),
                card(bankB, LoanType.AUTO, Currency.EUR, 6.5, 1.0, 0.3, 8),
                card(bankC, LoanType.AUTO, Currency.EUR, 7.5, 1.1, 0.4, 9)
        );
        when(rateCardRepository.findByValidToIsNull()).thenReturn(market);

        MarketAnalysisDTO dto = service.analyzeMarket(myBank);
        assertNotNull(dto);
        assertEquals(2, dto.myBankRates().size());
        assertEquals(2, dto.marketAverageRates().size());
        assertTrue(dto.bankCount() >= 3);

        // Validate HOME USD percentile: my APR 7.5 should be top percentile
        Optional<MyBankRateCardDTO> homeUsd = dto.myBankRates().stream()
                .filter(r -> r.loanType() == LoanType.HOME && r.currency() == Currency.USD)
                .findFirst();
        assertTrue(homeUsd.isPresent());
        assertEquals(100, homeUsd.get().marketPercentileRanking());
        assertEquals(CompetitivePosition.MORE_COMPETITIVE, homeUsd.get().competitivePosition());

        // Overall position should be at least AVERAGE or MORE_COMPETITIVE
        assertTrue(dto.overallCompetitivePosition().equals(CompetitivePosition.MORE_COMPETITIVE.name())
                || dto.overallCompetitivePosition().equals(CompetitivePosition.AVERAGE.name()));
    }
}

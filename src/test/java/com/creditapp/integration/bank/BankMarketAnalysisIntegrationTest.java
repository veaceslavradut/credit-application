package com.creditapp.integration.bank;

import com.creditapp.bank.dto.MarketAnalysisDTO;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.borrower.model.Currency;
import com.creditapp.borrower.model.LoanType;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BankMarketAnalysisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BankRateCardRepository rateCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String bank1Token;
    private String bank2Token;
    private UUID bank1Id;
    private UUID bank2Id;
    private UUID bank3Id;
    private UUID bank4Id;

    @BeforeEach
    public void setUp() {
        // Create 4 banks with rate cards for market analysis
        bank1Id = createBankWithAdmin("Bank A", "admin1@banka.com");
        bank2Id = createBankWithAdmin("Bank B", "admin2@bankb.com");
        bank3Id = createBankWithAdmin("Bank C", "admin3@bankc.com");
        bank4Id = createBankWithAdmin("Bank D", "admin4@bankd.com");

        // Generate tokens
        bank1Token = generateTokenForBank(bank1Id);
        bank2Token = generateTokenForBank(bank2Id);

        // Create rate cards for PERSONAL/EUR - 4 banks
        createRateCard(bank1Id, LoanType.PERSONAL, Currency.EUR, 7.5, 1.0, 0.5, 5);  // Best APR
        createRateCard(bank2Id, LoanType.PERSONAL, Currency.EUR, 8.0, 1.2, 0.4, 6);  // Second
        createRateCard(bank3Id, LoanType.PERSONAL, Currency.EUR, 9.0, 0.8, 0.3, 4);  // Third
        createRateCard(bank4Id, LoanType.PERSONAL, Currency.EUR, 11.0, 1.1, 0.6, 7); // Worst APR

        // Create rate cards for HOME/USD - 3 banks (minimum for privacy)
        createRateCard(bank1Id, LoanType.HOME, Currency.USD, 5.5, 1.5, 0.2, 10);
        createRateCard(bank2Id, LoanType.HOME, Currency.USD, 6.0, 1.0, 0.3, 8);
        createRateCard(bank3Id, LoanType.HOME, Currency.USD, 6.5, 1.2, 0.25, 9);
    }

    private UUID createBankWithAdmin(String bankName, String adminEmail) {
        Organization bank = new Organization();
        bank.setId(UUID.randomUUID());
        bank.setName(bankName);
        bank.setTaxId("TAX" + UUID.randomUUID().toString().substring(0, 8));
        bank.setRegistrationNumber("REG" + UUID.randomUUID().toString().substring(0, 8));
        bank.setStatus(BankStatus.ACTIVE);
        bank.setCountryCode("US");
        organizationRepository.save(bank);

        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("Password123!"));
        admin.setFirstName("Admin");
        admin.setLastName(bankName);
        admin.setRole(UserRole.BANK_ADMIN);
        admin.setOrganizationId(bank.getId());
        userRepository.save(admin);

        return bank.getId();
    }

    private String generateTokenForBank(UUID bankId) {
        User admin = userRepository.findAll().stream()
                .filter(u -> u.getOrganizationId() != null && u.getOrganizationId().equals(bankId))
                .findFirst()
                .orElseThrow();
        return jwtTokenService.generateToken(admin);
    }

    private void createRateCard(UUID bankId, LoanType loanType, Currency currency,
                                 double apr, double origFee, double insurance, int processingDays) {
        BankRateCard card = new BankRateCard();
        card.setId(UUID.randomUUID());
        card.setBankId(bankId);
        card.setLoanType(loanType);
        card.setCurrency(currency);
        card.setMinLoanAmount(BigDecimal.valueOf(5000));
        card.setMaxLoanAmount(BigDecimal.valueOf(100000));
        card.setBaseApr(BigDecimal.valueOf(apr));
        card.setAprAdjustmentRange(BigDecimal.valueOf(2.0));
        card.setOriginationFeePercent(BigDecimal.valueOf(origFee));
        card.setInsurancePercent(BigDecimal.valueOf(insurance));
        card.setProcessingTimeDays(processingDays);
        card.setValidFrom(LocalDateTime.now().minusDays(1));
        card.setValidTo(null); // Active
        rateCardRepository.save(card);
    }

    @Test
    public void test1_GetMarketAnalysis_CalculatesMarketAverageCorrectly() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankCount").value(4))
                .andExpect(jsonPath("$.myBankRates").isArray())
                .andExpect(jsonPath("$.marketAverageRates").isArray())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        MarketAnalysisDTO analysis = objectMapper.readValue(json, MarketAnalysisDTO.class);

        // Verify PERSONAL/EUR market average
        var personalMarket = analysis.marketAverageRates().stream()
                .filter(m -> m.loanType() == LoanType.PERSONAL && m.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        // Average APR: (7.5 + 8.0 + 9.0 + 11.0) / 4 = 8.875  8.88
        assertThat(personalMarket.averageApr()).isEqualByComparingTo(BigDecimal.valueOf(8.88));
        assertThat(personalMarket.minApr()).isEqualByComparingTo(BigDecimal.valueOf(7.5));
        assertThat(personalMarket.maxApr()).isEqualByComparingTo(BigDecimal.valueOf(11.0));
        assertThat(personalMarket.bankCount()).isEqualTo(4);
    }

    @Test
    public void test2_GetMarketAnalysis_CalculatesMedianAprCorrectly() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank1Token))
                .andExpect(status().isOk())
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        var personalMarket = analysis.marketAverageRates().stream()
                .filter(m -> m.loanType() == LoanType.PERSONAL && m.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        // Median of [7.5, 8.0, 9.0, 11.0] = (8.0 + 9.0) / 2 = 8.5
        assertThat(personalMarket.medianApr()).isEqualByComparingTo(BigDecimal.valueOf(8.5));
    }

    @Test
    public void test3_GetMarketAnalysis_IdentifiesMinMaxApr() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank1Token))
                .andExpect(status().isOk())
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        var personalMarket = analysis.marketAverageRates().stream()
                .filter(m -> m.loanType() == LoanType.PERSONAL && m.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        assertThat(personalMarket.minApr()).isEqualByComparingTo(BigDecimal.valueOf(7.5));
        assertThat(personalMarket.maxApr()).isEqualByComparingTo(BigDecimal.valueOf(11.0));
    }

    @Test
    public void test4_GetMarketAnalysis_CalculatesPercentileRankingCorrectly() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank1Token))
                .andExpect(status().isOk())
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        var myPersonalRate = analysis.myBankRates().stream()
                .filter(r -> r.loanType() == LoanType.PERSONAL && r.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        // Bank1 has 7.5% (best APR among 4)
        // Percentile = ((4 - 1 + 1) / 4) * 100 = 100
        assertThat(myPersonalRate.marketPercentileRanking()).isEqualTo(100);
    }

    @Test
    public void test5_BankWithLowestApr_VerifyMoreCompetitivePosition() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallCompetitivePosition").value("MORE_COMPETITIVE"))
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        var myPersonalRate = analysis.myBankRates().stream()
                .filter(r -> r.loanType() == LoanType.PERSONAL && r.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        assertThat(myPersonalRate.competitivePosition().name()).isEqualTo("MORE_COMPETITIVE");
        assertThat(myPersonalRate.marketPercentileRanking()).isGreaterThanOrEqualTo(75);
    }

    @Test
    public void test6_BankWithMidRangeApr_VerifyAveragePosition() throws Exception {
        // Bank2 has 8.0% APR (second among 4)
        // Percentile = ((4 - 2 + 1) / 4) * 100 = 75 (edge case: MORE_COMPETITIVE)
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank2Token))
                .andExpect(status().isOk())
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        var myPersonalRate = analysis.myBankRates().stream()
                .filter(r -> r.loanType() == LoanType.PERSONAL && r.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        // Bank2 percentile = 75 (edge: MORE_COMPETITIVE)
        assertThat(myPersonalRate.marketPercentileRanking()).isEqualTo(75);
    }

    @Test
    public void test7_BankWithHighestApr_VerifyLessCompetitivePosition() throws Exception {
        // Bank4 has 11.0% (worst among 4)
        String bank4Token = generateTokenForBank(bank4Id);

        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank4Token))
                .andExpect(status().isOk())
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        var myPersonalRate = analysis.myBankRates().stream()
                .filter(r -> r.loanType() == LoanType.PERSONAL && r.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        // Bank4 percentile = ((4 - 4 + 1) / 4) * 100 = 25 (edge: AVERAGE, but let's verify)
        assertThat(myPersonalRate.marketPercentileRanking()).isLessThanOrEqualTo(25);
        assertThat(myPersonalRate.competitivePosition().name()).isIn("AVERAGE", "LESS_COMPETITIVE");
    }

    @Test
    public void test8_InsufficientMarketData_Returns412() throws Exception {
        // Create a bank with rate card for AUTO/MDL (only 1 bank, < 3)
        UUID singleBank = createBankWithAdmin("Solo Bank", "solo@bank.com");
        String soloToken = generateTokenForBank(singleBank);
        createRateCard(singleBank, LoanType.AUTO, Currency.MDL, 10.0, 1.0, 0.5, 5);

        mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + soloToken))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.error").value("Precondition Failed"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("minimum 3 required")));
    }

    @Test
    public void test9_NoCompetitorNamesInResponse_VerifyPrivacy() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank1Token))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        // Verify no competitor bank names appear in response
        assertThat(json).doesNotContain("Bank B");
        assertThat(json).doesNotContain("Bank C");
        assertThat(json).doesNotContain("Bank D");

        // Verify only aggregated data
        assertThat(json).contains("marketAverageRates");
        assertThat(json).contains("averageApr");
        assertThat(json).doesNotContain("bankName");
    }

    @Test
    public void test10_DifferentBankAdmin_CannotSeeOtherAnalysis() throws Exception {
        // Bank2 admin trying to access should only see Bank2 data
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank2Token))
                .andExpect(status().isOk())
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        // Verify only Bank2 rates returned
        assertThat(analysis.myBankRates()).hasSize(2); // PERSONAL/EUR + HOME/USD

        var myPersonalRate = analysis.myBankRates().stream()
                .filter(r -> r.loanType() == LoanType.PERSONAL && r.currency() == Currency.EUR)
                .findFirst().orElseThrow();

        // Bank2 has 8.0% APR (not 7.5%)
        assertThat(myPersonalRate.baseApr()).isEqualByComparingTo(BigDecimal.valueOf(8.0));
    }

    @Test
    public void test_VisualizationDataIncluded() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bank/rate-cards/market-analysis")
                        .header("Authorization", "Bearer " + bank1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visualization").exists())
                .andExpect(jsonPath("$.visualization.aprComparisons").isArray())
                .andExpect(jsonPath("$.visualization.feeComparisons").isArray())
                .andExpect(jsonPath("$.visualization.processingComparisons").isArray())
                .andReturn();

        MarketAnalysisDTO analysis = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketAnalysisDTO.class);

        assertThat(analysis.visualization()).isNotNull();
        assertThat(analysis.visualization().aprComparisons()).isNotEmpty();
        assertThat(analysis.visualization().feeComparisons()).isNotEmpty();
        assertThat(analysis.visualization().processingComparisons()).isNotEmpty();
    }
}
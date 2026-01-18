package com.creditapp.integration.bank;

import com.creditapp.bank.dto.BankRateCardRequest;
import com.creditapp.bank.dto.BankRateCardResponse;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BankRateCardIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private String bankAdminToken;
    private UUID bankId;
    private User bankAdmin;

    @BeforeEach
    public void setUp() {
        // Create a bank organization
        Organization bank = new Organization();
        bank.setId(UUID.randomUUID());
        bank.setName("Test Bank");
        bank.setTaxId("TAX" + System.currentTimeMillis());
        bank.setRegistrationNumber("TST" + System.currentTimeMillis());
        bank.setStatus(BankStatus.ACTIVE);
        organizationRepository.save(bank);
        bankId = bank.getId();

        // Create a bank admin user
        bankAdmin = new User();
        bankAdmin.setId(UUID.randomUUID());
        bankAdmin.setEmail("admin@testbank.com");
        bankAdmin.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
        bankAdmin.setFirstName("Test");
        bankAdmin.setLastName("Admin");
        bankAdmin.setRole(UserRole.BANK_ADMIN);
        bankAdmin.setOrganizationId(bankId);
        userRepository.save(bankAdmin);

        // Generate JWT token for bank admin
        bankAdminToken = jwtTokenService.generateToken(bankAdmin);
    }

    @Test
    public void testCreateValidRateCard_Returns201Created() throws Exception {
        BankRateCardRequest request = createValidRateCardRequest();

        mockMvc.perform(post("/api/bank/rate-cards")
                .header("Authorization", "Bearer " + bankAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.baseApr").value(8.5))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.validFrom").isNotEmpty())
                .andExpect(jsonPath("$.validTo").isEmpty());
    }

    @Test
    public void testCreateRateCard_APROutOfRange_Returns400BadRequest() throws Exception {
        BankRateCardRequest invalidRequest = new BankRateCardRequest(
                LoanType.PERSONAL, Currency.EUR, new BigDecimal("5000"),
                new BigDecimal("100000"), new BigDecimal("51.0"), new BigDecimal("3.0"),
                new BigDecimal("2.5"), new BigDecimal("0.5"));

        mockMvc.perform(post("/api/bank/rate-cards")
                .header("Authorization", "Bearer " + bankAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetRateCards_UnauthorizedUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/bank/rate-cards")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateRateCard_NonBankAdminRole_Returns403Forbidden() throws Exception {
        // Create a borrower user
        User borrower = new User();
        borrower.setId(UUID.randomUUID());
        borrower.setEmail("borrower@test.com");
        borrower.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
        borrower.setFirstName("Borrower");
        borrower.setLastName("User");
        borrower.setRole(UserRole.BORROWER);
        userRepository.save(borrower);

        String borrowerToken = jwtTokenService.generateToken(borrower);

        BankRateCardRequest request = createValidRateCardRequest();

        mockMvc.perform(post("/api/bank/rate-cards")
                .header("Authorization", "Bearer " + borrowerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }



    @Test
    public void testUpdateRateCard_CreatesNewVersion_Returns200OK() throws Exception {
        // Create initial rate card
        BankRateCardRequest initialRequest = createValidRateCardRequest();
        MvcResult createResult = mockMvc.perform(post("/api/bank/rate-cards")
                .header("Authorization", "Bearer " + bankAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        BankRateCardResponse initialResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BankRateCardResponse.class);
        UUID rateCardId = initialResponse.getId();

        // Update rate card
        BankRateCardRequest updateRequest = new BankRateCardRequest(
                LoanType.PERSONAL, Currency.EUR, new BigDecimal("5000"),
                new BigDecimal("100000"), new BigDecimal("8.75"), new BigDecimal("3.0"),
                new BigDecimal("2.5"), new BigDecimal("0.5"));

        MvcResult updateResult = mockMvc.perform(put("/api/bank/rate-cards/{rateCardId}", rateCardId)
                .header("Authorization", "Bearer " + bankAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseApr").value(8.75))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        BankRateCardResponse updatedResponse = objectMapper.readValue(
                updateResult.getResponse().getContentAsString(),
                BankRateCardResponse.class);

        // Verify new version has different ID
        assert !updatedResponse.getId().equals(rateCardId);
    }

    @Test
    public void testUpdateNonExistentRateCard_Returns404NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        BankRateCardRequest request = createValidRateCardRequest();

        mockMvc.perform(put("/api/bank/rate-cards/{rateCardId}", nonExistentId)
                .header("Authorization", "Bearer " + bankAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // Helper method to create a valid rate card request
    private BankRateCardRequest createValidRateCardRequest() {
        return new BankRateCardRequest(
                LoanType.PERSONAL,
                Currency.EUR,
                new BigDecimal("5000"),
                new BigDecimal("100000"),
                new BigDecimal("8.5"),
                new BigDecimal("3.0"),
                new BigDecimal("2.5"),
                new BigDecimal("0.5"));
    }
}
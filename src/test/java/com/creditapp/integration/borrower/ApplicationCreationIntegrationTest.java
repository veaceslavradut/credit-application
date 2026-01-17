package com.creditapp.integration.borrower;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.dto.CreateApplicationRequest;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.AuditLogRepository;
import com.creditapp.shared.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApplicationCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private CreateApplicationRequest validRequest;
    private User testBorrower;
    private String borrowerToken;
    private User bankAdmin;
    private String bankAdminToken;

    @BeforeEach
    void setUp() {
        // Create test borrower user
        testBorrower = new User();
        testBorrower.setId(UUID.randomUUID());
        testBorrower.setEmail("borrower_app_" + UUID.randomUUID() + "@test.example.com");
        testBorrower.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
        testBorrower.setFirstName("John");
        testBorrower.setLastName("Borrower");
        testBorrower.setRole(UserRole.BORROWER);
        testBorrower = userRepository.save(testBorrower);
        borrowerToken = jwtTokenService.generateToken(testBorrower);
        
        // Create test bank admin user
        bankAdmin = new User();
        bankAdmin.setId(UUID.randomUUID());
        bankAdmin.setEmail("bankadmin_app_" + UUID.randomUUID() + "@test.example.com");
        bankAdmin.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
        bankAdmin.setFirstName("Bank");
        bankAdmin.setLastName("Admin");
        bankAdmin.setRole(UserRole.BANK_ADMIN);
        bankAdmin = userRepository.save(bankAdmin);
        bankAdminToken = jwtTokenService.generateToken(bankAdmin);
        
        validRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .build();
    }

    @Test
    void testCreateValidApplication_ShouldReturn201Created() throws Exception {
        mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.loanAmount").value(25000))
                .andExpect(jsonPath("$.loanTermMonths").value(36))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.ratePreference").value("VARIABLE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void testCreateValidApplication_ShouldPersistInDatabase() throws Exception {
        long initialCount = applicationRepository.count();

        String response = mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApplicationDTO createdApp = objectMapper.readValue(response, ApplicationDTO.class);

        assertThat(applicationRepository.count()).isEqualTo(initialCount + 1);
        assertThat(applicationRepository.findById(createdApp.getId())).isPresent();
        assertThat(applicationRepository.findById(createdApp.getId()).get().getStatus())
                .isEqualTo(ApplicationStatus.DRAFT);
    }

    @Test
    void testCreateApplication_WithLoanAmountTooLow_ShouldReturn400() throws Exception {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(50))
                .loanTermMonths(36)
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").value("Loan amount must be at least 100"));
    }

    @Test
    void testCreateApplication_WithLoanAmountTooHigh_ShouldReturn400() throws Exception {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(1500000))
                .loanTermMonths(36)
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").value("Loan amount cannot exceed 1,000,000"));
    }

    @Test
    void testCreateApplication_WithLoanTermTooLow_ShouldReturn400() throws Exception {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(3)
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").value("Loan term must be at least 6 months"));
    }

    @Test
    void testCreateApplication_WithLoanTermTooHigh_ShouldReturn400() throws Exception {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(361)
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").value("Loan term cannot exceed 360 months"));
    }

    @Test
    void testCreateApplication_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/borrower/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateApplication_WithBankAdminRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + bankAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateApplication_ShouldCreateAuditLogEntry() throws Exception {
        String response = mockMvc.perform(post("/api/borrower/applications")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApplicationDTO createdApp = objectMapper.readValue(response, ApplicationDTO.class);

        List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                "Application", 
                createdApp.getId()
        );
        
        assertThat(auditLogs).isNotEmpty();
        assertThat(auditLogs.get(0).getAction()).hasToString("APPLICATION_CREATED");
        assertThat(auditLogs.get(0).getEntityType()).isEqualTo("Application");
        assertThat(auditLogs.get(0).getEntityId()).isEqualTo(createdApp.getId());
    }
}
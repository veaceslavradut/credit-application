package com.creditapp.integration.borrower;

import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationHistory;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class ApplicationRetrievalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String borrowerToken;
    private UUID borrowerId;
    private UUID applicationId;
    private Application testApplication;
    private User testBorrower;

    @BeforeEach
    void setUp() {
        // Create borrower user
        testBorrower = new User();
        testBorrower.setEmail("retriever_" + UUID.randomUUID() + "@test.com");
        testBorrower.setPasswordHash("hashed");
        testBorrower.setFirstName("John");
        testBorrower.setLastName("Retriever");
        testBorrower.setRole(UserRole.BORROWER);
        testBorrower = userRepository.saveAndFlush(testBorrower);
        borrowerId = testBorrower.getId();

        // Generate JWT token
        borrowerToken = jwtTokenService.generateToken(testBorrower);

        // Create test application
        testApplication = Application.builder()
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();
        testApplication = applicationRepository.saveAndFlush(testApplication);
        applicationId = testApplication.getId();

        // Create some history entries
        for (int i = 0; i < 5; i++) {
            ApplicationHistory history = ApplicationHistory.builder()
                    .applicationId(applicationId)
                    .oldStatus(ApplicationStatus.DRAFT)
                    .newStatus(ApplicationStatus.UNDER_REVIEW)
                    .changedAt(LocalDateTime.now().minusHours(5 - i))
                    .changeReason("Status update " + i)
                    .build();
            applicationHistoryRepository.saveAndFlush(history);
        }
    }

    @Test
    void testRetrieveApplication_ShouldReturn200WithAllFields() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicationId.toString()))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.loanAmount").value(25000))
                .andExpect(jsonPath("$.loanTermMonths").value(36))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.ratePreference").value("VARIABLE"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void testRetrieveApplication_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/" + applicationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRetrieveApplication_DifferentBorrower_ShouldReturn404() throws Exception {
        // Create another borrower
        User anotherBorrower = new User();
        anotherBorrower.setEmail("another_retriever_" + UUID.randomUUID() + "@test.com");
        anotherBorrower.setPasswordHash("hashed");
        anotherBorrower.setFirstName("Jane");
        anotherBorrower.setLastName("Other");
        anotherBorrower.setRole(UserRole.BORROWER);
        anotherBorrower = userRepository.saveAndFlush(anotherBorrower);

        String anotherToken = jwtTokenService.generateToken(anotherBorrower);

        mockMvc.perform(get("/api/borrower/applications/" + applicationId)
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRetrieveApplication_NonExistent_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/borrower/applications/" + nonExistentId)
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRetrieveApplicationHistory_ShouldReturn200WithHistoryList() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/" + applicationId + "/history")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void testRetrieveApplicationHistory_WithPagination_ShouldReturn200WithPageData() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/" + applicationId + "/history?page=0&size=2")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void testRetrieveApplicationHistory_SecondPage_ShouldReturnCorrectData() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/" + applicationId + "/history?page=1&size=2")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void testRetrieveApplicationHistory_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/" + applicationId + "/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRetrieveApplicationHistory_DifferentBorrower_ShouldReturn404() throws Exception {
        // Create another borrower
        User anotherBorrower = new User();
        anotherBorrower.setEmail("another_retriever2_" + UUID.randomUUID() + "@test.com");
        anotherBorrower.setPasswordHash("hashed");
        anotherBorrower.setFirstName("Jane");
        anotherBorrower.setLastName("Other");
        anotherBorrower.setRole(UserRole.BORROWER);
        anotherBorrower = userRepository.saveAndFlush(anotherBorrower);

        String anotherToken = jwtTokenService.generateToken(anotherBorrower);

        mockMvc.perform(get("/api/borrower/applications/" + applicationId + "/history")
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRetrieveApplicationHistory_NonExistent_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/borrower/applications/" + nonExistentId + "/history")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRetrieveApplicationHistory_HistoryOrderedByDateDesc() throws Exception {
        mockMvc.perform(get("/api/borrower/applications/" + applicationId + "/history")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeReason").value("Status update 4"))
                .andExpect(jsonPath("$[1].changeReason").value("Status update 3"))
                .andExpect(jsonPath("$[2].changeReason").value("Status update 2"))
                .andExpect(jsonPath("$[3].changeReason").value("Status update 1"))
                .andExpect(jsonPath("$[4].changeReason").value("Status update 0"));
    }

    @Test
    void testRetrieveApplicationHistory_EmptyHistory_ShouldReturnEmptyList() throws Exception {
        // Create application without history
        Application appNoHistory = Application.builder()
                .borrowerId(borrowerId)
                .loanType("HOME")
                .loanAmount(BigDecimal.valueOf(100000))
                .loanTermMonths(240)
                .currency("USD")
                .ratePreference("FIXED")
                .status(ApplicationStatus.DRAFT)
                .build();
        appNoHistory = applicationRepository.saveAndFlush(appNoHistory);

        mockMvc.perform(get("/api/borrower/applications/" + appNoHistory.getId() + "/history")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
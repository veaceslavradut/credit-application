package com.creditapp.integration.borrower;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.model.DeletionRequest;
import com.creditapp.shared.model.DeletionStatus;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.DeletionRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class DataDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeletionRequestRepository deletionRequestRepository;

    private UUID borrowerId;
    private String borrowerEmail;

    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        borrowerEmail = "borrower" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        User user = new User();
        user.setId(borrowerId);
        user.setEmail(borrowerEmail);
        user.setFirstName("Test");
        user.setLastName("Borrower");
        user.setRole(UserRole.BORROWER);
        user.setPasswordHash("hashed");
        userRepository.save(user);
    }

    @Test
    void testRequestDeletionWithoutAuthentication() throws Exception {
        String requestBody = "{\"reason\":\"No longer need service\"}";

        mockMvc.perform(post("/api/borrower/data-deletion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testConfirmDeletionWithoutToken() throws Exception {
        mockMvc.perform(post("/api/borrower/data-deletion/confirm")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCancelDeletionWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/borrower/data-deletion/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeletionRequestPersistence() {
        DeletionRequest request = new DeletionRequest();
        request.setId(UUID.randomUUID());
        request.setBorrowerId(borrowerId);
        request.setStatus(DeletionStatus.PENDING);
        request.setConfirmationToken("test-token");
        request.setConfirmationTokenExpiresAt(LocalDateTime.now().plusDays(7));
        request.setRequestedAt(LocalDateTime.now());

        DeletionRequest saved = deletionRequestRepository.save(request);

        org.junit.jupiter.api.Assertions.assertNotNull(saved.getId());
        org.junit.jupiter.api.Assertions.assertEquals(DeletionStatus.PENDING, saved.getStatus());
    }

    @Test
    void testFindDeletionByToken() {
        DeletionRequest request = new DeletionRequest();
        request.setId(UUID.randomUUID());
        request.setBorrowerId(borrowerId);
        request.setStatus(DeletionStatus.PENDING);
        request.setConfirmationToken("unique-token-123");
        request.setConfirmationTokenExpiresAt(LocalDateTime.now().plusDays(7));
        request.setRequestedAt(LocalDateTime.now());
        deletionRequestRepository.save(request);

        var found = deletionRequestRepository.findByConfirmationToken("unique-token-123");
        org.junit.jupiter.api.Assertions.assertTrue(found.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals(request.getId(), found.get().getId());
    }

    @Test
    void testFindDeletionByBorrowerIdAndStatus() {
        DeletionRequest request = new DeletionRequest();
        request.setId(UUID.randomUUID());
        request.setBorrowerId(borrowerId);
        request.setStatus(DeletionStatus.CONFIRMED);
        request.setConfirmationToken("token");
        request.setConfirmationTokenExpiresAt(LocalDateTime.now().plusDays(7));
        request.setRequestedAt(LocalDateTime.now());
        deletionRequestRepository.save(request);

        var found = deletionRequestRepository.findByBorrowerIdAndStatus(borrowerId, DeletionStatus.CONFIRMED);
        org.junit.jupiter.api.Assertions.assertTrue(found.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals(request.getId(), found.get().getId());
    }
}
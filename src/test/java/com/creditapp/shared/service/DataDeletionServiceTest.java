package com.creditapp.shared.service;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.borrower.dto.DeletionRequestResponse;
import com.creditapp.shared.model.DeletionRequest;
import com.creditapp.shared.model.DeletionStatus;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.DeletionRequestRepository;
import com.creditapp.shared.repository.GDPRConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DataDeletionServiceTest {

    private DataDeletionService dataDelitionService;

    @Mock
    private DeletionRequestRepository deletionRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GDPRConsentRepository consentRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private DataDeletionEmailService dataDeletionEmailService;

    @Mock
    private DataAnonymizationService anonymizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataDelitionService = new DataDeletionService(
                deletionRequestRepository,
                userRepository,
                consentRepository,
                auditService,
                dataDeletionEmailService,
                anonymizationService
        );
    }

    @Test
    void testRequestDeletion() {
        UUID borrowerId = UUID.randomUUID();
        String reason = "No longer need the service";

        User user = new User();
        user.setId(borrowerId);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(user));
        when(deletionRequestRepository.save(any(DeletionRequest.class)))
                .thenAnswer(invocation -> {
                    DeletionRequest req = invocation.getArgument(0);
                    req.setId(UUID.randomUUID());
                    return req;
                });

        DeletionRequestResponse response = dataDelitionService.requestDeletion(borrowerId, reason);

        assertNotNull(response);
        assertEquals(DeletionStatus.PENDING.toString(), response.getStatus());
        assertNotNull(response.getConfirmationLink());
        assertNotNull(response.getExpiresAt());
    }

    @Test
    void testConfirmDeletion() {
        UUID borrowerId = UUID.randomUUID();
        UUID deletionRequestId = UUID.randomUUID();

        DeletionRequest request = new DeletionRequest();
        request.setId(deletionRequestId);
        request.setBorrowerId(borrowerId);
        request.setStatus(DeletionStatus.PENDING);
        request.setConfirmationToken("valid-token");
        request.setConfirmationTokenExpiresAt(LocalDateTime.now().plusDays(1));

        when(deletionRequestRepository.findByConfirmationToken("valid-token"))
                .thenReturn(Optional.of(request));
        when(deletionRequestRepository.save(any(DeletionRequest.class)))
                .thenReturn(request);

        DeletionRequestResponse response = dataDelitionService.confirmDeletion("valid-token", borrowerId);

        assertNotNull(response);
        assertEquals(DeletionStatus.CONFIRMED.toString(), response.getStatus());
    }

    @Test
    void testCancelDeletion() {
        UUID borrowerId = UUID.randomUUID();
        UUID deletionRequestId = UUID.randomUUID();

        DeletionRequest request = new DeletionRequest();
        request.setId(deletionRequestId);
        request.setBorrowerId(borrowerId);
        request.setStatus(DeletionStatus.CONFIRMED);

        when(deletionRequestRepository.findLatestByBorrowerId(borrowerId))
            .thenReturn(Optional.of(request));
        when(deletionRequestRepository.save(any(DeletionRequest.class)))
                .thenReturn(request);

        DeletionRequestResponse response = dataDelitionService.cancelDeletion(borrowerId);

        assertNotNull(response);
        assertEquals(DeletionStatus.CANCELLED.toString(), response.getStatus());
    }
}
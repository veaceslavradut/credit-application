package com.creditapp.borrower.service;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.borrower.dto.SubmitApplicationResponse;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionNotificationTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private UUID borrowerId;
    private UUID applicationId;

    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
    }

    @Test
    void submitApplication_shouldCreateApplicationSubmittedNotification() {
        // Arrange application in DRAFT
        Application draft = Application.builder()
                .id(applicationId)
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(draft));

        Application submitted = Application.builder()
                .id(applicationId)
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationRepository.save(org.mockito.ArgumentMatchers.<Application>any())).thenReturn(submitted);

        // Borrower user
        User borrower = new User();
        borrower.setId(borrowerId);
        borrower.setEmail("borrower@example.com");
        borrower.setFirstName("Alex");
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        // Act
        SubmitApplicationResponse response = applicationService.submitApplication(applicationId, borrowerId);

        // Assert response
        assertNotNull(response);
        assertEquals(applicationId, response.getId());
        assertEquals(ApplicationStatus.SUBMITTED.toString(), response.getStatus());

        // Verify notification created with APPLICATION_SUBMITTED type
        verify(notificationService, times(1))
            .createNotification(
                eq(borrowerId),
                eq(applicationId),
                eq(com.creditapp.shared.model.NotificationType.APPLICATION_SUBMITTED),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
            );

        // Verify application saved
        verify(applicationRepository, times(1)).save(org.mockito.ArgumentMatchers.<Application>any());
    }
}

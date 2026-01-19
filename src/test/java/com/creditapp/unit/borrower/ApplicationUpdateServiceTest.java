package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.ApplicationDetailsDTO;
import com.creditapp.borrower.dto.UpdateApplicationRequest;
import com.creditapp.borrower.dto.UpdateApplicationResponse;
import com.creditapp.borrower.exception.ApplicationNotEditableException;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.exception.ApplicationStaleException;
import com.creditapp.borrower.exception.InvalidApplicationException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationDetails;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationDetailsRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.ApplicationService;
import com.creditapp.shared.audit.BusinessAudit;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.NotificationService;
import com.creditapp.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationDetailsRepository applicationDetailsRepository;

    @Mock
    private AuditService auditService;

        @Mock
        private NotificationService notificationService;

        @Mock
        private UserRepository userRepository;

    private ApplicationService applicationService;

    private UUID borrowerId;
    private UUID applicationId;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                applicationRepository,
                mock(com.creditapp.borrower.repository.ApplicationHistoryRepository.class),
                auditService,
                notificationService,
                userRepository,
                mock(com.creditapp.bank.service.OfferCalculationService.class),
                mock(com.creditapp.shared.service.GDPRConsentService.class)
        );

        borrowerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();

        testApplication = Application.builder()
                .id(applicationId)
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(new BigDecimal("25000"))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1L)
                .build();
    }

    @Test
    void testUpdateApplicationWithValidData() {
        // Given
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("30000"))
                .loanTermMonths(48)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any())).thenReturn(testApplication);

        // When
        UpdateApplicationResponse response = applicationService.updateApplication(applicationId, borrowerId, request);

        // Then
        assertNotNull(response);
        assertEquals(applicationId, response.getId());
        assertEquals(new BigDecimal("30000"), response.getLoanAmount());
        assertEquals(48, response.getLoanTermMonths());
        assertEquals("DRAFT", response.getStatus());
        assertTrue(response.getEditedFields().contains("loanAmount"));
        assertTrue(response.getEditedFields().contains("loanTermMonths"));

        verify(applicationRepository).save(any());
    }

    @Test
    void testUpdateApplicationLoanAmountBelowMinimum() {
        // Given: loan amount below 100
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("50"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        // When & Then
        assertThrows(InvalidApplicationException.class, () ->
                applicationService.updateApplication(applicationId, borrowerId, request)
        );

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateApplicationLoanAmountAboveMaximum() {
        // Given: loan amount above 1000000
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("1000001"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        // When & Then
        assertThrows(InvalidApplicationException.class, () ->
                applicationService.updateApplication(applicationId, borrowerId, request)
        );

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateApplicationLoanTermBelowMinimum() {
        // Given: term below 6 months
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanTermMonths(3)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        // When & Then
        assertThrows(InvalidApplicationException.class, () ->
                applicationService.updateApplication(applicationId, borrowerId, request)
        );

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateApplicationLoanTermAboveMaximum() {
        // Given: term above 360 months
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanTermMonths(361)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        // When & Then
        assertThrows(InvalidApplicationException.class, () ->
                applicationService.updateApplication(applicationId, borrowerId, request)
        );

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateApplicationNotInDraftStatus() {
        // Given: application in SUBMITTED status
        testApplication.setStatus(ApplicationStatus.SUBMITTED);
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("30000"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        // When & Then
        assertThrows(ApplicationNotEditableException.class, () ->
                applicationService.updateApplication(applicationId, borrowerId, request)
        );

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateApplicationDifferentBorrower() {
        // Given: different borrower ID
        UUID otherBorrowerId = UUID.randomUUID();
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("30000"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        // When & Then
        assertThrows(ApplicationNotFoundException.class, () ->
                applicationService.updateApplication(applicationId, otherBorrowerId, request)
        );

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateApplicationNotFound() {
        // Given: application doesn't exist
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("30000"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApplicationNotFoundException.class, () ->
                applicationService.updateApplication(applicationId, borrowerId, request)
        );
    }

    @Test
    void testUpdateApplicationWithApplicationDetails() {
        // Given
        ApplicationDetailsDTO details = ApplicationDetailsDTO.builder()
                .annualIncome(new BigDecimal("75000"))
                .employmentStatus("EMPLOYED")
                .downPaymentAmount(new BigDecimal("5000"))
                .build();

        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("30000"))
                .applicationDetails(details)
                .build();

        testApplication.setDetails(null);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any())).thenReturn(testApplication);

        // When
        UpdateApplicationResponse response = applicationService.updateApplication(applicationId, borrowerId, request);

        // Then
        assertNotNull(response);
        assertTrue(response.getEditedFields().contains("annualIncome"));
        assertTrue(response.getEditedFields().contains("employmentStatus"));
        assertTrue(response.getEditedFields().contains("downPaymentAmount"));

        verify(applicationRepository).save(any());
    }

    @Test
    void testUpdateApplicationVersionIncrement() {
        // Given
        long initialVersion = 1L;
        testApplication.setVersion(initialVersion);
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("30000"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        Application savedApplication = testApplication;
        savedApplication.setVersion(2L);
        when(applicationRepository.save(any())).thenReturn(savedApplication);

        // When
        UpdateApplicationResponse response = applicationService.updateApplication(applicationId, borrowerId, request);

        // Then
        assertEquals(2L, response.getVersion());
    }

    @Test
    void testUpdateApplicationOptimisticLockingFailure() {
        // Given
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanAmount(new BigDecimal("30000"))
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("Optimistic lock", new Exception()));

        Application currentVersion = Application.builder()
                .id(applicationId)
                .borrowerId(borrowerId)
                .version(5L)
                .status(ApplicationStatus.DRAFT)
                .build();

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(testApplication))
                .thenReturn(Optional.of(currentVersion));

        // When & Then
        assertThrows(ApplicationStaleException.class, () ->
                applicationService.updateApplication(applicationId, borrowerId, request)
        );
    }

    @Test
    void testUpdateApplicationNoChangesMade() {
        // Given: all current values in request
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(testApplication.getLoanAmount())
                .loanTermMonths(testApplication.getLoanTermMonths())
                .currency(testApplication.getCurrency())
                .ratePreference(testApplication.getRatePreference())
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any())).thenReturn(testApplication);

        // When
        UpdateApplicationResponse response = applicationService.updateApplication(applicationId, borrowerId, request);

        // Then
        assertNotNull(response);
        assertTrue(response.getEditedFields().isEmpty());
    }

    @Test
    void testUpdateApplicationMultipleFields() {
        // Given: multiple field updates
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
                .loanType("HOME")
                .loanAmount(new BigDecimal("100000"))
                .loanTermMonths(120)
                .currency("USD")
                .ratePreference("FIXED")
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any())).thenReturn(testApplication);

        // When
        UpdateApplicationResponse response = applicationService.updateApplication(applicationId, borrowerId, request);

        // Then
        assertNotNull(response);
        assertEquals(5, response.getEditedFields().size());
        assertTrue(response.getEditedFields().contains("loanType"));
        assertTrue(response.getEditedFields().contains("loanAmount"));
        assertTrue(response.getEditedFields().contains("loanTermMonths"));
        assertTrue(response.getEditedFields().contains("currency"));
        assertTrue(response.getEditedFields().contains("ratePreference"));
    }
}
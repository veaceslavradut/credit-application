package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.SubmitApplicationResponse;
import com.creditapp.borrower.exception.ApplicationAlreadySubmittedException;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.exception.SubmissionValidationException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.ApplicationService;
import com.creditapp.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ApplicationService applicationService;

    private UUID applicationId;
    private UUID borrowerId;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();

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
                .build();
    }

    @Test
    void testSubmitApplicationWithValidData() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        SubmitApplicationResponse response = applicationService.submitApplication(applicationId, borrowerId);

        assertNotNull(response);
        assertEquals(applicationId, response.getId());
        assertEquals("SUBMITTED", response.getStatus());
        assertNotNull(response.getMessage());
        assertNotNull(response.getApplication());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void testSubmitApplicationNotFound() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationDifferentBorrower() {
        UUID differentBorrowerId = UUID.randomUUID();
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        assertThrows(ApplicationNotFoundException.class, () -> 
            applicationService.submitApplication(applicationId, differentBorrowerId)
        );
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationAlreadySubmitted() {
        testApplication.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        assertThrows(ApplicationAlreadySubmittedException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationInUnderReviewStatus() {
        testApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        assertThrows(ApplicationAlreadySubmittedException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationMissingLoanType() {
        testApplication.setLoanType(null);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );

        assertTrue(exception.getMissingFields().contains("loanType"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationMissingLoanAmount() {
        testApplication.setLoanAmount(null);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );

        assertTrue(exception.getMissingFields().contains("loanAmount"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationMissingLoanTermMonths() {
        testApplication.setLoanTermMonths(null);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );

        assertTrue(exception.getMissingFields().contains("loanTermMonths"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationMissingCurrency() {
        testApplication.setCurrency(null);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );

        assertTrue(exception.getMissingFields().contains("currency"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationMultipleMissingFields() {
        testApplication.setLoanType(null);
        testApplication.setLoanAmount(null);
        testApplication.setCurrency(null);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, () -> 
            applicationService.submitApplication(applicationId, borrowerId)
        );

        assertEquals(3, exception.getMissingFields().size());
        assertTrue(exception.getMissingFields().contains("loanType"));
        assertTrue(exception.getMissingFields().contains("loanAmount"));
        assertTrue(exception.getMissingFields().contains("currency"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testSubmitApplicationResponseIncludesFullApplicationDetails() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        SubmitApplicationResponse response = applicationService.submitApplication(applicationId, borrowerId);

        assertNotNull(response.getApplication());
        assertEquals(applicationId, response.getApplication().getId());
        assertEquals("PERSONAL", response.getApplication().getLoanType());
        assertEquals(new BigDecimal("25000"), response.getApplication().getLoanAmount());
        assertEquals(36, response.getApplication().getLoanTermMonths());
        assertEquals("EUR", response.getApplication().getCurrency());
    }
}
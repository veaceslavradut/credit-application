package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.dto.CreateApplicationRequest;
import com.creditapp.borrower.exception.InvalidApplicationException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationService.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationHistoryRepository applicationHistoryRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private UUID borrowerId;
    private CreateApplicationRequest validRequest;

    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        validRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .build();
    }

    @Test
    void createApplication_withValidData_shouldSucceed() {
        // Given
        Application savedApplication = Application.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);

        // When
        ApplicationDTO result = applicationService.createApplication(borrowerId, validRequest);

        // Then
        assertNotNull(result);
        assertEquals(savedApplication.getId(), result.getId());
        assertEquals(ApplicationStatus.DRAFT, result.getStatus());
        assertEquals(BigDecimal.valueOf(25000), result.getLoanAmount());
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void createApplication_withLoanAmountTooLow_shouldThrowInvalidApplicationException() {
        // Given
        validRequest.setLoanAmount(BigDecimal.valueOf(50));

        // When & Then
        InvalidApplicationException exception = assertThrows(InvalidApplicationException.class, 
                () -> applicationService.createApplication(borrowerId, validRequest));
        
        assertTrue(exception.getMessage().contains("at least 100"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void createApplication_withLoanAmountTooHigh_shouldThrowInvalidApplicationException() {
        // Given
        validRequest.setLoanAmount(BigDecimal.valueOf(1500000));

        // When & Then
        InvalidApplicationException exception = assertThrows(InvalidApplicationException.class, 
                () -> applicationService.createApplication(borrowerId, validRequest));
        
        assertTrue(exception.getMessage().contains("cannot exceed 1,000,000"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void createApplication_withLoanTermTooShort_shouldThrowInvalidApplicationException() {
        // Given
        validRequest.setLoanTermMonths(3);

        // When & Then
        InvalidApplicationException exception = assertThrows(InvalidApplicationException.class, 
                () -> applicationService.createApplication(borrowerId, validRequest));
        
        assertTrue(exception.getMessage().contains("at least 6 months"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void createApplication_withLoanTermTooLong_shouldThrowInvalidApplicationException() {
        // Given
        validRequest.setLoanTermMonths(361);

        // When & Then
        InvalidApplicationException exception = assertThrows(InvalidApplicationException.class, 
                () -> applicationService.createApplication(borrowerId, validRequest));
        
        assertTrue(exception.getMessage().contains("cannot exceed 360 months"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void createApplication_withoutRatePreference_shouldDefaultToVARIABLE() {
        // Given
        validRequest.setRatePreference(null);
        
        Application savedApplication = Application.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .build();

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);

        // When
        ApplicationDTO result = applicationService.createApplication(borrowerId, validRequest);

        // Then
        assertNotNull(result);
        assertEquals("VARIABLE", result.getRatePreference());
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void createApplication_withNullLoanAmount_shouldThrowInvalidApplicationException() {
        // Given
        validRequest.setLoanAmount(null);

        // When & Then
        InvalidApplicationException exception = assertThrows(InvalidApplicationException.class, 
                () -> applicationService.createApplication(borrowerId, validRequest));
        
        assertTrue(exception.getMessage().contains("Loan amount is required"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void createApplication_withNullLoanTerm_shouldThrowInvalidApplicationException() {
        // Given
        validRequest.setLoanTermMonths(null);

        // When & Then
        InvalidApplicationException exception = assertThrows(InvalidApplicationException.class, 
                () -> applicationService.createApplication(borrowerId, validRequest));
        
        assertTrue(exception.getMessage().contains("Loan term is required"));
        verify(applicationRepository, never()).save(any(Application.class));
    }
}

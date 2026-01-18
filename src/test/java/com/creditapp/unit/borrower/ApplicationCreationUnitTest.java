package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.dto.CreateApplicationRequest;
import com.creditapp.borrower.exception.InvalidApplicationException;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationCreationUnitTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Mock
    private AuditService auditService;

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
    void testCreateValidApplication_ShouldReturnDraftStatus() {
        Application savedApplication = Application.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .loanType(validRequest.getLoanType())
                .loanAmount(validRequest.getLoanAmount())
                .loanTermMonths(validRequest.getLoanTermMonths())
                .currency(validRequest.getCurrency())
                .ratePreference(validRequest.getRatePreference())
                .status(ApplicationStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);
        when(applicationRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(savedApplication));

        ApplicationDTO result = applicationService.createApplication(borrowerId, validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(result.getLoanType()).isEqualTo("PERSONAL");
        assertThat(result.getLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        assertThat(result.getLoanTermMonths()).isEqualTo(36);
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getRatePreference()).isEqualTo("VARIABLE");

        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void testCreateApplication_WithLoanAmountLessThan100_ShouldThrowException() {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(50))
                .loanTermMonths(36)
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> applicationService.createApplication(borrowerId, invalidRequest))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Loan amount must be at least 100");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreateApplication_WithLoanAmountGreaterThan1Million_ShouldThrowException() {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(1500000))
                .loanTermMonths(36)
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> applicationService.createApplication(borrowerId, invalidRequest))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Loan amount cannot exceed 1,000,000");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreateApplication_WithLoanTermLessThan6Months_ShouldThrowException() {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(3)
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> applicationService.createApplication(borrowerId, invalidRequest))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Loan term must be at least 6 months");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreateApplication_WithLoanTermGreaterThan360Months_ShouldThrowException() {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(361)
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> applicationService.createApplication(borrowerId, invalidRequest))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Loan term cannot exceed 360 months");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreateApplication_WithoutRatePreference_ShouldDefaultToVariable() {
        CreateApplicationRequest requestWithoutRatePreference = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference(null)
                .build();

        Application savedApplication = Application.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);
        when(applicationRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(savedApplication));

        ApplicationDTO result = applicationService.createApplication(borrowerId, requestWithoutRatePreference);

        assertThat(result.getRatePreference()).isEqualTo("VARIABLE");
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void testCreateApplication_WithMissingLoanAmount_ShouldThrowException() {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(null)
                .loanTermMonths(36)
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> applicationService.createApplication(borrowerId, invalidRequest))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Loan amount is required");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreateApplication_WithMissingLoanTerm_ShouldThrowException() {
        CreateApplicationRequest invalidRequest = CreateApplicationRequest.builder()
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(null)
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> applicationService.createApplication(borrowerId, invalidRequest))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Loan term is required");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreateApplication_ShouldIncludeAllRequiredFieldsInResponse() {
        Application savedApplication = Application.builder()
                .id(UUID.randomUUID())
                .borrowerId(borrowerId)
                .loanType("PERSONAL")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(36)
                .currency("EUR")
                .ratePreference("VARIABLE")
                .status(ApplicationStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);
        when(applicationRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(savedApplication));

        ApplicationDTO result = applicationService.createApplication(borrowerId, validRequest);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getLoanType()).isNotNull();
        assertThat(result.getLoanAmount()).isNotNull();
        assertThat(result.getLoanTermMonths()).isNotNull();
        assertThat(result.getCurrency()).isNotNull();
        assertThat(result.getRatePreference()).isNotNull();
        assertThat(result.getStatus()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
}
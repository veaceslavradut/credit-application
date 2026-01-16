package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.ApplicationDTO;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ListApplicationsService (ApplicationService list/view functionality).
 */
@ExtendWith(MockitoExtension.class)
class ListApplicationsServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private UUID borrowerId;
    private UUID applicationId1;
    private UUID applicationId2;
    private Application app1;
    private Application app2;

    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        applicationId1 = UUID.randomUUID();
        applicationId2 = UUID.randomUUID();

        // Create test applications
        app1 = Application.builder()
                .id(applicationId1)
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

        app2 = Application.builder()
                .id(applicationId2)
                .borrowerId(borrowerId)
                .loanType("HOME")
                .loanAmount(BigDecimal.valueOf(100000))
                .loanTermMonths(240)
                .currency("EUR")
                .ratePreference("FIXED")
                .status(ApplicationStatus.SUBMITTED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void testListApplicationsForBorrowerWithMultipleApps() {
        // Given: borrower with 2 applications
        List<Application> apps = Arrays.asList(app1, app2);
        Page<Application> page = new PageImpl<>(apps, PageRequest.of(0, 10), 2);

        when(applicationRepository.findByBorrowerId(borrowerId, PageRequest.of(0, 10)))
                .thenReturn(page);

        // When: list applications for borrower
        Page<ApplicationDTO> result = applicationService.listApplicationsByBorrower(borrowerId, PageRequest.of(0, 10));

        // Then: verify page returned with correct applications
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void testListApplicationsWithPagination() {
        // Given: borrower with 15 applications, requesting page 0 with size 10
        List<Application> firstPageApps = Arrays.asList(app1, app2);
        Page<Application> page = new PageImpl<>(firstPageApps, PageRequest.of(0, 10), 15);

        when(applicationRepository.findByBorrowerId(borrowerId, PageRequest.of(0, 10)))
                .thenReturn(page);

        // When: list first page
        Page<ApplicationDTO> result = applicationService.listApplicationsByBorrower(borrowerId, PageRequest.of(0, 10));

        // Then: verify pagination
        assertEquals(2, result.getContent().size());
        assertEquals(15, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    void testListApplicationsEmptyPage() {
        // Given: borrower with no applications
        Page<Application> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(applicationRepository.findByBorrowerId(borrowerId, PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        // When: list applications
        Page<ApplicationDTO> result = applicationService.listApplicationsByBorrower(borrowerId, PageRequest.of(0, 10));

        // Then: verify empty page
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetApplicationAccessControl() {
        // Given: application belongs to borrowerId
        UUID otherBorrowerId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId1))
                .thenReturn(java.util.Optional.of(app1));

        // When: try to access application as different borrower
        // Then: throw ApplicationNotFoundException (access denied)
        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.getApplication(applicationId1, otherBorrowerId);
        });
    }

    @Test
    void testGetApplicationValid() {
        // Given: application exists and belongs to borrower
        when(applicationRepository.findById(applicationId1))
                .thenReturn(java.util.Optional.of(app1));

        // When: get application
        ApplicationDTO result = applicationService.getApplication(applicationId1, borrowerId);

        // Then: verify returned correctly
        assertNotNull(result);
        assertEquals(applicationId1, result.getId());
        assertEquals("PERSONAL", result.getLoanType());
        assertEquals(ApplicationStatus.DRAFT, result.getStatus());
    }

    @Test
    void testGetApplicationNotFound() {
        // Given: application doesn't exist
        when(applicationRepository.findById(any(UUID.class)))
                .thenReturn(java.util.Optional.empty());

        // When: try to get non-existent application
        // Then: throw ApplicationNotFoundException
        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.getApplication(UUID.randomUUID(), borrowerId);
        });
    }

    @Test
    void testApplicationDTOHasAllRequiredFields() {
        // Given: application with all fields
        when(applicationRepository.findById(applicationId1))
                .thenReturn(java.util.Optional.of(app1));

        // When: get application
        ApplicationDTO result = applicationService.getApplication(applicationId1, borrowerId);

        // Then: verify all required fields present
        assertNotNull(result.getId());
        assertNotNull(result.getLoanType());
        assertNotNull(result.getLoanAmount());
        assertNotNull(result.getLoanTermMonths());
        assertNotNull(result.getCurrency());
        assertNotNull(result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }
}
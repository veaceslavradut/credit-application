package com.creditapp.unit.bank;

import com.creditapp.bank.dto.ApplicationQueueResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankAdminQueueService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Story 4.2: BankAdminQueueService
 */
class BankAdminQueueServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private BankAdminQueueService bankAdminQueueService;

    private UUID bankId;
    private UUID applicationId1;
    private UUID applicationId2;
    private UUID borrowerId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bankId = UUID.randomUUID();
        applicationId1 = UUID.randomUUID();
        applicationId2 = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
    }

    @Test
    void testGetApplicationQueue_EmptyResults() {
        // Given
        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "NEWEST_FIRST", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(0, response.getTotalCount());
        assertTrue(response.getApplications().isEmpty());
        assertFalse(response.isHasMore());
    }

    @Test
    void testGetApplicationQueue_WithApplications() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "NEWEST_FIRST", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalCount());
        assertEquals(2, response.getApplications().size());
        assertFalse(response.isHasMore());
        assertEquals(1, response.getQueueMetrics().getDocumentsAwaitingReview()); // Badge count (SUBMITTED)
    }

    @Test
    void testGetApplicationQueue_FilterByStatus() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When - filter by SUBMITTED status
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, List.of("SUBMITTED"), null, null, null, null, null, "NEWEST_FIRST", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getApplications().size());
        assertEquals("SUBMITTED", response.getApplications().get(0).getStatus());
    }

    @Test
    void testGetApplicationQueue_FilterByLoanType() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When - filter by HOME_LOAN
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, List.of("HOME_LOAN"), null, null, null, null, "NEWEST_FIRST", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(new BigDecimal("50000"), response.getApplications().get(0).getLoanAmount());
    }

    @Test
    void testGetApplicationQueue_FilterByAmountRange() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When - filter by amount range 40000-60000
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, new BigDecimal("40000"), new BigDecimal("60000"), 
            "NEWEST_FIRST", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(new BigDecimal("50000"), response.getApplications().get(0).getLoanAmount());
    }

    @Test
    void testGetApplicationQueue_FilterByDateRange() {
        // Given
        User borrower = createBorrower();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        app1.setSubmittedAt(yesterday);
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);
        app2.setSubmittedAt(now);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When - filter by today's date only
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, LocalDate.now(), LocalDate.now(), null, null, 
            "NEWEST_FIRST", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(applicationId2, response.getApplications().get(0).getApplicationId());
    }

    @Test
    void testGetApplicationQueue_SearchByApplicationId() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When - search by partial application ID
        String searchId = applicationId1.toString().substring(0, 8);
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "NEWEST_FIRST", searchId, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(applicationId1, response.getApplications().get(0).getApplicationId());
    }

    @Test
    void testGetApplicationQueue_SearchByBorrowerEmail() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(offer1)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(List.of(app1));

        // When - search by partial email (case-insensitive)
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "NEWEST_FIRST", null, "TEST", null
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals("test@example.com", response.getApplications().get(0).getBorrowerEmail());
    }

    @Test
    void testGetApplicationQueue_SearchByBorrowerName() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(offer1)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(List.of(app1));

        // When - search by partial name (case-insensitive)
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "NEWEST_FIRST", null, null, "john"
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertTrue(response.getApplications().get(0).getBorrowerName().toLowerCase().contains("john"));
    }

    @Test
    void testGetApplicationQueue_SortByOldestFirst() {
        // Given
        User borrower = createBorrower();
        LocalDateTime now = LocalDateTime.now();
        
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        app1.setSubmittedAt(now.minusDays(2));
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);
        app2.setSubmittedAt(now.minusDays(1));

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When - sort by oldest first
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "OLDEST_FIRST", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalCount());
        assertEquals(applicationId1, response.getApplications().get(0).getApplicationId());
        assertEquals(applicationId2, response.getApplications().get(1).getApplicationId());
    }

    @Test
    void testGetApplicationQueue_SortByAmountHighLow() {
        // Given
        User borrower = createBorrower();
        Application app1 = createApplication(applicationId1, borrowerId, "PERSONAL_LOAN", 
            new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
        app1.setBorrower(borrower);
        
        Application app2 = createApplication(applicationId2, borrowerId, "HOME_LOAN", 
            new BigDecimal("50000"), ApplicationStatus.UNDER_REVIEW);
        app2.setBorrower(borrower);

        Offer offer1 = createOffer(applicationId1, bankId, new BigDecimal("5.5"));
        Offer offer2 = createOffer(applicationId2, bankId, new BigDecimal("4.5"));

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(offer1, offer2)));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(Arrays.asList(app1, app2));

        // When - sort by amount high to low
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "AMOUNT_HIGH_LOW", null, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalCount());
        assertEquals(new BigDecimal("50000"), response.getApplications().get(0).getLoanAmount());
        assertEquals(new BigDecimal("10000"), response.getApplications().get(1).getLoanAmount());
    }

    @Test
    void testGetApplicationQueue_Pagination() {
        // Given
        User borrower = createBorrower();
        List<Application> applications = new ArrayList<>();
        List<Offer> offers = new ArrayList<>();
        
        for (int i = 0; i < 25; i++) {
            UUID appId = UUID.randomUUID();
            Application app = createApplication(appId, borrowerId, "PERSONAL_LOAN", 
                new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
            app.setBorrower(borrower);
            applications.add(app);
            offers.add(createOffer(appId, bankId, new BigDecimal("5.5")));
        }

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(offers));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(applications);

        // When - page 0
        ApplicationQueueResponse page0 = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "NEWEST_FIRST", null, null, null
        );

        // Then - page 0
        assertNotNull(page0);
        assertEquals(25, page0.getTotalCount());
        assertEquals(20, page0.getApplications().size()); // Page size is 20
        assertTrue(page0.isHasMore());

        // When - page 1
        ApplicationQueueResponse page1 = bankAdminQueueService.getApplicationQueue(
            bankId, 1, null, null, null, null, null, null, "NEWEST_FIRST", null, null, null
        );

        // Then - page 1
        assertNotNull(page1);
        assertEquals(25, page1.getTotalCount());
        assertEquals(5, page1.getApplications().size()); // Remaining 5 items
        assertFalse(page1.isHasMore());
    }

    @Test
    void testGetApplicationQueue_ResponseTime() {
        // Given
        User borrower = createBorrower();
        List<Application> applications = new ArrayList<>();
        List<Offer> offers = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            UUID appId = UUID.randomUUID();
            Application app = createApplication(appId, borrowerId, "PERSONAL_LOAN", 
                new BigDecimal("10000"), ApplicationStatus.SUBMITTED);
            app.setBorrower(borrower);
            applications.add(app);
            offers.add(createOffer(appId, bankId, new BigDecimal("5.5")));
        }

        when(offerRepository.findByBankId(any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(offers));
        when(applicationRepository.findAllById(anyIterable()))
            .thenReturn(applications);

        // When
        long startTime = System.currentTimeMillis();
        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId, 0, null, null, null, null, null, null, "NEWEST_FIRST", null, null, null
        );
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertNotNull(response);
        assertTrue(duration < 200, "Response time should be < 200ms, but was " + duration + "ms");
    }

    // Helper methods
    private User createBorrower() {
        User user = new User();
        user.setId(borrowerId);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(UserRole.BORROWER);
        return user;
    }

    private Application createApplication(UUID id, UUID borrowerId, String loanType, 
                                          BigDecimal amount, ApplicationStatus status) {
        return Application.builder()
            .id(id)
            .borrowerId(borrowerId)
            .loanType(loanType)
            .loanAmount(amount)
            .loanTermMonths(12)
            .currency("USD")
            .status(status)
            .submittedAt(LocalDateTime.now())
            .build();
    }

    private Offer createOffer(UUID applicationId, UUID bankId, BigDecimal apr) {
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(applicationId);
        offer.setBankId(bankId);
        offer.setApr(apr);
        offer.setMonthlyPayment(new BigDecimal("850.00"));
        // createdAt is auto-generated by @CreationTimestamp
        return offer;
    }
}

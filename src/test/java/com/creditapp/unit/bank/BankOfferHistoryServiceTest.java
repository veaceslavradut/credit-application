package com.creditapp.unit.bank;

import com.creditapp.bank.dto.OfferHistoryFilter;
import com.creditapp.bank.dto.OfferHistoryItem;
import com.creditapp.bank.dto.OfferHistoryResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.BankOfferHistoryService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.auth.repository.UserRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankOfferHistoryServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BankOfferHistoryService service;

    private UUID bankId;
    private UUID applicationId;
    private UUID borrowerId;
    private Offer offer1;
    private Offer offer2;
    private Application application;
    private User borrower;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();

        borrower = new User();
        borrower.setId(borrowerId);
        borrower.setFirstName("John");
        borrower.setLastName("Doe");

        application = new Application();
        application.setId(applicationId);
        application.setBorrowerId(borrowerId);
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
        application.setLoanAmount(new BigDecimal("50000"));

        offer1 = new Offer();
        offer1.setId(UUID.randomUUID());
        offer1.setApplicationId(applicationId);
        offer1.setBankId(bankId);
        offer1.setApr(new BigDecimal("5.50"));
        offer1.setMonthlyPayment(new BigDecimal("1000"));
        offer1.setProcessingTimeDays(5);
        offer1.setOfferStatus(OfferStatus.SUBMITTED);
        offer1.setOfferSubmittedAt(LocalDateTime.now().minusDays(2));
        offer1.setBorrowerSelectedAt(null);

        offer2 = new Offer();
        offer2.setId(UUID.randomUUID());
        offer2.setApplicationId(applicationId);
        offer2.setBankId(bankId);
        offer2.setApr(new BigDecimal("6.25"));
        offer2.setMonthlyPayment(new BigDecimal("1050"));
        offer2.setProcessingTimeDays(3);
        offer2.setOfferStatus(OfferStatus.ACCEPTED);
        offer2.setOfferSubmittedAt(LocalDateTime.now().minusDays(1));
        offer2.setBorrowerSelectedAt(LocalDateTime.now());
    }

    @Test
    void testDetermineOfferStatus_SUBMITTED() {
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer1));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals(1, response.getItems().size());
        assertEquals("SUBMITTED", response.getItems().get(0).getStatus());
    }

    @Test
    void testDetermineOfferStatus_ACCEPTED() {
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer2));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals(1, response.getItems().size());
        assertEquals("ACCEPTED", response.getItems().get(0).getStatus());
    }

    @Test
    void testDetermineOfferStatus_EXPIRED() {
        offer1.setOfferStatus(OfferStatus.EXPIRED);
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer1));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals("EXPIRED", response.getItems().get(0).getStatus());
    }

    @Test
    void testDetermineOfferStatus_WITHDRAWN() {
        offer1.setOfferStatus(OfferStatus.WITHDRAWN);
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer1));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals("WITHDRAWN", response.getItems().get(0).getStatus());
    }

    @Test
    void testBorrowerStatus_NOT_VIEWED() {
        application.setStatus(ApplicationStatus.DRAFT);
        offer1.setBorrowerSelectedAt(null);
        
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer1));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals("NOT_VIEWED", response.getItems().get(0).getBorrowerStatus());
    }

    @Test
    void testBorrowerStatus_VIEWED() {
        application.setStatus(ApplicationStatus.OFFERS_AVAILABLE);
        offer1.setBorrowerSelectedAt(null);
        
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer1));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals("VIEWED", response.getItems().get(0).getBorrowerStatus());
    }

    @Test
    void testBorrowerStatus_ACCEPTED_OTHER() {
        offer1.setBorrowerSelectedAt(LocalDateTime.now());
        
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer1));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals("ACCEPTED_OTHER", response.getItems().get(0).getBorrowerStatus());
    }

    @Test
    void testSorting_BySubmittedDateDESC() {
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, "offerSubmittedAt_DESC");
        Page<Offer> page = new PageImpl<>(List.of(offer1, offer2));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals(2, response.getItems().size());
        assertEquals(offer2.getId(), response.getItems().get(0).getOfferId());
        assertEquals(offer1.getId(), response.getItems().get(1).getOfferId());
    }

    @Test
    void testSorting_ByAPR_ASC() {
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, "apr_ASC");
        Page<Offer> page = new PageImpl<>(List.of(offer1, offer2));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals(offer1.getId(), response.getItems().get(0).getOfferId());
        assertEquals(offer2.getId(), response.getItems().get(1).getOfferId());
    }

    @Test
    void testFiltering_ByStatus() {
        OfferHistoryFilter filter = new OfferHistoryFilter(
                List.of("ACCEPTED"),
                null, null, null, null, null, null, null
        );
        Page<Offer> page = new PageImpl<>(List.of(offer1, offer2));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals(1, response.getItems().size());
        assertEquals("ACCEPTED", response.getItems().get(0).getStatus());
    }

    @Test
    void testFiltering_ByAPRRange() {
        OfferHistoryFilter filter = new OfferHistoryFilter(
                null, null, null,
                new BigDecimal("5.00"),
                new BigDecimal("6.00"),
                null, null, null
        );
        Page<Offer> page = new PageImpl<>(List.of(offer1, offer2));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("5.50"), response.getItems().get(0).getApr());
    }

    @Test
    void testPagination_Page0() {
        OfferHistoryFilter filter = new OfferHistoryFilter(null, null, null, null, null, null, null, null);
        Page<Offer> page = new PageImpl<>(List.of(offer1, offer2));
        when(offerRepository.findByBankId(eq(bankId), any(Pageable.class))).thenReturn(page);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        OfferHistoryResponse response = service.getOfferHistory(bankId, filter, 0, 20);

        assertEquals(0, response.getPage());
        assertEquals(20, response.getPageSize());
        assertEquals(2, response.getTotalCount());
    }
}

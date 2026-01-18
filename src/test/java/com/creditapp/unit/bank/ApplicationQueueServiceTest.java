package com.creditapp.unit.bank;

import com.creditapp.bank.dto.ApplicationQueueRequest;
import com.creditapp.bank.dto.ApplicationQueueResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.ApplicationQueueService;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationDocumentRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
public class ApplicationQueueServiceTest {

    private OfferRepository offerRepository;
    private ApplicationRepository applicationRepository;
    private ApplicationDocumentRepository applicationDocumentRepository;
    private ApplicationQueueService applicationQueueService;

    @BeforeEach
    void setup() {
        offerRepository = Mockito.mock(OfferRepository.class);
        applicationRepository = Mockito.mock(ApplicationRepository.class);
        applicationDocumentRepository = Mockito.mock(ApplicationDocumentRepository.class);
        applicationQueueService = new ApplicationQueueService(offerRepository, applicationRepository, applicationDocumentRepository);
    }

    @Test
    void returnsQueueItemsForBankWithMetrics() {
        UUID bankId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();

        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplicationId(appId);
        offer.setBankId(bankId);
        offer.setOfferStatus(OfferStatus.SUBMITTED);
        offer.setApr(new BigDecimal("5.25"));
        offer.setMonthlyPayment(new BigDecimal("250.00"));
        offer.setTotalCost(new BigDecimal("15000.00"));
        offer.setOriginationFee(new BigDecimal("100.00"));
        offer.setProcessingTimeDays(5);
        offer.setValidityPeriodDays(30);
        offer.setExpiresAt(LocalDateTime.now().plusDays(10));

        List<Offer> offerList = Collections.singletonList(offer);
        when(offerRepository.findByBankId(Mockito.eq(bankId), Mockito.<Pageable>any()))
            .thenReturn(new PageImpl<>(offerList, PageRequest.of(0, 20), 1));
        when(offerRepository.countDistinctApplicationsByBankId(Mockito.eq(bankId))).thenReturn(1L);
        when(offerRepository.findByApplicationIdAndOfferStatus(Mockito.eq(appId), Mockito.eq(OfferStatus.ACCEPTED))).thenReturn(java.util.Optional.empty());

        Application app = Application.builder()
            .id(appId)
            .borrowerId(UUID.randomUUID())
            .loanType("PERSONAL")
            .loanAmount(new BigDecimal("10000"))
            .loanTermMonths(48)
            .currency("USD")
            .status(ApplicationStatus.UNDER_REVIEW)
            .createdAt(LocalDateTime.now().minusDays(2))
            .submittedAt(LocalDateTime.now().minusDays(2))
            .updatedAt(LocalDateTime.now())
            .build();
        User borrower = new User();
        borrower.setFirstName("Jane");
        borrower.setLastName("Doe");
        borrower.setEmail("jane@example.com");
        borrower.setPhoneNumber("+1234567890");
        app.setBorrower(borrower);

        when(applicationRepository.findAllById(Mockito.<Iterable<UUID>>any()))
            .thenReturn(List.of(app));
        when(applicationDocumentRepository.findByApplicationIdAndDeletedAtIsNull(Mockito.eq(appId)))
            .thenReturn(Collections.emptyList());

        ApplicationQueueRequest request = ApplicationQueueRequest.builder()
            .limit(20)
            .offset(0)
            .build();

        ApplicationQueueResponse response = applicationQueueService.getApplicationQueue(bankId, request);

        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getApplications().size());
        assertEquals(20, response.getLimit());
        assertEquals(0, response.getOffset());

        var item = response.getApplications().get(0);
        assertEquals(appId, item.getApplicationId());
        assertEquals("Jane Doe", item.getBorrowerName());
        assertEquals("jane@example.com", item.getBorrowerEmail());
        assertEquals(new BigDecimal("10000"), item.getLoanAmount());
        assertEquals(48, item.getTermMonths());
        assertEquals(new BigDecimal("5.25"), item.getSelectedOfferAPR());
        assertEquals(new BigDecimal("250.00"), item.getSelectedOfferMonthlyPayment());
        assertEquals(ApplicationStatus.UNDER_REVIEW.name(), item.getStatus());
        assertEquals("none", item.getDocumentsStatus());
        assertEquals("pending", item.getApprovalStatus());

        assertNotNull(response.getQueueMetrics());
        assertEquals(1, response.getQueueMetrics().getTotalApplications());
        assertEquals(0, response.getQueueMetrics().getApprovedCount());
        assertEquals(0, response.getQueueMetrics().getRejectedCount());
    }
}
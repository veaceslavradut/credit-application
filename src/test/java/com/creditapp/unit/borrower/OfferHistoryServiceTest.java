package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.OfferHistoryResponse;
import com.creditapp.borrower.service.OfferHistoryService;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferHistoryServiceTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @InjectMocks
    private OfferHistoryService offerHistoryService;

    @Test
    void testGetOfferHistory_ReturnsResponse() {
        UUID borrowerId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID bankId = UUID.randomUUID();
        
        Application app = new Application();
        app.setId(applicationId);
        app.setLoanTermMonths(36);
        
        Organization bank = new Organization();
        bank.setId(bankId);
        bank.setName("Test Bank");
        
        List<Offer> offers = Arrays.asList(new Offer(), new Offer());
        Page<Offer> offerPage = new PageImpl<>(offers);
        
        when(offerRepository.findOffersByBorrowerId(eq(borrowerId), any(Pageable.class))).thenReturn(offerPage);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(bank));
        
        OfferHistoryResponse response = offerHistoryService.getOfferHistory(borrowerId, 20, 0, "createdAt");
        
        assertNotNull(response);
        assertEquals(2, response.getTotalCount());
        verify(offerRepository, times(1)).findOffersByBorrowerId(eq(borrowerId), any(Pageable.class));
    }

    @Test
    void testGetOfferHistory_MaxLimitEnforced() {
        UUID borrowerId = UUID.randomUUID();
        Page<Offer> offerPage = new PageImpl<>(Arrays.asList());
        when(offerRepository.findOffersByBorrowerId(eq(borrowerId), any(Pageable.class))).thenReturn(offerPage);
        
        OfferHistoryResponse response = offerHistoryService.getOfferHistory(borrowerId, 200, 0, "createdAt");
        
        assertEquals(100, response.getLimit());
    }
}

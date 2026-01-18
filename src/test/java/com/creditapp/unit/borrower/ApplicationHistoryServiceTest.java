package com.creditapp.unit.borrower;

import com.creditapp.borrower.dto.ApplicationHistoryRequest;
import com.creditapp.borrower.dto.ApplicationHistoryResponse;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.borrower.service.ApplicationHistoryService;
import com.creditapp.bank.repository.OfferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationHistoryServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private OfferRepository offerRepository;
    @InjectMocks
    private ApplicationHistoryService applicationHistoryService;

    @Test
    void testGetApplicationHistory_ReturnsApplications() {
        UUID borrowerId = UUID.randomUUID();
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setBorrowerId(borrowerId);
        app.setStatus(ApplicationStatus.ACCEPTED);
        app.setLoanAmount(BigDecimal.valueOf(25000));
        app.setLoanTermMonths(36);
        app.setLoanType("PERSONAL");
        app.setCreatedAt(LocalDateTime.now());
        
        List<Application> applications = Arrays.asList(app);
        Page<Application> applicationPage = new PageImpl<>(applications);
        
        when(applicationRepository.findByBorrowerId(eq(borrowerId), any(Pageable.class))).thenReturn(applicationPage);
        when(offerRepository.findByApplicationId(any())).thenReturn(Arrays.asList());
        
        ApplicationHistoryRequest request = new ApplicationHistoryRequest();
        ApplicationHistoryResponse response = applicationHistoryService.getApplicationHistory(borrowerId, request);
        
        assertNotNull(response);
        assertEquals(1, response.getApplications().size());
    }

    @Test
    void testGetApplicationHistory_FilterByStatus() {
        UUID borrowerId = UUID.randomUUID();
        Application app1 = new Application();
        app1.setId(UUID.randomUUID());
        app1.setBorrowerId(borrowerId);
        app1.setStatus(ApplicationStatus.ACCEPTED);
        app1.setLoanAmount(BigDecimal.valueOf(25000));
        app1.setLoanTermMonths(36);
        app1.setLoanType("PERSONAL");
        app1.setCreatedAt(LocalDateTime.now());
        
        Application app2 = new Application();
        app2.setId(UUID.randomUUID());
        app2.setBorrowerId(borrowerId);
        app2.setStatus(ApplicationStatus.DRAFT);
        app2.setLoanAmount(BigDecimal.valueOf(15000));
        app2.setLoanTermMonths(24);
        app2.setLoanType("PERSONAL");
        app2.setCreatedAt(LocalDateTime.now());
        
        Page<Application> applicationPage = new PageImpl<>(Arrays.asList(app1, app2));
        
        when(applicationRepository.findByBorrowerId(eq(borrowerId), any(Pageable.class))).thenReturn(applicationPage);
        when(offerRepository.findByApplicationId(any())).thenReturn(Arrays.asList());
        
        ApplicationHistoryRequest request = new ApplicationHistoryRequest();
        request.setStatus("ACCEPTED");
        ApplicationHistoryResponse response = applicationHistoryService.getApplicationHistory(borrowerId, request);
        
        assertEquals(1, response.getApplications().size());
        assertEquals("ACCEPTED", response.getApplications().get(0).getStatus());
    }
}

package com.creditapp.unit.borrower;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.creditapp.borrower.controller.BorrowerOfferController;
import com.creditapp.borrower.dto.OfferComparisonTableRequest;
import com.creditapp.borrower.dto.OfferComparisonTableResponse;
import com.creditapp.borrower.dto.OfferComparisonTableRow;
import com.creditapp.borrower.service.OfferComparisonTableService;
import com.creditapp.shared.security.AuthorizationService;

@ExtendWith(MockitoExtension.class)
class OfferComparisonTableControllerTest {
    
    @Mock
    private OfferComparisonTableService offerComparisonTableService;
    
    @Mock
    private AuthorizationService authorizationService;
    
    @InjectMocks
    private BorrowerOfferController borrowerOfferController;
    
    private UUID applicationId;
    private UUID borrowerId;
    private OfferComparisonTableResponse mockResponse;
    
    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
        mockResponse = createMockResponse();
    }
    
    @Test
    void testGetOffersTable_Success() {
        when(authorizationService.getCurrentUserId()).thenReturn(borrowerId);
        when(offerComparisonTableService.getOffersTable(eq(applicationId), eq(borrowerId), any(OfferComparisonTableRequest.class)))
            .thenReturn(mockResponse);
        
        ResponseEntity<OfferComparisonTableResponse> response = borrowerOfferController.getOffersTable(
            applicationId, "apr", "asc", 20, 0, null, null, null, null, null, "full"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getOffers().size());
    }
    
    @Test
    void testGetOffersTable_WithFilters() {
        when(authorizationService.getCurrentUserId()).thenReturn(borrowerId);
        when(offerComparisonTableService.getOffersTable(eq(applicationId), eq(borrowerId), any(OfferComparisonTableRequest.class)))
            .thenReturn(mockResponse);
        
        ResponseEntity<OfferComparisonTableResponse> response = borrowerOfferController.getOffersTable(
            applicationId, "apr", "asc", 20, 0, 
            new BigDecimal("8.0"), new BigDecimal("9.0"), null, null, null, "full"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    void testGetOffersTable_WithSort() {
        when(authorizationService.getCurrentUserId()).thenReturn(borrowerId);
        when(offerComparisonTableService.getOffersTable(eq(applicationId), eq(borrowerId), any(OfferComparisonTableRequest.class)))
            .thenReturn(mockResponse);
        
        ResponseEntity<OfferComparisonTableResponse> response = borrowerOfferController.getOffersTable(
            applicationId, "monthlyPayment", "desc", 20, 0, null, null, null, null, null, "full"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void testGetOffersTable_SummaryMode() {
        when(authorizationService.getCurrentUserId()).thenReturn(borrowerId);
        when(offerComparisonTableService.getOffersTable(eq(applicationId), eq(borrowerId), any(OfferComparisonTableRequest.class)))
            .thenReturn(mockResponse);
        
        ResponseEntity<OfferComparisonTableResponse> response = borrowerOfferController.getOffersTable(
            applicationId, "apr", "asc", 20, 0, null, null, null, null, null, "summary"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    private OfferComparisonTableResponse createMockResponse() {
        List<OfferComparisonTableRow> rows = new ArrayList<>();
        
        OfferComparisonTableRow row1 = new OfferComparisonTableRow(
            UUID.randomUUID(), UUID.randomUUID(), "Bank A", "https://bank-a.com/logo.png",
            new BigDecimal("8.5"), new BigDecimal("1489.51"), new BigDecimal("370823.60"),
            new BigDecimal("5000"), new BigDecimal("30000"), 360, 7, 14,
            LocalDateTime.now().plusDays(14), "CALCULATED", "enabled", "5 days"
        );
        rows.add(row1);
        
        OfferComparisonTableRow row2 = new OfferComparisonTableRow(
            UUID.randomUUID(), UUID.randomUUID(), "Bank B", "https://bank-b.com/logo.png",
            new BigDecimal("9.0"), new BigDecimal("1609.25"), new BigDecimal("420052.00"),
            new BigDecimal("5500"), new BigDecimal("32000"), 360, 10, 14,
            LocalDateTime.now().plusDays(14), "CALCULATED", "enabled", "5 days"
        );
        rows.add(row2);
        
        OfferComparisonTableResponse response = new OfferComparisonTableResponse(
            rows, 2, 20, 0, "apr", "asc", new java.util.HashMap<>()
        );
        return response;
    }
}

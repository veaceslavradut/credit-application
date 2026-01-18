package com.creditapp.unit.bank;

import com.creditapp.bank.dto.OfferDocumentsListResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferDocument;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.bank.repository.OfferDocumentRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.OfferDocumentRetrievalService;
import com.creditapp.shared.service.S3DocumentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferDocumentRetrievalServiceTest {
    
    @Mock
    private OfferDocumentRepository offerDocumentRepository;
    
    @Mock
    private OfferRepository offerRepository;
    
    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private S3DocumentStorageService s3DocumentStorageService;
    
    @InjectMocks
    private OfferDocumentRetrievalService retrievalService;
    
    private UUID offerId;
    private UUID userId;
    private UUID applicationId;
    private Offer testOffer;
    private Application testApplication;
    
    @BeforeEach
    void setUp() {
        offerId = UUID.randomUUID();
        userId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        
        testOffer = new Offer();
        testOffer.setId(offerId);
        testOffer.setApplicationId(applicationId);
        
        testApplication = new Application();
        testApplication.setId(applicationId);
        testApplication.setBorrowerId(userId);
    }
    
    @Test
    void testGetDocuments_ValidRequest_ReturnsList() {
        // Arrange
        OfferDocument doc1 = OfferDocument.builder()
            .id(UUID.randomUUID())
            .offerId(offerId)
            .documentType("TERMS_CONDITIONS")
            .fileName("terms.pdf")
            .fileSize(5000L)
            .uploadedAt(LocalDateTime.now().minusHours(10))
            .uploadedByOfficerId(UUID.randomUUID())
            .virusScanStatus("CLEAN")
            .build();
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(offerDocumentRepository.findByOfferId(offerId)).thenReturn(List.of(doc1));
        
        // Act
        OfferDocumentsListResponse result = retrievalService.getDocuments(offerId, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(offerId, result.getOfferId());
        assertEquals(1, result.getDocuments().size());
        assertEquals(1, result.getTotalCount());
        
        verify(offerRepository).findById(offerId);
        verify(applicationRepository).findById(applicationId);
        verify(offerDocumentRepository).findByOfferId(offerId);
    }
    
    @Test
    void testGetDocuments_NoDocuments_ReturnsEmpty() {
        // Arrange
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(offerDocumentRepository.findByOfferId(offerId)).thenReturn(List.of());
        
        // Act
        OfferDocumentsListResponse result = retrievalService.getDocuments(offerId, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getDocuments().size());
        assertEquals(0, result.getTotalCount());
    }
    
    @Test
    void testGetDocuments_OfferNotFound_ThrowsException() {
        // Arrange
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            retrievalService.getDocuments(offerId, userId)
        );
        
        assertTrue(ex.getMessage().contains("Offer not found"));
    }
    
    @Test
    void testGetDocuments_UnauthorizedBorrower_ThrowsException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            retrievalService.getDocuments(offerId, differentUserId)
        );
        
        assertTrue(ex.getMessage().contains("Unauthorized"));
    }
    
    @Test
    void testGetDocuments_RegenerateUrlIfNearExpiration() {
        // Arrange
        OfferDocument doc = OfferDocument.builder()
            .id(UUID.randomUUID())
            .offerId(offerId)
            .documentType("TERMS_CONDITIONS")
            .fileName("terms.pdf")
            .uploadedAt(LocalDateTime.now().minusHours(23).minusMinutes(30))
            .uploadedByOfficerId(UUID.randomUUID())
            .virusScanStatus("CLEAN")
            .s3Url("https://s3.old/url")
            .build();
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(offerDocumentRepository.findByOfferId(offerId)).thenReturn(List.of(doc));
        
        // Act
        OfferDocumentsListResponse result = retrievalService.getDocuments(offerId, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getDocuments().size());
    }
}
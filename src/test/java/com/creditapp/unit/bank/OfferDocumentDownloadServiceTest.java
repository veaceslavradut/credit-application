package com.creditapp.unit.bank;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferDocument;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.bank.repository.OfferDocumentRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.OfferDocumentDownloadService;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferDocumentDownloadServiceTest {
    
    @Mock
    private OfferDocumentRepository offerDocumentRepository;
    
    @Mock
    private OfferRepository offerRepository;
    
    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private OfferDocumentDownloadService downloadService;
    
    private UUID offerId;
    private UUID documentId;
    private UUID userId;
    private UUID applicationId;
    private Offer testOffer;
    private Application testApplication;
    
    @BeforeEach
    void setUp() {
        offerId = UUID.randomUUID();
        documentId = UUID.randomUUID();
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
    void testGetDownloadUrl_ValidRequest_ReturnsUrl() {
        // Arrange
        OfferDocument doc = OfferDocument.builder()
            .id(documentId)
            .offerId(offerId)
            .documentType("TERMS_CONDITIONS")
            .fileName("terms.pdf")
            .s3Url("https://s3.amazonaws.com/presigned-url")
            .virusScanStatus("CLEAN")
            .build();
        
        when(offerDocumentRepository.findByIdAndOfferId(documentId, offerId))
            .thenReturn(Optional.of(doc));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act
        String result = downloadService.getDownloadUrl(offerId, documentId, userId);
        
        // Assert
        assertEquals("https://s3.amazonaws.com/presigned-url", result);
        
        verify(offerDocumentRepository).findByIdAndOfferId(documentId, offerId);
        verify(offerRepository).findById(offerId);
        verify(applicationRepository).findById(applicationId);
        verify(auditService).logActionWithValues(
            eq("OfferDocument"),
            eq(documentId),
            eq(AuditAction.DOCUMENT_DOWNLOADED),
            any(),
            any()
        );
    }
    
    @Test
    void testGetDownloadUrl_DocumentNotFound_ThrowsException() {
        // Arrange
        when(offerDocumentRepository.findByIdAndOfferId(documentId, offerId))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(offerId, documentId, userId)
        );
        
        assertTrue(ex.getMessage().contains("Document not found"));
    }
    
    @Test
    void testGetDownloadUrl_OfferNotFound_ThrowsException() {
        // Arrange
        OfferDocument doc = OfferDocument.builder()
            .id(documentId)
            .offerId(offerId)
            .virusScanStatus("CLEAN")
            .build();
        
        when(offerDocumentRepository.findByIdAndOfferId(documentId, offerId))
            .thenReturn(Optional.of(doc));
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(offerId, documentId, userId)
        );
        
        assertTrue(ex.getMessage().contains("Offer not found"));
    }
    
    @Test
    void testGetDownloadUrl_UnauthorizedBorrower_ThrowsException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        OfferDocument doc = OfferDocument.builder()
            .id(documentId)
            .offerId(offerId)
            .virusScanStatus("CLEAN")
            .build();
        
        when(offerDocumentRepository.findByIdAndOfferId(documentId, offerId))
            .thenReturn(Optional.of(doc));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(offerId, documentId, differentUserId)
        );
        
        assertTrue(ex.getMessage().contains("Unauthorized"));
    }
    
    @Test
    void testGetDownloadUrl_VirusInfected_ThrowsException() {
        // Arrange
        OfferDocument doc = OfferDocument.builder()
            .id(documentId)
            .offerId(offerId)
            .virusScanStatus("INFECTED")
            .virusScanResult("Detected: Malware.Generic")
            .build();
        
        when(offerDocumentRepository.findByIdAndOfferId(documentId, offerId))
            .thenReturn(Optional.of(doc));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(offerId, documentId, userId)
        );
        
        assertTrue(ex.getMessage().contains("infected"));
    }
    
    @Test
    void testGetDownloadUrl_VirusScanPending_ThrowsException() {
        // Arrange
        OfferDocument doc = OfferDocument.builder()
            .id(documentId)
            .offerId(offerId)
            .virusScanStatus("PENDING")
            .build();
        
        when(offerDocumentRepository.findByIdAndOfferId(documentId, offerId))
            .thenReturn(Optional.of(doc));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(offerId, documentId, userId)
        );
        
        assertTrue(ex.getMessage().contains("pending"));
    }
    
    @Test
    void testGetDownloadUrl_AuditLogged() {
        // Arrange
        OfferDocument doc = OfferDocument.builder()
            .id(documentId)
            .offerId(offerId)
            .documentType("DISCLOSURE")
            .fileName("disclosure.pdf")
            .s3Url("https://s3.amazonaws.com/url")
            .virusScanStatus("CLEAN")
            .build();
        
        when(offerDocumentRepository.findByIdAndOfferId(documentId, offerId))
            .thenReturn(Optional.of(doc));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act
        downloadService.getDownloadUrl(offerId, documentId, userId);
        
        // Assert - verify audit log includes document context
        verify(auditService).logActionWithValues(
            eq("OfferDocument"),
            eq(documentId),
            eq(AuditAction.DOCUMENT_DOWNLOADED),
            any(),
            any()
        );
    }
}
package com.creditapp.integration.bank;

import com.creditapp.bank.dto.OfferDocumentMetadata;
import com.creditapp.bank.dto.OfferDocumentsListResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferDocument;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.bank.repository.OfferDocumentRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.OfferDocumentDownloadService;
import com.creditapp.bank.service.OfferDocumentRetrievalService;
import com.creditapp.bank.service.OfferDocumentUploadService;
import com.creditapp.shared.service.FileValidationService;
import com.creditapp.shared.service.S3DocumentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OfferDocumentIntegrationTest {
    
    @Autowired
    private OfferDocumentUploadService uploadService;
    
    @Autowired
    private OfferDocumentRetrievalService retrievalService;
    
    @Autowired
    private OfferDocumentDownloadService downloadService;
    
    @Autowired
    private OfferRepository offerRepository;
    
    @Autowired
    private OfferDocumentRepository documentRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    private Application testApplication;
    private Offer testOffer;
    private UUID bankId;
    private UUID officerId;
    
    @BeforeEach
    void setUp() {
        // Create test data
        bankId = UUID.randomUUID();
        officerId = UUID.randomUUID();
        
        testApplication = new Application();
        testApplication.setBorrowerId(UUID.randomUUID());
        testApplication = applicationRepository.save(testApplication);
        
        testOffer = new Offer();
        testOffer.setApplicationId(testApplication.getId());
        testOffer.setBankId(bankId);
        testOffer = offerRepository.save(testOffer);
    }
    
    @Test
    void testUploadDocument_ValidFile_PersistsToDatabase() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF content".getBytes()
        );
        
        var request = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file)
            .documentType("TERMS_CONDITIONS")
            .description("Loan terms")
            .build();
        
        // Act
        OfferDocumentMetadata result = uploadService.uploadDocument(
            testOffer.getId(), bankId, officerId, request
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(testOffer.getId(), result.getOfferId());
        assertEquals("TERMS_CONDITIONS", result.getDocumentType());
        assertEquals("terms.pdf", result.getFileName());
        assertEquals("CLEAN", result.getVirusScanStatus());
        
        // Verify persisted in database
        Optional<OfferDocument> savedDoc = documentRepository.findById(result.getDocumentId());
        assertTrue(savedDoc.isPresent());
        assertEquals("TERMS_CONDITIONS", savedDoc.get().getDocumentType());
    }
    
    @Test
    void testUploadMultipleDocuments_AllPersisted() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF content".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "disclosure.pdf", "application/pdf", "PDF content".getBytes()
        );
        
        // Act
        var request1 = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file1)
            .documentType("TERMS_CONDITIONS")
            .build();
        
        var request2 = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file2)
            .documentType("DISCLOSURE")
            .build();
        
        OfferDocumentMetadata result1 = uploadService.uploadDocument(
            testOffer.getId(), bankId, officerId, request1
        );
        OfferDocumentMetadata result2 = uploadService.uploadDocument(
            testOffer.getId(), bankId, officerId, request2
        );
        
        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getDocumentId(), result2.getDocumentId());
        
        // Verify both documents in database
        var documents = documentRepository.findByOfferId(testOffer.getId());
        assertEquals(2, documents.size());
    }
    
    @Test
    void testGetDocuments_ReturnsAllDocumentsForOffer() throws Exception {
        // Arrange - upload two documents
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "disclosure.pdf", "application/pdf", "PDF".getBytes()
        );
        
        var request1 = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file1)
            .documentType("TERMS_CONDITIONS")
            .build();
        
        var request2 = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file2)
            .documentType("DISCLOSURE")
            .build();
        
        uploadService.uploadDocument(testOffer.getId(), bankId, officerId, request1);
        uploadService.uploadDocument(testOffer.getId(), bankId, officerId, request2);
        
        // Act
        OfferDocumentsListResponse result = retrievalService.getDocuments(
            testOffer.getId(), testApplication.getBorrowerId()
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(testOffer.getId(), result.getOfferId());
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getDocuments().size());
    }
    
    @Test
    void testGetDocuments_OnlyAuthorizedBorrowerCanAccess() throws Exception {
        // Arrange - upload document
        MockMultipartFile file = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF".getBytes()
        );
        
        var request = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file)
            .documentType("TERMS_CONDITIONS")
            .build();
        
        uploadService.uploadDocument(testOffer.getId(), bankId, officerId, request);
        
        // Act & Assert - authorized borrower can access
        OfferDocumentsListResponse result = retrievalService.getDocuments(
            testOffer.getId(), testApplication.getBorrowerId()
        );
        assertEquals(1, result.getDocuments().size());
        
        // Act & Assert - different borrower cannot access
        UUID differentBorrowerId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
            retrievalService.getDocuments(testOffer.getId(), differentBorrowerId)
        );
    }
    
    @Test
    void testDownloadDocument_ValidDocument_ReturnsPresignedUrl() throws Exception {
        // Arrange - upload document
        MockMultipartFile file = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF".getBytes()
        );
        
        var request = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file)
            .documentType("TERMS_CONDITIONS")
            .build();
        
        OfferDocumentMetadata uploadResult = uploadService.uploadDocument(
            testOffer.getId(), bankId, officerId, request
        );
        
        // Act
        String downloadUrl = downloadService.getDownloadUrl(
            testOffer.getId(), uploadResult.getDocumentId(), testApplication.getBorrowerId()
        );
        
        // Assert
        assertNotNull(downloadUrl);
        assertEquals("https://s3.amazonaws.com/presigned", downloadUrl);
    }
    
    @Test
    void testDownloadDocument_InfectedDocument_Blocked() throws Exception {
        // Arrange - create document with INFECTED status
        OfferDocument doc = OfferDocument.builder()
            .id(UUID.randomUUID())
            .offerId(testOffer.getId())
            .documentType("MALICIOUS")
            .fileName("virus.exe")
            .fileSize(1000L)
            .mimeType("application/x-msdownload")
            .s3Key("offers/" + testOffer.getId() + "/virus.exe")
            .s3Url("https://s3.amazonaws.com/virus")
            .uploadedByOfficerId(officerId)
            .uploadedAt(LocalDateTime.now())
            .virusScanStatus("INFECTED")
            .virusScanResult("Detected: Win32.Malware")
            .build();
        
        documentRepository.save(doc);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(testOffer.getId(), doc.getId(), testApplication.getBorrowerId())
        );
    }
    
    @Test
    void testDownloadDocument_PendingVirusScan_Blocked() throws Exception {
        // Arrange - create document with PENDING status
        OfferDocument doc = OfferDocument.builder()
            .id(UUID.randomUUID())
            .offerId(testOffer.getId())
            .documentType("SUSPICIOUS")
            .fileName("unknown.pdf")
            .fileSize(1000L)
            .mimeType("application/pdf")
            .s3Key("offers/" + testOffer.getId() + "/unknown.pdf")
            .s3Url("https://s3.amazonaws.com/unknown")
            .uploadedByOfficerId(officerId)
            .uploadedAt(LocalDateTime.now())
            .virusScanStatus("PENDING")
            .build();
        
        documentRepository.save(doc);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(testOffer.getId(), doc.getId(), testApplication.getBorrowerId())
        );
    }
    
    @Test
    void testDownloadDocument_UnauthorizedBorrower_Blocked() throws Exception {
        // Arrange - upload document
        MockMultipartFile file = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF".getBytes()
        );
        
        var request = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file)
            .documentType("TERMS_CONDITIONS")
            .build();
        
        OfferDocumentMetadata uploadResult = uploadService.uploadDocument(
            testOffer.getId(), bankId, officerId, request
        );
        
        // Act & Assert - different borrower cannot download
        UUID differentBorrowerId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
            downloadService.getDownloadUrl(testOffer.getId(), uploadResult.getDocumentId(), differentBorrowerId)
        );
    }
    
    @Test
    void testDocumentsSortedByUploadDate_NewestFirst() throws Exception {
        // Arrange - upload documents with delays
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "doc1.pdf", "application/pdf", "PDF".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "doc2.pdf", "application/pdf", "PDF".getBytes()
        );
        
        var request1 = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file1)
            .documentType("DOC1")
            .build();
        
        var request2 = com.creditapp.bank.dto.OfferDocumentUploadRequest.builder()
            .offerId(testOffer.getId())
            .file(file2)
            .documentType("DOC2")
            .build();
        
        uploadService.uploadDocument(testOffer.getId(), bankId, officerId, request1);
        Thread.sleep(100); // Small delay to ensure different timestamps
        uploadService.uploadDocument(testOffer.getId(), bankId, officerId, request2);
        
        // Act
        OfferDocumentsListResponse result = retrievalService.getDocuments(
            testOffer.getId(), testApplication.getBorrowerId()
        );
        
        // Assert - most recent document should be first
        assertEquals(2, result.getDocuments().size());
        assertEquals("DOC2", result.getDocuments().get(0).getDocumentType());
        assertEquals("DOC1", result.getDocuments().get(1).getDocumentType());
    }
}
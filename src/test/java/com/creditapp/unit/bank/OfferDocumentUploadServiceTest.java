package com.creditapp.unit.bank;

import com.creditapp.bank.dto.OfferDocumentMetadata;
import com.creditapp.bank.dto.OfferDocumentUploadRequest;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferDocument;
import com.creditapp.bank.repository.OfferDocumentRepository;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.OfferDocumentUploadService;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.FileValidationService;
import com.creditapp.shared.service.S3DocumentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferDocumentUploadServiceTest {
    
    @Mock
    private OfferDocumentRepository offerDocumentRepository;
    
    @Mock
    private OfferRepository offerRepository;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private S3DocumentStorageService s3DocumentStorageService;
    
    @Mock
    private FileValidationService fileValidationService;
    
    @Mock
    private MultipartFile mockFile;
    
    @InjectMocks
    private OfferDocumentUploadService uploadService;
    
    private UUID offerId;
    private UUID bankId;
    private UUID officerId;
    private Offer testOffer;
    
    @BeforeEach
    void setUp() {
        offerId = UUID.randomUUID();
        bankId = UUID.randomUUID();
        officerId = UUID.randomUUID();
        
        testOffer = new Offer();
        testOffer.setId(offerId);
        testOffer.setBankId(bankId);
        testOffer.setApplicationId(UUID.randomUUID());
    }
    
    @Test
    void testUploadDocument_ValidRequest_Success() throws IOException {
        // Arrange
        when(mockFile.getOriginalFilename()).thenReturn("terms.pdf");
        when(mockFile.getSize()).thenReturn(5000L);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[5000]));
        
        OfferDocumentUploadRequest request = OfferDocumentUploadRequest.builder()
            .offerId(offerId)
            .file(mockFile)
            .documentType("TERMS_CONDITIONS")
            .description("Loan terms")
            .build();
        
        when(fileValidationService.validateFile(mockFile))
            .thenReturn(FileValidationService.ValidationResult.valid());
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        
        S3DocumentStorageService.S3ObjectMetadata s3Metadata = 
            new S3DocumentStorageService.S3ObjectMetadata("bucket", "key", "etag", "version");
        when(s3DocumentStorageService.uploadFile(anyString(), any(), anyString(), any()))
            .thenReturn(s3Metadata);
        
        when(s3DocumentStorageService.generatePresignedUrl(anyString(), any()))
            .thenReturn("https://s3.amazonaws.com/presigned-url");
        
        OfferDocument savedDocument = OfferDocument.builder()
            .id(UUID.randomUUID())
            .offerId(offerId)
            .documentType("TERMS_CONDITIONS")
            .fileName("terms.pdf")
            .fileSize(5000L)
            .mimeType("application/pdf")
            .s3Key("offers/" + offerId + "/TERMS_CONDITIONS_123_terms.pdf")
            .s3Url("https://s3.amazonaws.com/presigned-url")
            .uploadedByOfficerId(officerId)
            .virusScanStatus("CLEAN")
            .build();
        
        when(offerDocumentRepository.save(any(OfferDocument.class))).thenReturn(savedDocument);
        
        // Act
        OfferDocumentMetadata result = uploadService.uploadDocument(offerId, bankId, officerId, request);
        
        // Assert
        assertNotNull(result);
        assertEquals(offerId, result.getOfferId());
        assertEquals("TERMS_CONDITIONS", result.getDocumentType());
        assertEquals("terms.pdf", result.getFileName());
        assertEquals("CLEAN", result.getVirusScanStatus());
        assertEquals("https://s3.amazonaws.com/presigned-url", result.getDownloadUrl());
        
        verify(fileValidationService).validateFile(mockFile);
        verify(offerRepository).findById(offerId);
        verify(s3DocumentStorageService).uploadFile(anyString(), any(), anyString(), any());
        verify(s3DocumentStorageService).generatePresignedUrl(anyString(), any());
        verify(offerDocumentRepository).save(any(OfferDocument.class));
        verify(auditService).logActionWithValues(eq("OfferDocument"), any(), eq(AuditAction.DOCUMENT_UPLOADED), any(), any());
    }
    
    @Test
    void testUploadDocument_InvalidFileType_ThrowsException() {
        // Arrange
        OfferDocumentUploadRequest request = OfferDocumentUploadRequest.builder()
            .offerId(offerId)
            .file(mockFile)
            .documentType("CUSTOM")
            .build();
        
        when(fileValidationService.validateFile(mockFile))
            .thenReturn(FileValidationService.ValidationResult.invalid("File type not allowed"));
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            uploadService.uploadDocument(offerId, bankId, officerId, request)
        );
        
        assertTrue(ex.getMessage().contains("File validation failed"));
        verify(fileValidationService).validateFile(mockFile);
        verify(offerRepository, never()).findById(any());
    }
    
    @Test
    void testUploadDocument_FileSizeExceeded_ThrowsException() {
        // Arrange
        OfferDocumentUploadRequest request = OfferDocumentUploadRequest.builder()
            .offerId(offerId)
            .file(mockFile)
            .documentType("CUSTOM")
            .build();
        
        when(fileValidationService.validateFile(mockFile))
            .thenReturn(FileValidationService.ValidationResult.invalid("File size exceeds 10MB limit"));
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            uploadService.uploadDocument(offerId, bankId, officerId, request)
        );
        
        assertTrue(ex.getMessage().contains("File size"));
    }
    
    @Test
    void testUploadDocument_OfferNotFound_ThrowsException() {
        // Arrange
        OfferDocumentUploadRequest request = OfferDocumentUploadRequest.builder()
            .offerId(offerId)
            .file(mockFile)
            .documentType("CUSTOM")
            .build();
        
        when(fileValidationService.validateFile(mockFile))
            .thenReturn(FileValidationService.ValidationResult.valid());
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            uploadService.uploadDocument(offerId, bankId, officerId, request)
        );
        
        assertTrue(ex.getMessage().contains("Offer not found"));
    }
    
    @Test
    void testUploadDocument_UnauthorizedBank_ThrowsException() {
        // Arrange
        UUID differentBankId = UUID.randomUUID();
        testOffer.setBankId(differentBankId);
        
        OfferDocumentUploadRequest request = OfferDocumentUploadRequest.builder()
            .offerId(offerId)
            .file(mockFile)
            .documentType("CUSTOM")
            .build();
        
        when(fileValidationService.validateFile(mockFile))
            .thenReturn(FileValidationService.ValidationResult.valid());
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            uploadService.uploadDocument(offerId, bankId, officerId, request)
        );
        
        assertTrue(ex.getMessage().contains("Bank does not own"));
    }
    
    @Test
    void testUploadDocument_AuditLogged() throws IOException {
        // Arrange - same as success test
        when(mockFile.getOriginalFilename()).thenReturn("terms.pdf");
        when(mockFile.getSize()).thenReturn(5000L);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[5000]));
        
        OfferDocumentUploadRequest request = OfferDocumentUploadRequest.builder()
            .offerId(offerId)
            .file(mockFile)
            .documentType("TERMS_CONDITIONS")
            .build();
        
        when(fileValidationService.validateFile(mockFile))
            .thenReturn(FileValidationService.ValidationResult.valid());
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(s3DocumentStorageService.uploadFile(anyString(), any(), anyString(), any()))
            .thenReturn(new S3DocumentStorageService.S3ObjectMetadata("bucket", "key", "etag", "v"));
        when(s3DocumentStorageService.generatePresignedUrl(anyString(), any()))
            .thenReturn("https://s3.amazonaws.com/url");
        
        OfferDocument savedDoc = OfferDocument.builder()
            .id(UUID.randomUUID())
            .offerId(offerId)
            .documentType("TERMS_CONDITIONS")
            .fileName("terms.pdf")
            .fileSize(5000L)
            .mimeType("application/pdf")
            .s3Key("key")
            .s3Url("url")
            .uploadedByOfficerId(officerId)
            .virusScanStatus("CLEAN")
            .build();
        
        when(offerDocumentRepository.save(any())).thenReturn(savedDoc);
        
        // Act
        uploadService.uploadDocument(offerId, bankId, officerId, request);
        
        // Assert - verify audit was called with DOCUMENT_UPLOADED
        verify(auditService).logActionWithValues(
            eq("OfferDocument"),
            eq(savedDoc.getId()),
            eq(AuditAction.DOCUMENT_UPLOADED),
            any(),
            any()
        );
    }
}
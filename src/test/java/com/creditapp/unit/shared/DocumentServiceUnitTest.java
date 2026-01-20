package com.creditapp.unit.shared;

import com.creditapp.shared.model.Document;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.model.SignatureLog;
import com.creditapp.shared.model.SignatureStatus;
import com.creditapp.shared.repository.DocumentRepository;
import com.creditapp.shared.repository.SignatureLogRepository;
import com.creditapp.shared.service.DocumentService;
import com.creditapp.shared.service.DocumentUploadService;
import com.creditapp.shared.service.ESignatureService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceUnitTest {
    
    @Mock
    private DocumentRepository documentRepository;
    
    @Mock
    private SignatureLogRepository signatureLogRepository;
    
    @InjectMocks
    private DocumentService documentService;
    
    private UUID applicationId;
    private UUID documentId;
    private Document testDocument;
    
    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        documentId = UUID.randomUUID();
        
        testDocument = Document.builder()
                .id(documentId)
                .applicationId(applicationId)
                .documentType(DocumentType.LOAN_AGREEMENT)
                .fileUrl("s3://bucket/app-123/LOAN_AGREEMENT/doc.pdf")
                .documentName("Loan Agreement")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void testFindByApplicationId() {
        // Arrange
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findByApplicationId(applicationId)).thenReturn(documents);
        
        // Act
        List<Document> result = documentService.findByApplicationId(applicationId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDocument.getId(), result.get(0).getId());
        verify(documentRepository, times(1)).findByApplicationId(applicationId);
    }
    
    @Test
    void testFindByApplicationIdAndType() {
        // Arrange
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findByApplicationIdAndDocumentType(applicationId, DocumentType.LOAN_AGREEMENT))
                .thenReturn(documents);
        
        // Act
        List<Document> result = documentService.findByApplicationIdAndType(applicationId, DocumentType.LOAN_AGREEMENT);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(DocumentType.LOAN_AGREEMENT, result.get(0).getDocumentType());
    }
    
    @Test
    void testFindById() {
        // Arrange
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));
        
        // Act
        Optional<Document> result = documentService.findById(documentId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(documentId, result.get().getId());
    }
    
    @Test
    void testSaveDocument() {
        // Arrange
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // Act
        Document result = documentService.save(testDocument);
        
        // Assert
        assertNotNull(result);
        assertEquals(documentId, result.getId());
        verify(documentRepository, times(1)).save(testDocument);
    }
    
    @Test
    void testDeleteDocument() {
        // Act
        documentService.delete(documentId);
        
        // Assert
        verify(documentRepository, times(1)).deleteById(documentId);
    }
}
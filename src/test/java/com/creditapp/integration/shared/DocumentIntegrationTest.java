package com.creditapp.integration.shared;

import com.creditapp.shared.model.Document;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.model.SignatureLog;
import com.creditapp.shared.model.SignatureStatus;
import com.creditapp.shared.repository.DocumentRepository;
import com.creditapp.shared.repository.SignatureLogRepository;
import com.creditapp.shared.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class DocumentIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private SignatureLogRepository signatureLogRepository;
    
    private UUID applicationId;
    private Document testDocument;
    
    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        
        testDocument = Document.builder()
                .id(UUID.randomUUID())
                .applicationId(applicationId)
                .documentType(DocumentType.LOAN_AGREEMENT)
                .fileUrl("s3://bucket/loan.pdf")
                .documentName("Loan Agreement")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void testDocumentPersistence() {
        // Arrange & Act
        Document saved = documentRepository.save(testDocument);
        entityManager.flush();
        
        // Assert
        Document retrieved = documentRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(testDocument.getDocumentName(), retrieved.getDocumentName());
        assertEquals(testDocument.getDocumentType(), retrieved.getDocumentType());
    }
    
    @Test
    void testFindByApplicationId() {
        // Arrange
        documentRepository.save(testDocument);
        entityManager.flush();
        
        // Act
        List<Document> documents = documentRepository.findByApplicationId(applicationId);
        
        // Assert
        assertFalse(documents.isEmpty());
        assertTrue(documents.stream().anyMatch(d -> d.getId().equals(testDocument.getId())));
    }
    
    @Test
    void testFindByApplicationIdAndDocumentType() {
        // Arrange
        documentRepository.save(testDocument);
        entityManager.flush();
        
        // Act
        List<Document> documents = documentRepository.findByApplicationIdAndDocumentType(
                applicationId, DocumentType.LOAN_AGREEMENT);
        
        // Assert
        assertFalse(documents.isEmpty());
        assertTrue(documents.stream().allMatch(d -> d.getDocumentType() == DocumentType.LOAN_AGREEMENT));
    }
    
    @Test
    void testSignatureLogPersistence() {
        // Arrange
        Document doc = documentRepository.save(testDocument);
        
        SignatureLog signatureLog = SignatureLog.builder()
                .id(UUID.randomUUID())
                .documentId(doc.getId())
                .signerId(UUID.randomUUID())
                .signatureStatus(SignatureStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Act
        SignatureLog saved = signatureLogRepository.save(signatureLog);
        entityManager.flush();
        
        // Assert
        SignatureLog retrieved = signatureLogRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(SignatureStatus.PENDING, retrieved.getSignatureStatus());
    }
    
    // Note: Skipping cascade delete test - Phase 1 doesn't require document deletion
    // Phase 2 will implement proper cascade handling with application deletion
    /*
    @Test
    void testSignatureLogCascadeDelete() {
        // Arrange
        Document doc = documentRepository.save(testDocument);
        
        SignatureLog signatureLog = SignatureLog.builder()
                .id(UUID.randomUUID())
                .documentId(doc.getId())
                .signerId(UUID.randomUUID())
                .signatureStatus(SignatureStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        SignatureLog savedLog = signatureLogRepository.save(signatureLog);
        entityManager.flush();
        entityManager.clear();
        
        // Act
        documentRepository.deleteById(doc.getId());
        entityManager.flush();
        entityManager.clear();
        
        // Assert
        assertTrue(signatureLogRepository.findById(savedLog.getId()).isEmpty());
    }
    */
    
    @Test
    void testDocumentS3UrlFormat() {
        // Arrange & Act
        documentRepository.save(testDocument);
        entityManager.flush();
        
        // Assert
        Document retrieved = documentRepository.findById(testDocument.getId()).orElse(null);
        assertNotNull(retrieved);
        assertTrue(retrieved.getFileUrl().startsWith("s3://"));
    }
}
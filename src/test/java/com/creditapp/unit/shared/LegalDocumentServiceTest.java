package com.creditapp.unit.shared;

import com.creditapp.shared.dto.LegalDocumentResponse;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.model.LegalDocument;
import com.creditapp.shared.model.LegalStatus;
import com.creditapp.shared.repository.LegalDocumentRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.LegalDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegalDocumentServiceTest {

    @Mock
    private LegalDocumentRepository legalDocumentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private LegalDocumentService legalDocumentService;

    private UUID userId;
    private LegalDocument privacyPolicyV1;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        privacyPolicyV1 = LegalDocument.builder()
            .id(UUID.randomUUID())
            .documentType(DocumentType.PRIVACY_POLICY)
            .version(1)
            .content("Privacy Policy: Data Collected. Purpose of use. Retention for 3 years. Contact: support@example.com")
            .contentHash("hash123")
            .language("en")
            .status(LegalStatus.PUBLISHED)
            .updatedAt(LocalDateTime.now())
            .updatedBy(userId)
            .build();
    }

    @Test
    void testGetPublishedDocument() {
        when(legalDocumentRepository.findLatestPublishedByType(DocumentType.PRIVACY_POLICY, "en"))
            .thenReturn(Optional.of(privacyPolicyV1));

        LegalDocumentResponse response = legalDocumentService.getPublishedDocument(DocumentType.PRIVACY_POLICY, "en");

        assertNotNull(response);
        assertEquals(DocumentType.PRIVACY_POLICY, response.getDocumentType());
        assertEquals(1, response.getVersion());
        assertNotNull(response.getContent());
        
        verify(legalDocumentRepository, times(1)).findLatestPublishedByType(DocumentType.PRIVACY_POLICY, "en");
    }

    @Test
    void testGetPublishedDocumentNotFound() {
        when(legalDocumentRepository.findLatestPublishedByType(DocumentType.PRIVACY_POLICY, "en"))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            legalDocumentService.getPublishedDocument(DocumentType.PRIVACY_POLICY, "en"));
    }

    @Test
    void testUpdateDocumentWithNewVersion() {
        String newContent = "Updated Privacy Policy: Data Collected. Purpose. Retention for 3 years. Contact: support@example.com";
        
        when(legalDocumentRepository.findAllByDocumentTypeOrderByVersionDesc(DocumentType.PRIVACY_POLICY))
            .thenReturn(List.of(privacyPolicyV1));
        when(legalDocumentRepository.save(any(LegalDocument.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        LegalDocumentResponse response = legalDocumentService.updateDocument(
            DocumentType.PRIVACY_POLICY, newContent, false, userId);

        assertNotNull(response);
        assertEquals(DocumentType.PRIVACY_POLICY, response.getDocumentType());
        assertEquals(2, response.getVersion());
        assertEquals(newContent, response.getContent());
        
        verify(legalDocumentRepository, times(1)).save(any(LegalDocument.class));
        verify(auditService, times(1)).logAction(eq("LegalDocument"), any(UUID.class), eq(AuditAction.LEGAL_DOCUMENT_UPDATED));
    }

    @Test
    void testUpdateDocumentWithMaterialChange() {
        String newContent = "Updated Privacy Policy: Data Collected. Purpose. Retention for 3 years. Contact: support@example.com";
        
        when(legalDocumentRepository.findAllByDocumentTypeOrderByVersionDesc(DocumentType.PRIVACY_POLICY))
            .thenReturn(List.of(privacyPolicyV1));
        when(legalDocumentRepository.save(any(LegalDocument.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        LegalDocumentResponse response = legalDocumentService.updateDocument(
            DocumentType.PRIVACY_POLICY, newContent, true, userId);

        assertNotNull(response);
        verify(auditService, times(1)).logAction(eq("LegalDocument"), any(UUID.class), eq(AuditAction.LEGAL_DOCUMENT_UPDATED));
    }

    @Test
    void testUpdateDocumentInvalidContent_MissingDataCollected() {
        String invalidContent = "Invalid policy without required sections";

        assertThrows(IllegalArgumentException.class, () ->
            legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, invalidContent, false, userId));
    }

    @Test
    void testUpdateDocumentInvalidContent_MissingRetention() {
        String invalidContent = "Privacy Policy: Data Collected. Purpose. Contact: support@example.com";

        assertThrows(IllegalArgumentException.class, () ->
            legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, invalidContent, false, userId));
    }

    @Test
    void testUpdateTermsOfServiceInvalidContent() {
        String invalidContent = "Terms without marketplace clause";

        assertThrows(IllegalArgumentException.class, () ->
            legalDocumentService.updateDocument(DocumentType.TERMS_OF_SERVICE, invalidContent, false, userId));
    }

    @Test
    void testGetAllVersions() {
        LegalDocument v2 = LegalDocument.builder()
            .id(UUID.randomUUID())
            .documentType(DocumentType.PRIVACY_POLICY)
            .version(2)
            .content("Updated Privacy Policy: Data Collected. Purpose. Retention for 3 years. Contact: support@example.com")
            .contentHash("hash456")
            .language("en")
            .status(LegalStatus.PUBLISHED)
            .updatedAt(LocalDateTime.now())
            .build();

        when(legalDocumentRepository.findAllByDocumentTypeOrderByVersionDesc(DocumentType.PRIVACY_POLICY))
            .thenReturn(List.of(v2, privacyPolicyV1));

        List<LegalDocumentResponse> versions = legalDocumentService.getAllVersions(DocumentType.PRIVACY_POLICY);

        assertNotNull(versions);
        assertEquals(2, versions.size());
        assertEquals(2, versions.get(0).getVersion());
        assertEquals(1, versions.get(1).getVersion());
    }
}
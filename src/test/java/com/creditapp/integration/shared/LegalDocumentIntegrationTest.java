package com.creditapp.integration.shared;

import com.creditapp.shared.dto.LegalDocumentResponse;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.model.LegalDocument;
import com.creditapp.shared.model.LegalStatus;
import com.creditapp.shared.repository.LegalDocumentRepository;
import com.creditapp.shared.service.LegalDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LegalDocumentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LegalDocumentService legalDocumentService;

    @Autowired
    private LegalDocumentRepository legalDocumentRepository;

    private String privacyPolicyContent;
    private String termsOfServiceContent;

    @BeforeEach
    void setUp() {
        privacyPolicyContent = """
            # Privacy Policy
            
            ## Data Collected
            We collect name, email, phone, address, and financial data.
            
            ## Purpose
            We use your data for loan application matching and verification.
            
            ## Sharing and Disclosure
            We share data with bank partners and verification services.
            
            ## Retention
            We retain your data for 3 years for audit and compliance purposes.
            
            ## Your Rights
            You have the right to access, deletion, and correction of your data.
            
            ## Contact
            For privacy questions: privacy@example.com
            """;

        termsOfServiceContent = """
            # Terms of Service
            
            ## Platform Role
            This platform operates as a marketplace connecting borrowers and banks.
            
            ## User Obligations
            Users must provide accurate information and comply with applicable laws.
            
            ## Bank Obligations
            Banks must deliver preliminary offers within 24 hours.
            
            ## Limitation of Liability
            The platform is not liable for bank decisions or offer terms.
            
            ## Dispute Resolution
            Any disputes shall be resolved under Moldovan law.
            """;
    }

    @Test
    void testGetPrivacyPolicy() throws Exception {
        legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, privacyPolicyContent, false, null);

        mockMvc.perform(get("/api/legal/privacy-policy")
                .param("language", "en")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentType").value("PRIVACY_POLICY"))
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void testGetTermsOfService() throws Exception {
        legalDocumentService.updateDocument(DocumentType.TERMS_OF_SERVICE, termsOfServiceContent, false, null);

        mockMvc.perform(get("/api/legal/terms-of-service")
                .param("language", "en")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentType").value("TERMS_OF_SERVICE"))
            .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void testUpdatePrivacyPolicyIncrementVersion() {
        LegalDocumentResponse v1 = legalDocumentService.updateDocument(
            DocumentType.PRIVACY_POLICY, privacyPolicyContent, false, null);
        assertEquals(1, v1.getVersion());

        String updatedContent = privacyPolicyContent + "\n\nUpdated on: 2026-01-19";
        LegalDocumentResponse v2 = legalDocumentService.updateDocument(
            DocumentType.PRIVACY_POLICY, updatedContent, false, null);

        assertEquals(2, v2.getVersion());
        assertNotEquals(v1.getContentHash(), v2.getContentHash());
    }

    @Test
    void testDocumentPersistenceInDatabase() {
        legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, privacyPolicyContent, false, null);

        List<LegalDocument> documents = legalDocumentRepository.findAllByDocumentTypeOrderByVersionDesc(DocumentType.PRIVACY_POLICY);
        assertEquals(1, documents.size());

        LegalDocument doc = documents.get(0);
        assertEquals(DocumentType.PRIVACY_POLICY, doc.getDocumentType());
        assertEquals(1, doc.getVersion());
        assertEquals(LegalStatus.PUBLISHED, doc.getStatus());
        assertNotNull(doc.getContentHash());
    }

    @Test
    void testContentHashForTamperDetection() {
        LegalDocumentResponse v1 = legalDocumentService.updateDocument(
            DocumentType.PRIVACY_POLICY, privacyPolicyContent, false, null);

        String v2Content = privacyPolicyContent + "\n\nAdditional section";
        LegalDocumentResponse v2 = legalDocumentService.updateDocument(
            DocumentType.PRIVACY_POLICY, v2Content, false, null);

        assertNotNull(v1.getContentHash());
        assertNotNull(v2.getContentHash());
        assertNotEquals(v1.getContentHash(), v2.getContentHash());
    }

    @Test
    void testInvalidPrivacyPolicyMissingDataCollected() {
        String invalidContent = privacyPolicyContent.replace("## Data Collected", "## Data Info");

        assertThrows(IllegalArgumentException.class, () ->
            legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, invalidContent, false, null));
    }

    @Test
    void testInvalidTermsOfServiceMissingMarketplace() {
        String invalidContent = termsOfServiceContent.replace("marketplace", "platform");

        assertThrows(IllegalArgumentException.class, () ->
            legalDocumentService.updateDocument(DocumentType.TERMS_OF_SERVICE, invalidContent, false, null));
    }

    @Test
    void testEmptyContentValidation() {
        assertThrows(IllegalArgumentException.class, () ->
            legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, "", false, null));
    }

    @Test
    void testGetAllVersionsOrdered() {
        legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, privacyPolicyContent, false, null);
        
        String v2Content = privacyPolicyContent.replace("privacy@example.com", "privacy@updated.com");
        legalDocumentService.updateDocument(DocumentType.PRIVACY_POLICY, v2Content, false, null);

        List<LegalDocumentResponse> versions = legalDocumentService.getAllVersions(DocumentType.PRIVACY_POLICY);

        assertEquals(2, versions.size());
        assertEquals(2, versions.get(0).getVersion());
        assertEquals(1, versions.get(1).getVersion());
    }
}
package com.creditapp.unit.bank;

import com.creditapp.bank.controller.OfferDocumentController;
import com.creditapp.bank.dto.OfferDocumentMetadata;
import com.creditapp.bank.dto.OfferDocumentsListResponse;
import com.creditapp.bank.service.OfferDocumentDownloadService;
import com.creditapp.bank.service.OfferDocumentRetrievalService;
import com.creditapp.bank.service.OfferDocumentUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfferDocumentController.class)
class OfferDocumentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OfferDocumentUploadService uploadService;
    
    @Autowired
    private OfferDocumentRetrievalService retrievalService;
    
    @Autowired
    private OfferDocumentDownloadService downloadService;
    
    @Configuration
    static class TestConfig {
        @Bean
        OfferDocumentUploadService uploadService() {
            return Mockito.mock(OfferDocumentUploadService.class);
        }
        
        @Bean
        OfferDocumentRetrievalService retrievalService() {
            return Mockito.mock(OfferDocumentRetrievalService.class);
        }
        
        @Bean
        OfferDocumentDownloadService downloadService() {
            return Mockito.mock(OfferDocumentDownloadService.class);
        }
    }
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = "BANK_OFFICER")
    void testUploadDocument_ValidRequest_Returns201Created() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        UUID bankId = UUID.randomUUID();
        UUID officerId = UUID.randomUUID();
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF content".getBytes()
        );
        
        OfferDocumentMetadata metadata = OfferDocumentMetadata.builder()
            .documentId(UUID.randomUUID())
            .offerId(offerId)
            .documentType("TERMS_CONDITIONS")
            .fileName("terms.pdf")
            .fileSize(11L)
            .uploadedAt(LocalDateTime.now())
            .uploadedByOfficerId(officerId)
            .uploadedByOfficerName("John Officer")
            .virusScanStatus("CLEAN")
            .downloadUrl("https://s3.amazonaws.com/presigned")
            .build();
        
        when(uploadService.uploadDocument(eq(offerId), eq(bankId), eq(officerId), any()))
            .thenReturn(metadata);
        
        // Act & Assert
        mockMvc.perform(multipart("/api/offers/{offerId}/documents", offerId)
                .file(file)
                .param("bankId", bankId.toString())
                .param("officerId", officerId.toString())
                .param("documentType", "TERMS_CONDITIONS")
                .param("description", "Loan terms")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.offerId").value(offerId.toString()))
            .andExpect(jsonPath("$.documentType").value("TERMS_CONDITIONS"))
            .andExpect(jsonPath("$.fileName").value("terms.pdf"))
            .andExpect(jsonPath("$.virusScanStatus").value("CLEAN"));
        
        verify(uploadService).uploadDocument(eq(offerId), eq(bankId), eq(officerId), any());
    }
    
    @Test
    @WithMockUser(roles = "BORROWER")
    void testUploadDocument_UnauthorizedRole_Returns403() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
            "file", "terms.pdf", "application/pdf", "PDF content".getBytes()
        );
        
        // Act & Assert
        mockMvc.perform(multipart("/api/offers/{offerId}/documents", offerId)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andExpect(status().isForbidden());
        
        verify(uploadService, never()).uploadDocument(any(), any(), any(), any());
    }
    
    @Test
    @WithMockUser(roles = "BANK_OFFICER")
    void testUploadDocument_NoFile_Returns400() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        UUID bankId = UUID.randomUUID();
        UUID officerId = UUID.randomUUID();
        
        // Act & Assert
        mockMvc.perform(multipart("/api/offers/{offerId}/documents", offerId)
                .param("bankId", bankId.toString())
                .param("officerId", officerId.toString())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "BORROWER")
    void testGetDocuments_ValidRequest_ReturnsDocumentsList() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        OfferDocumentMetadata doc1 = OfferDocumentMetadata.builder()
            .documentId(UUID.randomUUID())
            .offerId(offerId)
            .documentType("TERMS_CONDITIONS")
            .fileName("terms.pdf")
            .fileSize(5000L)
            .uploadedAt(LocalDateTime.now())
            .uploadedByOfficerId(UUID.randomUUID())
            .virusScanStatus("CLEAN")
            .downloadUrl("https://s3.amazonaws.com/url1")
            .build();
        
        OfferDocumentMetadata doc2 = OfferDocumentMetadata.builder()
            .documentId(UUID.randomUUID())
            .offerId(offerId)
            .documentType("DISCLOSURE")
            .fileName("disclosure.pdf")
            .fileSize(3000L)
            .uploadedAt(LocalDateTime.now())
            .uploadedByOfficerId(UUID.randomUUID())
            .virusScanStatus("CLEAN")
            .downloadUrl("https://s3.amazonaws.com/url2")
            .build();
        
        OfferDocumentsListResponse response = OfferDocumentsListResponse.builder()
            .offerId(offerId)
            .documents(Arrays.asList(doc1, doc2))
            .totalCount(2)
            .retrievedAt(LocalDateTime.now())
            .build();
        
        when(retrievalService.getDocuments(eq(offerId), any())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(get("/api/offers/{offerId}/documents", offerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.offerId").value(offerId.toString()))
            .andExpect(jsonPath("$.documents.length()").value(2))
            .andExpect(jsonPath("$.totalCount").value(2))
            .andExpect(jsonPath("$.documents[0].documentType").value("TERMS_CONDITIONS"))
            .andExpect(jsonPath("$.documents[1].documentType").value("DISCLOSURE"));
        
        verify(retrievalService).getDocuments(eq(offerId), any());
    }
    
    @Test
    void testGetDocuments_Unauthenticated_Returns401() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        
        // Act & Assert
        mockMvc.perform(get("/api/offers/{offerId}/documents", offerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "BANK_OFFICER")
    void testGetDownloadUrl_ValidRequest_ReturnsUrl() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        String presignedUrl = "https://s3.amazonaws.com/presigned-url";
        
        when(downloadService.getDownloadUrl(eq(offerId), eq(documentId), any()))
            .thenReturn(presignedUrl);
        
        // Act & Assert
        mockMvc.perform(get("/api/offers/{offerId}/documents/{documentId}/download", offerId, documentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.downloadUrl").value(presignedUrl));
        
        verify(downloadService).getDownloadUrl(eq(offerId), eq(documentId), any());
    }
    
    @Test
    @WithMockUser(roles = "BORROWER")
    void testGetDownloadUrl_DocumentInfected_Returns403() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        
        when(downloadService.getDownloadUrl(eq(offerId), eq(documentId), any()))
            .thenThrow(new IllegalArgumentException("Document is infected"));
        
        // Act & Assert
        mockMvc.perform(get("/api/offers/{offerId}/documents/{documentId}/download", offerId, documentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "BANK_OFFICER")
    void testGetDownloadUrl_DocumentPending_Returns202() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        
        when(downloadService.getDownloadUrl(eq(offerId), eq(documentId), any()))
            .thenThrow(new IllegalArgumentException("Document is pending virus scan"));
        
        // Act & Assert
        mockMvc.perform(get("/api/offers/{offerId}/documents/{documentId}/download", offerId, documentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }
    
    @Test
    @WithMockUser(roles = "BORROWER")
    void testGetDownloadUrl_DocumentNotFound_Returns404() throws Exception {
        // Arrange
        UUID offerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        
        when(downloadService.getDownloadUrl(eq(offerId), eq(documentId), any()))
            .thenThrow(new IllegalArgumentException("Document not found"));
        
        // Act & Assert
        mockMvc.perform(get("/api/offers/{offerId}/documents/{documentId}/download", offerId, documentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
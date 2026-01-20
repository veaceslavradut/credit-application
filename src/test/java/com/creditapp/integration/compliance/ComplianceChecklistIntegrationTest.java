package com.creditapp.integration.compliance;

import com.creditapp.compliance.dto.ComplianceChecklistResponse;
import com.creditapp.compliance.dto.ComplianceChecklistUpdateRequest;
import com.creditapp.shared.model.ComplianceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComplianceChecklistIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testGetComplianceChecklist_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(get("/api/compliance/checklist"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "BORROWER")
    void testGetComplianceChecklist_Forbidden_Returns403() throws Exception {
        mockMvc.perform(get("/api/compliance/checklist"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "COMPLIANCE_OFFICER")
    void testGetComplianceChecklist_ReturnsChecklistWithAllItems() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/compliance/checklist")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        
        String content = result.getResponse().getContentAsString();
        ComplianceChecklistResponse response = objectMapper.readValue(content, ComplianceChecklistResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getItems());
        assertTrue(response.getItems().size() >= 12);
        assertNotNull(response.getOverallStatus());
        assertTrue(response.getGreenCount() >= 0);
        assertTrue(response.getYellowCount() >= 0);
        assertTrue(response.getRedCount() >= 0);
    }
    
    @Test
    @WithMockUser(roles = "COMPLIANCE_OFFICER")
    void testGetComplianceChecklist_ItemsHaveGDPRArticles() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/compliance/checklist")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        
        String content = result.getResponse().getContentAsString();
        ComplianceChecklistResponse response = objectMapper.readValue(content, ComplianceChecklistResponse.class);
        
        response.getItems().forEach(item -> {
            assertNotNull(item.getGdprArticles());
            assertFalse(item.getGdprArticles().isEmpty());
        });
    }
    
    @Test
    @WithMockUser(roles = "COMPLIANCE_OFFICER")
    void testUpdateChecklistItem_UpdatesStatus() throws Exception {
        // First get the checklist to get an item ID
        MvcResult getResult = mockMvc.perform(get("/api/compliance/checklist")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        
        String content = getResult.getResponse().getContentAsString();
        ComplianceChecklistResponse response = objectMapper.readValue(content, ComplianceChecklistResponse.class);
        String itemId = response.getItems().get(0).getId().toString();
        
        ComplianceChecklistUpdateRequest updateRequest = ComplianceChecklistUpdateRequest.builder()
            .status(ComplianceStatus.GREEN)
            .notes("Verified by compliance officer")
            .build();
        
        mockMvc.perform(put("/api/compliance/checklist/" + itemId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "COMPLIANCE_OFFICER")
    void testGenerateSubmissionPackage_ReturnsPDF() throws Exception {
        byte[] pdfBytes = mockMvc.perform(get("/api/compliance/checklist/submission-package")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Verify PDF signature
        assertTrue(pdfBytes[0] == 0x25); // '%' character (start of PDF)
    }
    
    @Test
    @WithMockUser(roles = "BORROWER")
    void testGenerateSubmissionPackage_Forbidden_Returns403() throws Exception {
        mockMvc.perform(get("/api/compliance/checklist/submission-package"))
            .andExpect(status().isForbidden());
    }
}
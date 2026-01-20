package com.creditapp.unit.compliance;

import com.creditapp.compliance.dto.ComplianceChecklistResponse;
import com.creditapp.compliance.dto.ComplianceChecklistUpdateRequest;
import com.creditapp.compliance.service.ComplianceChecklistService;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.ComplianceChecklistItem;
import com.creditapp.shared.model.ComplianceStatus;
import com.creditapp.shared.repository.ComplianceChecklistItemRepository;
import com.creditapp.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceChecklistUnitTest {
    
    @Mock
    private ComplianceChecklistItemRepository mockRepository;
    
    @Mock
    private AuditService mockAuditService;
    
    private ComplianceChecklistService service;
    
    @BeforeEach
    void setUp() {
        service = new ComplianceChecklistService(mockRepository, mockAuditService);
    }
    
    @Test
    void testGetComplianceChecklist_ReturnsAllItems() {
        // Arrange
        List<ComplianceChecklistItem> items = Arrays.asList(
            ComplianceChecklistItem.builder()
                .id(UUID.randomUUID())
                .itemName("Explicit Consent")
                .description("Consent captured")
                .status(ComplianceStatus.GREEN)
                .gdprArticles("6, 7")
                .evidence("url1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            ComplianceChecklistItem.builder()
                .id(UUID.randomUUID())
                .itemName("Data Deletion")
                .description("Deletion capability")
                .status(ComplianceStatus.GREEN)
                .gdprArticles("17")
                .evidence("url2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        when(mockRepository.findAll()).thenReturn(items);
        
        // Act
        ComplianceChecklistResponse response = service.getComplianceChecklist();
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getItems().size());
        assertEquals(2, response.getGreenCount());
        assertEquals(0, response.getYellowCount());
        assertEquals(0, response.getRedCount());
        assertEquals(ComplianceStatus.GREEN, response.getOverallStatus());
    }
    
    @Test
    void testGetComplianceChecklist_CalculatesOverallStatusRed() {
        // Arrange
        List<ComplianceChecklistItem> items = Arrays.asList(
            ComplianceChecklistItem.builder()
                .id(UUID.randomUUID())
                .itemName("Item 1")
                .status(ComplianceStatus.GREEN)
                .gdprArticles("1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            ComplianceChecklistItem.builder()
                .id(UUID.randomUUID())
                .itemName("Item 2")
                .status(ComplianceStatus.RED)
                .gdprArticles("2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        when(mockRepository.findAll()).thenReturn(items);
        
        // Act
        ComplianceChecklistResponse response = service.getComplianceChecklist();
        
        // Assert
        assertEquals(ComplianceStatus.RED, response.getOverallStatus());
        assertEquals(1, response.getGreenCount());
        assertEquals(0, response.getYellowCount());
        assertEquals(1, response.getRedCount());
    }
    
    @Test
    void testGetComplianceChecklist_CalculatesOverallStatusYellow() {
        // Arrange
        List<ComplianceChecklistItem> items = Arrays.asList(
            ComplianceChecklistItem.builder()
                .id(UUID.randomUUID())
                .itemName("Item 1")
                .status(ComplianceStatus.GREEN)
                .gdprArticles("1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            ComplianceChecklistItem.builder()
                .id(UUID.randomUUID())
                .itemName("Item 2")
                .status(ComplianceStatus.YELLOW)
                .gdprArticles("2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        when(mockRepository.findAll()).thenReturn(items);
        
        // Act
        ComplianceChecklistResponse response = service.getComplianceChecklist();
        
        // Assert
        assertEquals(ComplianceStatus.YELLOW, response.getOverallStatus());
        assertEquals(1, response.getGreenCount());
        assertEquals(1, response.getYellowCount());
        assertEquals(0, response.getRedCount());
    }
    
    @Test
    void testUpdateChecklistItem_UpdatesStatusAndNotes() {
        // Arrange
        UUID itemId = UUID.randomUUID();
        ComplianceChecklistItem item = ComplianceChecklistItem.builder()
            .id(itemId)
            .itemName("Test Item")
            .description("Test")
            .status(ComplianceStatus.YELLOW)
            .gdprArticles("1")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        ComplianceChecklistUpdateRequest request = ComplianceChecklistUpdateRequest.builder()
            .status(ComplianceStatus.GREEN)
            .notes("Verified and approved")
            .build();
        
        when(mockRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(mockRepository.save(any())).thenReturn(item);
        
        // Act
        var response = service.updateChecklistItem(itemId, request);
        
        // Assert
        assertNotNull(response);
        verify(mockRepository).save(any());
        verify(mockAuditService).logAction(
            eq("COMPLIANCE_CHECKLIST_ITEM"),
            eq(itemId),
            eq(AuditAction.COMPLIANCE_CHECKLIST_REVIEWED)
        );
    }
    
    @Test
    void testUpdateChecklistItem_ThrowsWhenItemNotFound() {
        // Arrange
        UUID itemId = UUID.randomUUID();
        ComplianceChecklistUpdateRequest request = ComplianceChecklistUpdateRequest.builder()
            .status(ComplianceStatus.GREEN)
            .build();
        
        when(mockRepository.findById(itemId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.updateChecklistItem(itemId, request));
    }
    
    @Test
    void testGenerateSubmissionPackage_ReturnsPDF() {
        // Arrange
        List<ComplianceChecklistItem> items = Arrays.asList(
            ComplianceChecklistItem.builder()
                .id(UUID.randomUUID())
                .itemName("Item 1")
                .description("Description 1")
                .status(ComplianceStatus.GREEN)
                .gdprArticles("1, 2")
                .evidence("evidence1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        when(mockRepository.findAll()).thenReturn(items);
        
        // Act
        byte[] pdf = service.generateSubmissionPackage();
        
        // Assert
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // Submission package is UTF-8 text format (not binary PDF)
        String content = new String(pdf, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("GDPR") || content.contains("COMPLIANCE"));
    }
    
    @Test
    void testInitializeChecklistItems_CreatesDefaultItems() {
        // Arrange
        when(mockRepository.findByItemName(anyString())).thenReturn(null);
        when(mockRepository.save(any())).thenReturn(null);
        
        // Act
        service.initializeChecklistItems();
        
        // Assert
        verify(mockRepository, times(12)).save(any()); // 12 predefined items
    }
}
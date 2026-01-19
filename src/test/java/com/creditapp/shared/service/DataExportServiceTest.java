package com.creditapp.shared.service;

import com.creditapp.shared.model.DataExport;
import com.creditapp.shared.model.ExportFormat;
import com.creditapp.shared.model.ExportStatus;
import com.creditapp.shared.repository.DataExportRepository;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.model.User;
import com.creditapp.borrower.dto.DataExportResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

    @Mock
    private DataExportRepository dataExportRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ExportFileGenerator exportFileGenerator;

    @Mock
    private DataExportEmailService dataExportEmailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataExportService dataExportService;

    private UUID borrowerId;
    private UUID exportId;
    private String downloadToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        exportId = UUID.randomUUID();
        downloadToken = "test-download-token-12345";

        testUser = new User();
        testUser.setId(borrowerId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
    }

    @Test
    void testInitiateExport_CreatesPendingExportWithToken() {
        // Arrange
        ExportFormat format = ExportFormat.JSON;
        String ipAddress = "192.168.1.1";

        DataExport savedExport = DataExport.builder()
            .id(exportId)
            .borrowerId(borrowerId)
            .status(ExportStatus.PENDING)
            .format(format)
            .downloadToken(downloadToken)
            .createdByIp(ipAddress)
            .requestedAt(LocalDateTime.now())
            .build();

        when(dataExportRepository.save(any(DataExport.class)))
            .thenReturn(savedExport);

        // Act
        DataExportResponse response = dataExportService.initiateExport(borrowerId, format, ipAddress);

        // Assert
        assertNotNull(response);
        assertEquals(ExportStatus.PENDING, response.getStatus());
        assertEquals(format, response.getFormat());
        assertEquals(exportId, response.getExportId());

        ArgumentCaptor<DataExport> captor = ArgumentCaptor.forClass(DataExport.class);
        verify(dataExportRepository).save(captor.capture());

        DataExport captured = captor.getValue();
        assertEquals(borrowerId, captured.getBorrowerId());
        assertEquals(ExportStatus.PENDING, captured.getStatus());
        assertNotNull(captured.getDownloadToken());
        assertNotNull(captured.getDownloadTokenExpiresAt());

        verify(auditService).logAction(anyString(), any(UUID.class), any());
    }

    @Test
    void testInitiateExport_GeneratesValidDownloadToken() {
        // Arrange
        when(dataExportRepository.save(any(DataExport.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataExportService.initiateExport(borrowerId, ExportFormat.JSON, "192.168.1.1");

        // Assert
        ArgumentCaptor<DataExport> captor = ArgumentCaptor.forClass(DataExport.class);
        verify(dataExportRepository).save(captor.capture());

        DataExport captured = captor.getValue();
        String token = captured.getDownloadToken();

        assertNotNull(token);
        assertNotEmpty(token);
        assertTrue(token.length() > 20);  // Base64 encoded 32 bytes should be > 20 chars
    }

    @Test
    void testInitiateExport_TokenExpiresTomorrow() {
        // Arrange
        when(dataExportRepository.save(any(DataExport.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime before = LocalDateTime.now().plusHours(23);
        LocalDateTime after = LocalDateTime.now().plusHours(25);

        // Act
        dataExportService.initiateExport(borrowerId, ExportFormat.JSON, "192.168.1.1");

        // Assert
        ArgumentCaptor<DataExport> captor = ArgumentCaptor.forClass(DataExport.class);
        verify(dataExportRepository).save(captor.capture());

        LocalDateTime expiresAt = captor.getValue().getDownloadTokenExpiresAt();
        assertTrue(expiresAt.isAfter(before) && expiresAt.isBefore(after),
            "Token should expire in 24 hours");
    }

    @Test
    void testDownloadExport_TokenValid24Hours() {
        // Arrange
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        DataExport export = DataExport.builder()
            .id(exportId)
            .borrowerId(borrowerId)
            .status(ExportStatus.COMPLETED)
            .downloadToken(downloadToken)
            .downloadTokenExpiresAt(expiresAt)
            .build();

        when(dataExportRepository.findByDownloadToken(downloadToken))
            .thenReturn(Optional.of(export));
        when(dataExportRepository.save(any(DataExport.class)))
            .thenReturn(export);

        // Act & Assert - should not throw
        assertDoesNotThrow(() ->
            dataExportService.downloadExport(downloadToken, borrowerId)
        );

        verify(auditService).logAction(anyString(), any(UUID.class), any());
    }

    @Test
    void testDownloadExport_ExpiredToken_Throws() {
        // Arrange
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1);  // Expired
        DataExport export = DataExport.builder()
            .id(exportId)
            .borrowerId(borrowerId)
            .downloadToken(downloadToken)
            .downloadTokenExpiresAt(expiresAt)
            .build();

        when(dataExportRepository.findByDownloadToken(downloadToken))
            .thenReturn(Optional.of(export));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            dataExportService.downloadExport(downloadToken, borrowerId)
        );
    }

    @Test
    void testDownloadExport_WrongBorrower_Returns403() {
        // Arrange
        UUID differentBorrowerId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        DataExport export = DataExport.builder()
            .id(exportId)
            .borrowerId(differentBorrowerId)
            .status(ExportStatus.COMPLETED)
            .downloadToken(downloadToken)
            .downloadTokenExpiresAt(expiresAt)
            .build();

        when(dataExportRepository.findByDownloadToken(downloadToken))
            .thenReturn(Optional.of(export));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            dataExportService.downloadExport(downloadToken, borrowerId)
        );
    }

    @Test
    void testDownloadExport_NotCompleted_Throws() {
        // Arrange
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        DataExport export = DataExport.builder()
            .id(exportId)
            .borrowerId(borrowerId)
            .status(ExportStatus.PENDING)  // Not completed
            .downloadToken(downloadToken)
            .downloadTokenExpiresAt(expiresAt)
            .build();

        when(dataExportRepository.findByDownloadToken(downloadToken))
            .thenReturn(Optional.of(export));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            dataExportService.downloadExport(downloadToken, borrowerId)
        );
    }

    @Test
    void testDownloadExport_TokenInvalidatedAfterDownload() {
        // Arrange
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        DataExport export = DataExport.builder()
            .id(exportId)
            .borrowerId(borrowerId)
            .status(ExportStatus.COMPLETED)
            .downloadToken(downloadToken)
            .downloadTokenExpiresAt(expiresAt)
            .build();

        when(dataExportRepository.findByDownloadToken(downloadToken))
            .thenReturn(Optional.of(export));
        when(dataExportRepository.save(any(DataExport.class)))
            .thenReturn(export);

        // Act
        dataExportService.downloadExport(downloadToken, borrowerId);

        // Assert - token should be nullified
        ArgumentCaptor<DataExport> captor = ArgumentCaptor.forClass(DataExport.class);
        verify(dataExportRepository).save(captor.capture());

        assertNull(captor.getValue().getDownloadToken());
    }

    @Test
    void testGetExportStatus_ReturnsCorrectStatus() {
        // Arrange
        DataExport export = DataExport.builder()
            .id(exportId)
            .borrowerId(borrowerId)
            .status(ExportStatus.COMPLETED)
            .format(ExportFormat.JSON)
            .requestedAt(LocalDateTime.now().minusHours(1))
            .completedAt(LocalDateTime.now())
            .build();

        when(dataExportRepository.findById(exportId))
            .thenReturn(Optional.of(export));

        // Act
        DataExportResponse response = dataExportService.getExportStatus(exportId, borrowerId);

        // Assert
        assertNotNull(response);
        assertEquals(ExportStatus.COMPLETED, response.getStatus());
        assertEquals(ExportFormat.JSON, response.getFormat());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void testGetExportStatus_WrongBorrower_Throws() {
        // Arrange
        UUID differentBorrowerId = UUID.randomUUID();
        DataExport export = DataExport.builder()
            .id(exportId)
            .borrowerId(differentBorrowerId)
            .build();

        when(dataExportRepository.findById(exportId))
            .thenReturn(Optional.of(export));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            dataExportService.getExportStatus(exportId, borrowerId)
        );
    }

    private void assertNotEmpty(String str) {
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }
}
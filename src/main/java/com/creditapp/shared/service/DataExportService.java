package com.creditapp.shared.service;

import com.creditapp.borrower.dto.DataExportResponse;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.DataExport;
import com.creditapp.shared.model.ExportFormat;
import com.creditapp.shared.model.ExportStatus;
import com.creditapp.shared.repository.DataExportRepository;
import com.creditapp.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {
    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRY_HOURS = 24;
    private static final long TIMEOUT_SECONDS = 300;
    
    private final DataExportRepository dataExportRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final ExportFileGenerator exportFileGenerator;
    private final DataExportEmailService dataExportEmailService;
    private final UserRepository userRepository;
    
    @Transactional
    public DataExportResponse initiateExport(UUID borrowerId, ExportFormat format, String ipAddress) {
        log.info("Initiating data export for borrower: {}", borrowerId);
        
        String downloadToken = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(EXPIRY_HOURS);
        
        DataExport export = DataExport.builder()
            .borrowerId(borrowerId)
            .status(ExportStatus.PENDING)
            .format(format)
            .downloadToken(downloadToken)
            .downloadTokenExpiresAt(expiresAt)
            .createdByIp(ipAddress)
            .expiresAt(expiresAt)
            .build();
        
        DataExport saved = dataExportRepository.save(export);
        
        auditService.logAction("DataExport", saved.getId(), AuditAction.DATA_EXPORT_REQUESTED);
        
        log.info("Data export created: {}", saved.getId());
        
        // Queue async job for export generation
        generateExportFileAsync(saved.getId());
        
        return DataExportResponse.builder()
            .exportId(saved.getId())
            .status(saved.getStatus())
            .format(saved.getFormat())
            .message("Export requested. Download link sent to email.")
            .requestedAt(saved.getRequestedAt())
            .expiresAt(saved.getExpiresAt())
            .build();
    }
    
    @Transactional
    public byte[] downloadExport(String downloadToken, UUID borrowerId) {
        log.info("Attempting export download");
        
        Optional<DataExport> exportOpt = dataExportRepository.findByDownloadToken(downloadToken);
        if (exportOpt.isEmpty()) {
            throw new RuntimeException("Export not found");
        }
        
        DataExport export = exportOpt.get();
        
        if (!export.getBorrowerId().equals(borrowerId)) {
            throw new RuntimeException("Forbidden");
        }
        
        if (export.getDownloadTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }
        
        if (export.getStatus() != ExportStatus.COMPLETED) {
            throw new RuntimeException("Export not ready");
        }
        
        export.setDownloadToken(null);
        DataExport updated = dataExportRepository.save(export);
        
        auditService.logAction("DataExport", updated.getId(), AuditAction.DATA_EXPORT_DOWNLOADED);
        
        return new byte[]{};
    }
    
    public DataExportResponse getExportStatus(UUID exportId, UUID borrowerId) {
        Optional<DataExport> exportOpt = dataExportRepository.findById(exportId);
        if (exportOpt.isEmpty()) {
            throw new RuntimeException("Export not found");
        }
        
        DataExport export = exportOpt.get();
        if (!export.getBorrowerId().equals(borrowerId)) {
            throw new RuntimeException("Forbidden");
        }
        
        return DataExportResponse.builder()
            .exportId(export.getId())
            .status(export.getStatus())
            .format(export.getFormat())
            .requestedAt(export.getRequestedAt())
            .completedAt(export.getCompletedAt())
            .expiresAt(export.getExpiresAt())
            .build();
    }
    
    @Async("dataExportExecutor")
    @Transactional
    public void generateExportFileAsync(UUID exportId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting async export file generation for export: {}", exportId);
        
        try {
            Optional<DataExport> exportOpt = dataExportRepository.findById(exportId);
            if (exportOpt.isEmpty()) {
                log.error("Export not found: {}", exportId);
                return;
            }
            
            DataExport export = exportOpt.get();
            UUID borrowerId = export.getBorrowerId();
            
            // Check timeout
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsedSeconds > TIMEOUT_SECONDS) {
                log.error("Export generation timeout for export: {}", exportId);
                export.setStatus(ExportStatus.FAILED);
                dataExportRepository.save(export);
                auditService.logAction("DataExport", exportId, AuditAction.DATA_EXPORT_COMPLETED);
                return;
            }
            
            // Generate export data
            JsonNode exportData = exportFileGenerator.generateJsonExport(borrowerId);
            
            // TODO: Upload to S3 and get actual file URL
            String fileContent = objectMapper.writeValueAsString(exportData);
            log.debug("Generated export file size: {} bytes", fileContent.length());
            
            // Mark as completed (in real scenario, would upload to S3)
            export.setStatus(ExportStatus.COMPLETED);
            export.setCompletedAt(LocalDateTime.now());
            export.setDownloadTokenExpiresAt(LocalDateTime.now().plusHours(EXPIRY_HOURS));
            export.setFileUrl("s3://bucket/exports/" + exportId + ".json");
            
            dataExportRepository.save(export);
            
            // Send email notification with download link
            sendExportReadyEmail(export);
            
            auditService.logAction("DataExport", exportId, AuditAction.DATA_EXPORT_COMPLETED);
            
            log.info("Export generation completed for export: {} (took {} seconds)", exportId, 
                (System.currentTimeMillis() - startTime) / 1000);
            
        } catch (Exception e) {
            log.error("Error generating export: {}", exportId, e);
            handleExportFailure(exportId);
        }
    }
    
    private void handleExportFailure(UUID exportId) {
        try {
            Optional<DataExport> exportOpt = dataExportRepository.findById(exportId);
            if (exportOpt.isPresent()) {
                DataExport export = exportOpt.get();
                export.setStatus(ExportStatus.FAILED);
                export.setCompletedAt(LocalDateTime.now());
                dataExportRepository.save(export);
                
                log.error("Export marked as FAILED: {}", exportId);
            }
        } catch (Exception e) {
            log.error("Error handling export failure: {}", exportId, e);
        }
    }
    
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    private void sendExportReadyEmail(DataExport export) {
        try {
            Optional<com.creditapp.shared.model.User> userOpt = userRepository.findById(export.getBorrowerId());
            if (userOpt.isPresent()) {
                String borrowerEmail = userOpt.get().getEmail();
                dataExportEmailService.sendDataExportReadyEmail(export, borrowerEmail);
                log.info("Email notification sent for export: {}", export.getId());
            } else {
                log.warn("User not found for export: {}", export.getId());
            }
        } catch (Exception e) {
            log.error("Error sending export ready email for export: {}", export.getId(), e);
            // Don't throw - allow export to complete even if email fails
        }
    }
}
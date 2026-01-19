package com.creditapp.shared.service;

import com.creditapp.borrower.dto.DataExportResponse;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.DataExport;
import com.creditapp.shared.model.ExportFormat;
import com.creditapp.shared.model.ExportStatus;
import com.creditapp.shared.repository.DataExportRepository;
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
    
    private final DataExportRepository dataExportRepository;
    private final AuditService auditService;
    
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
    
    @Async
    public void generateExportFileAsync(UUID exportId) {
        log.info("Starting async export file generation");
    }
    
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
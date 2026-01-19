package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.DataExportResponse;
import com.creditapp.shared.model.ExportFormat;
import com.creditapp.shared.service.DataExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/borrower/data-export")
@RequiredArgsConstructor
@Slf4j
public class BorrowerDataExportController {
    private final DataExportService dataExportService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<DataExportResponse> initiateExport(
            @RequestParam(value = "format", defaultValue = "JSON") String format,
            Authentication authentication) {
        log.info("Initiating data export");
        UUID borrowerId = UUID.fromString(authentication.getName());
        String ipAddress = "127.0.0.1";
        
        DataExportResponse response = dataExportService.initiateExport(
            borrowerId, 
            ExportFormat.valueOf(format), 
            ipAddress);
        
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
    
    @GetMapping("/download")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<byte[]> downloadExport(
            @RequestParam String token,
            Authentication authentication) {
        log.info("Downloading export");
        UUID borrowerId = UUID.fromString(authentication.getName());
        
        byte[] fileContent = dataExportService.downloadExport(token, borrowerId);
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=export.json")
            .body(fileContent);
    }
    
    @GetMapping("/status/{exportId}")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<DataExportResponse> getExportStatus(
            @PathVariable UUID exportId,
            Authentication authentication) {
        log.info("Getting export status");
        UUID borrowerId = UUID.fromString(authentication.getName());
        
        DataExportResponse response = dataExportService.getExportStatus(exportId, borrowerId);
        
        return ResponseEntity.ok(response);
    }
}
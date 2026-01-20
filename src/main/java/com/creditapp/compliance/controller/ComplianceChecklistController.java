package com.creditapp.compliance.controller;

import com.creditapp.compliance.dto.ComplianceChecklistResponse;
import com.creditapp.compliance.dto.ComplianceChecklistUpdateRequest;
import com.creditapp.compliance.service.ComplianceChecklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/compliance/checklist")
@RequiredArgsConstructor
@Slf4j
public class ComplianceChecklistController {
    
    private final ComplianceChecklistService complianceChecklistService;
    
    @GetMapping
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ComplianceChecklistResponse> getComplianceChecklist() {
        log.info("Retrieving compliance checklist");
        ComplianceChecklistResponse response = complianceChecklistService.getComplianceChecklist();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<?> updateChecklistItem(
            @PathVariable UUID itemId,
            @RequestBody ComplianceChecklistUpdateRequest request) {
        log.info("Updating compliance checklist item: {}", itemId);
        
        try {
            var response = complianceChecklistService.updateChecklistItem(itemId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error updating compliance item: {}", e.getMessage());
            return new ResponseEntity<>(
                new ErrorResponse("ERROR", e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @GetMapping("/submission-package")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<byte[]> generateSubmissionPackage() {
        log.info("Generating compliance submission package");
        
        try {
            byte[] pdfBytes = complianceChecklistService.generateSubmissionPackage();
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compliance-submission-package.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
        } catch (Exception e) {
            log.error("Error generating submission package: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    static class ErrorResponse {
        private String status;
        private String message;
        
        ErrorResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
        
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
package com.creditapp.compliance.controller;

import com.creditapp.shared.security.RequiresComplianceOfficer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    @RequiresComplianceOfficer
    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok().body(new Object());
    }

    @PreAuthorize("hasAnyAuthority('COMPLIANCE_OFFICER', 'BANK_ADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        return ResponseEntity.ok().body(new Object());
    }
}
package com.creditapp.compliance.controller;

import com.creditapp.compliance.dto.AuditLogFilterDTO;
import com.creditapp.compliance.dto.AuditLogResponse;
import com.creditapp.compliance.service.ComplianceAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/compliance/audit-logs")
@RequiredArgsConstructor
public class ComplianceAuditController {

    private final ComplianceAuditService complianceAuditService;

    @GetMapping
    @PreAuthorize("hasAuthority('COMPLIANCE_OFFICER')")
    public Page<AuditLogResponse> getAuditLogs(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
            @RequestParam(value = "result", required = false) String result,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "100") Integer size
    ) {
        AuditLogFilterDTO filter = new AuditLogFilterDTO();
        filter.setUserId(userId);
        filter.setAction(action);
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);
        filter.setResult(result);
        filter.setPage(page);
        filter.setSize(size);
        return complianceAuditService.getAuditLogs(filter);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('COMPLIANCE_OFFICER')")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
            @RequestParam(value = "result", required = false) String result,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "100") Integer size
    ) {
        AuditLogFilterDTO filter = new AuditLogFilterDTO();
        filter.setUserId(userId);
        filter.setAction(action);
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);
        filter.setResult(result);
        filter.setPage(page);
        filter.setSize(size);
        byte[] csv = complianceAuditService.exportAuditLogsAsCSV(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit_logs.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}

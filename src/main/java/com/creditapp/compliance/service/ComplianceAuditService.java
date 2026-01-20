package com.creditapp.compliance.service;

import com.creditapp.compliance.dto.AuditLogExportDTO;
import com.creditapp.compliance.dto.AuditLogFilterDTO;
import com.creditapp.compliance.dto.AuditLogResponse;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import com.creditapp.shared.service.SensitiveDataRedactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplianceAuditService {
    private final AuditLogRepository auditLogRepository;
    private final SensitiveDataRedactionService dataRedactionService;

    public Page<AuditLogResponse> getAuditLogs(AuditLogFilterDTO filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? Math.min(filter.getSize(), 1000) : 100;
        PageRequest pr = PageRequest.of(page, size);

        Page<AuditLog> raw;
        if (filter.getUserId() != null) {
            raw = auditLogRepository.findByActorIdOrderByCreatedAtDesc(filter.getUserId(), pr);
        } else if (filter.getAction() != null) {
            AuditAction act = AuditAction.valueOf(filter.getAction());
            raw = auditLogRepository.findByEntityTypeAndActionOrderByCreatedAtDesc("*", act, pr); // placeholder entityType
        } else if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            LocalDateTime from = filter.getDateFrom().atStartOfDay();
            LocalDateTime to = filter.getDateTo().atTime(23, 59, 59);
            raw = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pr);
        } else if (filter.getResult() != null) {
            raw = auditLogRepository.findByResultOrderByCreatedAtDesc(filter.getResult(), pr);
        } else {
            // default: date range last 30 days
            LocalDateTime to = LocalDateTime.now();
            LocalDateTime from = to.minusDays(30);
            raw = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pr);
        }

        List<AuditLogResponse> mapped = raw.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return new PageImpl<>(mapped, pr, raw.getTotalElements());
    }

    public byte[] exportAuditLogsAsCSV(AuditLogFilterDTO filter) {
        Page<AuditLogResponse> page = getAuditLogs(filter);
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp,User ID,Action,Result,IP Address,User Agent,Resource Type,Resource ID\n");
        for (AuditLogResponse r : page.getContent()) {
            sb.append(Objects.toString(r.getTimestamp(), "")).append(',')
              .append(Objects.toString(r.getUserId(), "")).append(',')
              .append(Objects.toString(r.getAction(), "")).append(',')
              .append(Objects.toString(r.getResult(), "")).append(',')
              .append(escapeCsv(Objects.toString(r.getIpAddress(), ""))).append(',')
              .append(escapeCsv(Objects.toString(r.getUserAgent(), ""))).append(',')
              .append(escapeCsv(Objects.toString(r.getResourceType(), ""))).append(',')
              .append(Objects.toString(r.getResourceId(), ""))
              .append('\n');
        }
        return sb.toString().getBytes();
    }

    public List<AuditLogResponse> getAuditLogsByUser(UUID userId, LocalDate from, LocalDate to) {
        LocalDateTime f = from.atStartOfDay();
        LocalDateTime t = to.atTime(23, 59, 59);
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(userId).stream()
                .filter(a -> a.getCreatedAt().isAfter(f) && a.getCreatedAt().isBefore(t))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getAuditLogsByAction(String action, LocalDate from, LocalDate to) {
        AuditAction act = AuditAction.valueOf(action);
        LocalDateTime f = from.atStartOfDay();
        LocalDateTime t = to.atTime(23, 59, 59);
        return auditLogRepository.findByEntityTypeAndActionOrderByCreatedAtDesc("*", act).stream()
                .filter(a -> a.getCreatedAt().isAfter(f) && a.getCreatedAt().isBefore(t))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .userId(a.getActorId())
                .action(a.getAction())
                .result(a.getResult())
                .timestamp(a.getCreatedAt())
                .ipAddress(a.getIpAddress())
                .userAgent(a.getUserAgent())
                .resourceType(a.getEntityType())
                .resourceId(a.getEntityId())
                .details(dataRedactionService.redactSensitiveData(a.getNewValues()))
                .build();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}

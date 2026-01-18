package com.creditapp.bank.controller;

import com.creditapp.bank.dto.ApplicationQueueRequest;
import com.creditapp.bank.dto.ApplicationQueueResponse;
import com.creditapp.bank.dto.ApplicationStatusUpdateRequest;
import com.creditapp.bank.dto.ApplicationStatusUpdateResponse;
import com.creditapp.bank.service.ApplicationQueueService;
import com.creditapp.bank.service.ApplicationStatusUpdateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank/dashboard")
public class BankApplicationQueueController {

    private final ApplicationQueueService applicationQueueService;
    private final ApplicationStatusUpdateService applicationStatusUpdateService;

    @GetMapping("/application-queue")
    @PreAuthorize("hasAuthority('BANK_OFFICER')")
    public ResponseEntity<ApplicationQueueResponse> getApplicationQueue(
        @RequestParam("bankId") UUID bankId,
        @RequestParam(value = "limit", required = false) @Min(1) @Max(100) Integer limit,
        @RequestParam(value = "offset", required = false) @Min(0) Integer offset,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "aprMin", required = false) BigDecimal aprMin,
        @RequestParam(value = "aprMax", required = false) BigDecimal aprMax,
        @RequestParam(value = "loanAmountMin", required = false) BigDecimal loanAmountMin,
        @RequestParam(value = "loanAmountMax", required = false) BigDecimal loanAmountMax,
        @RequestParam(value = "dateRangeStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRangeStart,
        @RequestParam(value = "dateRangeEnd", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRangeEnd,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "sortOrder", required = false) String sortOrder
    ) {
        ApplicationQueueRequest request = ApplicationQueueRequest.builder()
            .limit(limit)
            .offset(offset)
            .status(status)
            .aprMin(aprMin)
            .aprMax(aprMax)
            .loanAmountMin(loanAmountMin)
            .loanAmountMax(loanAmountMax)
            .dateRangeStart(dateRangeStart)
            .dateRangeEnd(dateRangeEnd)
            .sortBy(sortBy)
            .sortOrder(sortOrder)
            .build();

        ApplicationQueueResponse response = applicationQueueService.getApplicationQueue(bankId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasAuthority('BANK_OFFICER')")
    public ResponseEntity<ApplicationStatusUpdateResponse> updateApplicationStatus(
        @PathVariable UUID applicationId,
        @RequestParam("bankId") UUID bankId,
        @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        ApplicationStatusUpdateResponse response = applicationStatusUpdateService.updateApplicationStatus(
            applicationId,
            bankId,
            request
        );
        return ResponseEntity.ok(response);
    }
}

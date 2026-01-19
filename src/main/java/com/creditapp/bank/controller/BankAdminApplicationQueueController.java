package com.creditapp.bank.controller;

import com.creditapp.bank.dto.ApplicationQueueResponse;
import com.creditapp.bank.service.BankAdminQueueService;
import com.creditapp.shared.security.AuthorizationService;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Story 4.2: Application Queue Dashboard
 * Provides bank admins with a paginated view of applications with filtering and search
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank/applications")
@Slf4j
public class BankAdminApplicationQueueController {

    private final BankAdminQueueService bankAdminQueueService;
    private final AuthorizationService authorizationService;

    @GetMapping("/queue")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<ApplicationQueueResponse> getApplicationQueue(
        @RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) Integer page,
        @RequestParam(value = "status", required = false) List<String> status,
        @RequestParam(value = "loanType", required = false) List<String> loanType,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @RequestParam(value = "amountFrom", required = false) BigDecimal amountFrom,
        @RequestParam(value = "amountTo", required = false) BigDecimal amountTo,
        @RequestParam(value = "sortBy", required = false, defaultValue = "NEWEST_FIRST") String sortBy,
        @RequestParam(value = "applicationId", required = false) String applicationId,
        @RequestParam(value = "borrowerEmail", required = false) String borrowerEmail,
        @RequestParam(value = "borrowerName", required = false) String borrowerName
    ) {
        // Extract bankId from authenticated user
        UUID bankId = authorizationService.getBankIdFromContext();
        log.debug("[BANK_ADMIN_QUEUE] Fetching application queue for bankId={}, page={}", bankId, page);

        ApplicationQueueResponse response = bankAdminQueueService.getApplicationQueue(
            bankId,
            page,
            status,
            loanType,
            dateFrom,
            dateTo,
            amountFrom,
            amountTo,
            sortBy,
            applicationId,
            borrowerEmail,
            borrowerName
        );

        return ResponseEntity.ok(response);
    }
}
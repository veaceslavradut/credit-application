package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.ApplicationHistoryRecord;
import com.creditapp.borrower.dto.ApplicationHistoryRequest;
import com.creditapp.borrower.dto.ApplicationHistoryResponse;
import com.creditapp.borrower.model.Application;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for retrieving borrower's application history.
 * Provides filtering, sorting, pagination, and aggregated offer data per application.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationHistoryService {
    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;

    /**
     * Get application history for a borrower with filtering and pagination.
     * 
     * @param borrowerId Borrower ID
     * @param request Application history request with filters and pagination
     * @return ApplicationHistoryResponse with paginated applications
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "borrowerApplicationHistory", key = "#borrowerId")
    public ApplicationHistoryResponse getApplicationHistory(UUID borrowerId, ApplicationHistoryRequest request) {
        log.debug("Retrieving application history for borrower: {} with filters: {}", borrowerId, request);

        // Defaults
        Integer limit = request.getLimit() != null ? request.getLimit() : 20;
        if (limit <= 0 || limit > 100) {
            limit = 20;
        }
        Integer offset = request.getOffset() != null ? request.getOffset() : 0;
        if (offset < 0) {
            offset = 0;
        }
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "submittedAt";

        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);

        // Query applications
        Page<Application> applications = applicationRepository.findByBorrowerId(borrowerId, pageable);
        
        long totalCount = applications.getTotalElements();
        
        List<ApplicationHistoryRecord> records = applications.getContent().stream()
            .filter(app -> applyStatusFilter(app, request.getStatus()))
            .filter(app -> applyDateRangeFilter(app, request.getDateRangeStart(), request.getDateRangeEnd()))
            .filter(app -> applyLoanAmountFilter(app, request.getLoanAmountMin(), request.getLoanAmountMax()))
            .map(this::mapToRecord)
            .collect(Collectors.toList());

        boolean hasMore = (offset + limit) < totalCount;

        log.info("Retrieved {} applications for borrower {}, total: {}", records.size(), borrowerId, totalCount);

        return ApplicationHistoryResponse.builder()
            .applications(records)
            .totalCount((int) totalCount)
            .limit(limit)
            .offset(offset)
            .hasMore(hasMore)
            .retrievedAt(LocalDateTime.now())
            .build();
    }

    private boolean applyStatusFilter(Application app, String statusFilter) {
        if (statusFilter == null || statusFilter.isEmpty()) {
            return true;
        }
        return app.getStatus().toString().equalsIgnoreCase(statusFilter);
    }

    private boolean applyDateRangeFilter(Application app, LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return true;
        }
        LocalDateTime appDate = app.getCreatedAt();
        if (start != null && appDate.isBefore(start)) {
            return false;
        }
        if (end != null && appDate.isAfter(end)) {
            return false;
        }
        return true;
    }

    private boolean applyLoanAmountFilter(Application app, BigDecimal minAmount, BigDecimal maxAmount) {
        if (minAmount == null && maxAmount == null) {
            return true;
        }
        BigDecimal amount = app.getLoanAmount();
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        return true;
    }

    private ApplicationHistoryRecord mapToRecord(Application app) {
        List<Offer> offers = offerRepository.findByApplicationId(app.getId());
        
        int offerCount = offers.size();
        BigDecimal bestAPR = offers.stream()
            .map(Offer::getApr)
            .min(BigDecimal::compareTo)
            .orElse(null);
        
        String expirationStatus = determineExpirationStatus(offers);
        
        return ApplicationHistoryRecord.builder()
            .applicationId(app.getId())
            .referenceNumber(app.getId().toString())  // Use application ID as reference since no explicit reference number
            .status(app.getStatus().toString())
            .loanAmount(app.getLoanAmount())
            .termMonths(app.getLoanTermMonths())
            .loanPurpose(app.getLoanType())
            .createdAt(app.getCreatedAt())
            .submittedAt(app.getSubmittedAt())
            .closedAt(app.getWithdrawnAt())  // Use withdrawn_at as closed_at indicator
            .offerCount(offerCount)
            .bestAPR(bestAPR)
            .selectedOfferId(null) // TODO: fetch from application if offer selection tracked
            .finalAcceptedOfferId(null) // TODO: fetch from application if acceptance tracked
            .expirationStatus(expirationStatus)
            .build();
    }

    private String determineExpirationStatus(List<Offer> offers) {
        if (offers.isEmpty()) {
            return "no_offers";
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean hasActive = offers.stream().anyMatch(o -> o.getExpiresAt().isAfter(now));
        boolean hasExpired = offers.stream().anyMatch(o -> o.getExpiresAt().isBefore(now) || o.getExpiresAt().isEqual(now));
        
        if (hasActive && hasExpired) {
            return "mixed";
        } else if (hasActive) {
            return "all_active";
        } else {
            return "all_expired";
        }
    }
}

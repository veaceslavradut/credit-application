package com.creditapp.bank.service;

import com.creditapp.bank.dto.ApplicationQueueItem;
import com.creditapp.bank.dto.ApplicationQueueResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.ApplicationQueueMetrics;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service for Story 4.2: Bank Admin Application Queue Dashboard
 * Provides paginated, filtered, searchable view of applications for bank admins
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankAdminQueueService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;

    private static final int PAGE_SIZE = 20;

    public ApplicationQueueResponse getApplicationQueue(
        UUID bankId,
        Integer page,
        List<String> statusFilters,
        List<String> loanTypeFilters,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        String sortBy,
        String searchApplicationId,
        String searchBorrowerEmail,
        String searchBorrowerName
    ) {
        long startTime = System.currentTimeMillis();
        log.debug("[BANK_ADMIN_QUEUE] Starting queue retrieval for bankId={}", bankId);

        // Get all offers from this bank
        List<Offer> allOffers = offerRepository.findByBankId(bankId, Pageable.unpaged()).getContent();
        
        // Get unique application IDs
        Set<UUID> applicationIds = allOffers.stream()
            .map(Offer::getApplicationId)
            .collect(Collectors.toSet());

        if (applicationIds.isEmpty()) {
            log.debug("[BANK_ADMIN_QUEUE] No applications found for bankId={}", bankId);
            return emptyResponse(page);
        }

        // Load all applications
        List<Application> applications = applicationRepository.findAllById(applicationIds);
        
        // Create a map of offers by application ID
        Map<UUID, Offer> offerMap = allOffers.stream()
            .collect(Collectors.toMap(
                Offer::getApplicationId,
                offer -> offer,
                (existing, replacement) -> existing // Keep first offer if duplicates
            ));

        // Build queue items
        List<ApplicationQueueItem> items = new ArrayList<>();
        for (Application app : applications) {
            Offer offer = offerMap.get(app.getId());
            
            // Apply filters
            if (!matchesFilters(app, statusFilters, loanTypeFilters, dateFrom, dateTo, amountFrom, amountTo)) {
                continue;
            }
            
            // Apply search
            if (!matchesSearch(app, searchApplicationId, searchBorrowerEmail, searchBorrowerName)) {
                continue;
            }

            ApplicationQueueItem item = ApplicationQueueItem.builder()
                .applicationId(app.getId())
                .referenceNumber(null) // Not available in Application model
                .borrowerName(app.getBorrower() != null ? 
                    (app.getBorrower().getFirstName() + " " + app.getBorrower().getLastName()) : "N/A")
                .borrowerEmail(app.getBorrower() != null ? app.getBorrower().getEmail() : null)
                .loanAmount(app.getLoanAmount())
                .termMonths(app.getLoanTermMonths())
                .selectedOfferAPR(offer != null ? offer.getApr() : null)
                .selectedOfferMonthlyPayment(offer != null ? offer.getMonthlyPayment() : null)
                .status(app.getStatus() != null ? app.getStatus().name() : null)
                .receivedAt(app.getCreatedAt())
                .submittedAt(app.getSubmittedAt())
                .lastUpdatedAt(app.getUpdatedAt())
                .documentsStatus(null) // Not needed for Story 4.2
                .approvalStatus(null) // Not needed for Story 4.2
                .actionItems(Collections.emptyList())
                .build();

            items.add(item);
        }

        // Apply sorting
        applySorting(items, sortBy);

        // Calculate new applications count (SUBMITTED status) for badge
        int newApplicationsCount = (int) applications.stream()
            .filter(app -> app.getStatus() == ApplicationStatus.SUBMITTED)
            .count();

        // Apply pagination
        int totalItems = items.size();
        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalItems);
        
        List<ApplicationQueueItem> paginatedItems = startIndex < totalItems 
            ? items.subList(startIndex, endIndex)
            : Collections.emptyList();

        // Build response
        ApplicationQueueMetrics metrics = ApplicationQueueMetrics.builder()
            .totalApplications(totalItems)
            .documentsAwaitingReview(newApplicationsCount) // Badge: count of SUBMITTED applications
            .approvedCount(0)
            .rejectedCount(0)
            .build();

        ApplicationQueueResponse response = ApplicationQueueResponse.builder()
            .applications(paginatedItems)
            .totalCount(totalItems)
            .limit(PAGE_SIZE)
            .offset(startIndex)
            .hasMore(endIndex < totalItems)
            .queueMetrics(metrics)
            .retrievedAt(LocalDateTime.now())
            .build();

        long duration = System.currentTimeMillis() - startTime;
        log.info("[BANK_ADMIN_QUEUE] Queue retrieved in {}ms: {} total items, page {}, {} items returned", 
            duration, totalItems, page, paginatedItems.size());

        return response;
    }

    private boolean matchesFilters(
        Application app,
        List<String> statusFilters,
        List<String> loanTypeFilters,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal amountFrom,
        BigDecimal amountTo
    ) {
        // Status filter
        if (statusFilters != null && !statusFilters.isEmpty()) {
            String appStatus = app.getStatus() != null ? app.getStatus().name() : null;
            if (appStatus == null || !statusFilters.contains(appStatus)) {
                return false;
            }
        }

        // Loan type filter
        if (loanTypeFilters != null && !loanTypeFilters.isEmpty()) {
            String loanType = app.getLoanType();
            if (loanType == null || !loanTypeFilters.contains(loanType)) {
                return false;
            }
        }

        // Date range filter (using submittedAt)
        if (dateFrom != null && app.getSubmittedAt() != null) {
            LocalDateTime dateFromTime = dateFrom.atStartOfDay();
            if (app.getSubmittedAt().isBefore(dateFromTime)) {
                return false;
            }
        }
        if (dateTo != null && app.getSubmittedAt() != null) {
            LocalDateTime dateToTime = dateTo.atTime(LocalTime.MAX);
            if (app.getSubmittedAt().isAfter(dateToTime)) {
                return false;
            }
        }

        // Amount range filter
        if (amountFrom != null && app.getLoanAmount() != null) {
            if (app.getLoanAmount().compareTo(amountFrom) < 0) {
                return false;
            }
        }
        if (amountTo != null && app.getLoanAmount() != null) {
            if (app.getLoanAmount().compareTo(amountTo) > 0) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesSearch(
        Application app,
        String searchApplicationId,
        String searchBorrowerEmail,
        String searchBorrowerName
    ) {
        // If no search criteria, match all
        if (searchApplicationId == null && searchBorrowerEmail == null && searchBorrowerName == null) {
            return true;
        }

        // Search by application ID (exact match)
        if (searchApplicationId != null && !searchApplicationId.isBlank()) {
            if (app.getId().toString().equals(searchApplicationId) ||
                app.getId().toString().contains(searchApplicationId)) {
                return true;
            }
        }

        // Search by borrower email (case-insensitive partial match)
        if (searchBorrowerEmail != null && !searchBorrowerEmail.isBlank() && app.getBorrower() != null) {
            String email = app.getBorrower().getEmail();
            if (email != null && email.toLowerCase().contains(searchBorrowerEmail.toLowerCase())) {
                return true;
            }
        }

        // Search by borrower name (case-insensitive partial match)
        if (searchBorrowerName != null && !searchBorrowerName.isBlank() && app.getBorrower() != null) {
            String fullName = (app.getBorrower().getFirstName() + " " + app.getBorrower().getLastName()).trim();
            if (fullName != null && !fullName.isBlank() && 
                fullName.toLowerCase().contains(searchBorrowerName.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private void applySorting(List<ApplicationQueueItem> items, String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "NEWEST_FIRST";
        }

        Comparator<ApplicationQueueItem> comparator = switch (sortBy.toUpperCase()) {
            case "OLDEST_FIRST" -> Comparator.comparing(ApplicationQueueItem::getSubmittedAt, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "AMOUNT_LOW_HIGH" -> Comparator.comparing(ApplicationQueueItem::getLoanAmount, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "AMOUNT_HIGH_LOW" -> Comparator.comparing(ApplicationQueueItem::getLoanAmount, 
                Comparator.nullsFirst(Comparator.reverseOrder()));
            case "STATUS" -> Comparator.comparing(ApplicationQueueItem::getStatus, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            default -> // NEWEST_FIRST
                Comparator.comparing(ApplicationQueueItem::getSubmittedAt, 
                    Comparator.nullsFirst(Comparator.reverseOrder()));
        };

        items.sort(comparator);
    }

    private ApplicationQueueResponse emptyResponse(Integer page) {
        return ApplicationQueueResponse.builder()
            .applications(Collections.emptyList())
            .totalCount(0)
            .limit(PAGE_SIZE)
            .offset(page * PAGE_SIZE)
            .hasMore(false)
            .queueMetrics(ApplicationQueueMetrics.builder()
                .totalApplications(0)
                .documentsAwaitingReview(0)
                .approvedCount(0)
                .rejectedCount(0)
                .build())
            .retrievedAt(LocalDateTime.now())
            .build();
    }
}
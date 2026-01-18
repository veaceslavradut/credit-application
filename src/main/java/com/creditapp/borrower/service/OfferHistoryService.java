package com.creditapp.borrower.service;

import com.creditapp.borrower.dto.OfferHistoryRecord;
import com.creditapp.borrower.dto.OfferHistoryResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for retrieving borrower's offer history.
 * Provides pagination, sorting, and caching for offer history queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfferHistoryService {
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Get offer history for a borrower with pagination and sorting.
     * 
     * @param borrowerId Borrower ID
     * @param limit Maximum number of records (default 20, max 100)
     * @param offset Pagination offset
     * @param sortBy Sort field (default: "offerReceivedAt")
     * @return OfferHistoryResponse with paginated offers
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "borrowerOfferHistory", key = "#borrowerId")
    public OfferHistoryResponse getOfferHistory(UUID borrowerId, Integer limit, Integer offset, String sortBy) {
        long startTime = System.currentTimeMillis();
        log.debug("[HISTORY] Retrieving offer history for borrower: {}, limit: {}, offset: {}, sortBy: {}", 
            borrowerId, limit, offset, sortBy);
        
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100;
        }
        if (offset == null || offset < 0) {
            offset = 0;
        }
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";
        }

        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);

        // Query offers by borrower using custom repository method
        Page<Offer> offerPage = offerRepository.findOffersByBorrowerId(borrowerId, pageable);
        
        long totalCount = offerPage.getTotalElements();
        
        // Warn if large history detected
        if (totalCount > 1000) {
            log.warn("[HISTORY] Large offer history detected for borrower {}: {} total offers", borrowerId, totalCount);
        }
        
        // Pre-load all related applications and organizations to avoid N+1 queries
        Map<UUID, Application> applicationCache = new HashMap<>();
        Map<UUID, Organization> organizationCache = new HashMap<>();
        
        List<OfferHistoryRecord> records = offerPage.getContent().stream()
            .map(offer -> mapToRecord(offer, applicationCache, organizationCache))
            .collect(Collectors.toList());

        boolean hasMore = (offset + limit) < totalCount;

        long queryTimeMs = System.currentTimeMillis() - startTime;
        log.info("[HISTORY] Retrieved {} offers for borrower {} in {}ms, total: {}, hasMore: {}", 
            records.size(), borrowerId, queryTimeMs, totalCount, hasMore);

        return OfferHistoryResponse.builder()
            .offers(records)
            .totalCount((int) totalCount)
            .limit(limit)
            .offset(offset)
            .hasMore(hasMore)
            .retrievedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Map Offer entity to OfferHistoryRecord DTO.
     * Uses caches to avoid N+1 queries on related entities.
     */
    private OfferHistoryRecord mapToRecord(Offer offer, Map<UUID, Application> appCache, Map<UUID, Organization> orgCache) {
        // Get application details from cache or database
        Application app = appCache.computeIfAbsent(offer.getApplicationId(), id -> 
            applicationRepository.findById(id).orElse(null)
        );

        // Get organization (bank) details from cache or database
        Organization org = orgCache.computeIfAbsent(offer.getBankId(), id -> 
            organizationRepository.findById(id).orElse(null)
        );

        String bankName = org != null ? org.getName() : "Unknown Bank";
        Integer termMonths = app != null ? app.getLoanTermMonths() : null;

        return OfferHistoryRecord.builder()
            .offerId(offer.getId())
            .applicationId(offer.getApplicationId())
            .bankName(bankName)
            .apr(offer.getApr())
            .monthlyPayment(offer.getMonthlyPayment())
            .totalCost(offer.getTotalCost())
            .originationFee(offer.getOriginationFee())
            .insuranceCost(offer.getInsuranceCost())
            .termMonths(termMonths)
            .validityPeriodDays(offer.getValidityPeriodDays())
            .expiresAt(offer.getExpiresAt())
            .offerStatus(offer.getOfferStatus().toString())
            .offerReceivedAt(offer.getCreatedAt())
            .borrowerSelectedAt(offer.getBorrowerSelectedAt())
            .finalAcceptedAt(offer.getOfferSubmittedAt())
            .build();
    }

    /**
     * Invalidate offer history cache for a borrower.
     * Called when new offers are created, modified, or when offer status changes.
     * Task 7: Caching Invalidation Strategy
     * 
     * @param borrowerId Borrower ID whose cache should be cleared
     */
    @CacheEvict(value = "borrowerOfferHistory", key = "#borrowerId")
    public void invalidateOfferHistoryCache(UUID borrowerId) {
        log.debug("Invalidated offer history cache for borrower: {}", borrowerId);
    }
}

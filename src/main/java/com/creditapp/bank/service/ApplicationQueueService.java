package com.creditapp.bank.service;

import com.creditapp.bank.dto.ApplicationQueueItem;
import com.creditapp.bank.dto.ApplicationQueueRequest;
import com.creditapp.bank.dto.ApplicationQueueResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationDocumentRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.ApplicationQueueMetrics;
import com.creditapp.shared.util.PaginationUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationQueueService {
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;

    public ApplicationQueueResponse getApplicationQueue(UUID bankId, ApplicationQueueRequest request) {
        long start = System.currentTimeMillis();
        log.debug("[QUEUE] Querying application queue for bankId={}, request={}", bankId, request);

        // Basic validation
        if (bankId == null) {
            log.warn("[QUEUE] Missing bankId for queue request");
            return emptyResponse(request);
        }
        int limit = safeLimit(request.getLimit());
        int offset = safeOffset(request.getOffset());
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PaginationUtils.validateAndGetPageable(limit, offset, sort);

        Page<Offer> offerPage = offerRepository.findByBankId(bankId, pageable);
        List<Offer> offers = offerPage.getContent();

        // Deduplicate applications preserving order (most recent offer first)
        Set<UUID> applicationIdsOrdered = offers.stream()
            .map(Offer::getApplicationId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<UUID> applicationIds = new ArrayList<>(applicationIdsOrdered);

        if (applicationIds.isEmpty()) {
            ApplicationQueueMetrics metrics = ApplicationQueueMetrics.builder()
                .totalApplications(0)
                .documentsAwaitingReview(0)
                .approvedCount(0)
                .rejectedCount(0)
                .build();
            ApplicationQueueResponse response = ApplicationQueueResponse.builder()
                .applications(Collections.emptyList())
                .totalCount(0)
                .limit(limit)
                .offset(offset)
                .hasMore(false)
                .queueMetrics(metrics)
                .retrievedAt(LocalDateTime.now())
                .build();
            logQueuePerf(start, bankId, response.getTotalCount());
            return response;
        }

        // Load applications
        List<Application> applications = applicationRepository.findAllById(applicationIds);
        Map<UUID, Application> applicationMap = applications.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Application::getId, a -> a));

        // Build queue items
        List<ApplicationQueueItem> items = new ArrayList<>();
        int docsAwaitingReview = 0;
        int approvedCount = 0;
        int rejectedCount = 0;

        for (UUID appId : applicationIds) {
            Application app = applicationMap.get(appId);
            if (app == null) continue;

            Offer selected = selectOfferForBankApp(offers, appId, bankId);
            BigDecimal apr = selected != null ? selected.getApr() : null;
            BigDecimal monthlyPayment = selected != null ? selected.getMonthlyPayment() : null;

            String borrowerName = app.getBorrower() != null
                ? ((app.getBorrower().getFirstName() != null ? app.getBorrower().getFirstName() : "")
                   + " "
                   + (app.getBorrower().getLastName() != null ? app.getBorrower().getLastName() : "")).trim()
                : null;
            String borrowerEmail = app.getBorrower() != null ? app.getBorrower().getEmail() : null;
            String borrowerPhone = app.getBorrower() != null
                ? (app.getBorrower().getPhoneNumber() != null ? app.getBorrower().getPhoneNumber() : app.getBorrower().getPhone())
                : null;

            String documentsStatus = applicationDocumentRepository.findByApplicationIdAndDeletedAtIsNull(appId).isEmpty()
                ? "none" : "submitted";
            String approvalStatus = deriveApprovalStatus(app.getStatus());

            docsAwaitingReview += ("submitted".equals(documentsStatus) && "pending".equals(approvalStatus)) ? 1 : 0;
            approvedCount += ("approved".equals(approvalStatus)) ? 1 : 0;
            rejectedCount += ("rejected".equals(approvalStatus)) ? 1 : 0;

            items.add(ApplicationQueueItem.builder()
                .applicationId(app.getId())
                .referenceNumber(app.getId() != null ? app.getId().toString() : null)
                .borrowerName(borrowerName)
                .borrowerEmail(borrowerEmail)
                .borrowerPhone(borrowerPhone)
                .loanAmount(app.getLoanAmount())
                .termMonths(app.getLoanTermMonths())
                .selectedOfferAPR(apr)
                .selectedOfferMonthlyPayment(monthlyPayment)
                .status(app.getStatus() != null ? app.getStatus().name() : null)
                .receivedAt(app.getCreatedAt())
                .submittedAt(app.getSubmittedAt())
                .lastUpdatedAt(app.getUpdatedAt())
                .documentsStatus(documentsStatus)
                .approvalStatus(approvalStatus)
                .actionItems(Collections.emptyList())
                .build());
        }

        long totalDistinct = Optional.ofNullable(offerRepository.countDistinctApplicationsByBankId(bankId)).orElse(0L);
        boolean hasMore = offset + items.size() < totalDistinct;

        ApplicationQueueMetrics metrics = ApplicationQueueMetrics.builder()
            .totalApplications((int) totalDistinct)
            .documentsAwaitingReview(docsAwaitingReview)
            .approvedCount(approvedCount)
            .rejectedCount(rejectedCount)
            .build();

        ApplicationQueueResponse response = ApplicationQueueResponse.builder()
            .applications(items)
            .totalCount((int) totalDistinct)
            .limit(limit)
            .offset(offset)
            .hasMore(hasMore)
            .queueMetrics(metrics)
            .retrievedAt(LocalDateTime.now())
            .build();

        logQueuePerf(start, bankId, response.getTotalCount());
        return response;
    }

    private ApplicationQueueResponse emptyResponse(ApplicationQueueRequest request) {
        return ApplicationQueueResponse.builder()
            .applications(Collections.<ApplicationQueueItem>emptyList())
            .totalCount(0)
            .limit(safeLimit(request != null ? request.getLimit() : null))
            .offset(safeOffset(request != null ? request.getOffset() : null))
            .hasMore(false)
            .queueMetrics(ApplicationQueueMetrics.builder().build())
            .retrievedAt(LocalDateTime.now())
            .build();
    }

    private int safeLimit(Integer limit) {
        int l = limit == null ? 20 : limit;
        if (l <= 0) l = 20;
        return Math.min(l, 100);
    }

    private int safeOffset(Integer offset) {
        int o = offset == null ? 0 : offset;
        return Math.max(o, 0);
    }

    private void logQueuePerf(long start, UUID bankId, int totalCount) {
        long took = System.currentTimeMillis() - start;
        log.info("[QUEUE] Retrieved {} applications for bankId={} in {}ms", totalCount, bankId, took);
        if (took > 200) {
            log.warn("[QUEUE] Performance warning: query took {}ms (>200ms)", took);
        }
    }

    private Offer selectOfferForBankApp(List<Offer> offersForBankPage, UUID applicationId, UUID bankId) {
        // Prefer accepted offer if it belongs to this bank
        Optional<Offer> acceptedAnyBank = offerRepository.findByApplicationIdAndOfferStatus(applicationId, OfferStatus.ACCEPTED);
        if (acceptedAnyBank.isPresent() && bankId.equals(acceptedAnyBank.get().getBankId())) {
            return acceptedAnyBank.get();
        }
        // Otherwise choose the lowest APR offer from this bank present in page content
        return offersForBankPage.stream()
            .filter(o -> applicationId.equals(o.getApplicationId()))
            .min((a, b) -> a.getApr().compareTo(b.getApr()))
            .orElse(null);
    }

    private String deriveApprovalStatus(ApplicationStatus status) {
        if (status == null) return "pending";
        return switch (status) {
            case ACCEPTED, COMPLETED -> "approved";
            case REJECTED -> "rejected";
            default -> "pending";
        };
    }
}

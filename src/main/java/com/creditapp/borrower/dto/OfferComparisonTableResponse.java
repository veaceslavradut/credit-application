package com.creditapp.borrower.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class OfferComparisonTableResponse {
    private List<OfferComparisonTableRow> offers;
    private Integer totalCount;
    private Integer limit;
    private Integer offset;
    private boolean hasMore;
    private String sortBy;
    private String sortOrder;
    private Map<String, Object> appliedFilters;
    private LocalDateTime retrievedAt;

    // Constructors
    public OfferComparisonTableResponse() {}

    // Getters and Setters
    public List<OfferComparisonTableRow> getOffers() { return offers; }
    public void setOffers(List<OfferComparisonTableRow> offers) { this.offers = offers; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public Map<String, Object> getAppliedFilters() { return appliedFilters; }
    public void setAppliedFilters(Map<String, Object> appliedFilters) { this.appliedFilters = appliedFilters; }

    public LocalDateTime getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(LocalDateTime retrievedAt) { this.retrievedAt = retrievedAt; }
}

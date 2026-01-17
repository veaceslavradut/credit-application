package com.creditapp.borrower.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OfferComparisonResponse {
    public static final String DEFAULT_DISCLAIMER = "These are preliminary offers based on estimated calculations. Final terms may vary after formal review by the bank.";

    private String disclaimer;
    private List<OfferComparisonDTO> offers;
    private UUID applicationId;
    private int totalOffersCount;
    private LocalDateTime retrievedAt;
    private LocalDateTime nextRefreshAvailableAt;

    public OfferComparisonResponse() {
        this.disclaimer = DEFAULT_DISCLAIMER;
        this.offers = new ArrayList<>();
        this.retrievedAt = LocalDateTime.now();
        this.nextRefreshAvailableAt = LocalDateTime.now().plusMinutes(5);
    }

    public OfferComparisonResponse(List<OfferComparisonDTO> offers, UUID applicationId) {
        this();
        this.offers = offers;
        this.applicationId = applicationId;
        this.totalOffersCount = offers.size();
    }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
    public List<OfferComparisonDTO> getOffers() { return offers; }
    public void setOffers(List<OfferComparisonDTO> offers) { this.offers = offers; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public int getTotalOffersCount() { return totalOffersCount; }
    public void setTotalOffersCount(int totalOffersCount) { this.totalOffersCount = totalOffersCount; }
    public LocalDateTime getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(LocalDateTime retrievedAt) { this.retrievedAt = retrievedAt; }
    public LocalDateTime getNextRefreshAvailableAt() { return nextRefreshAvailableAt; }
    public void setNextRefreshAvailableAt(LocalDateTime nextRefreshAvailableAt) { this.nextRefreshAvailableAt = nextRefreshAvailableAt; }
}
package com.creditapp.borrower.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OfferSummaryDTO(
    UUID offerId,
    String bankName,
    BigDecimal apr,
    BigDecimal monthlyPayment,
    BigDecimal totalCost
) {}

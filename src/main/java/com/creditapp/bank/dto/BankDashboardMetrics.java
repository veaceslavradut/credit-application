package com.creditapp.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankDashboardMetrics(
    Integer applicationsReceivedToday,
    Integer applicationsReceivedAll,
    Integer offersSubmitted,
    Integer offersAccepted,
    BigDecimal conversionRate,
    Integer averageTimeToOfferDays,
    LocalDateTime lastUpdated
) {}
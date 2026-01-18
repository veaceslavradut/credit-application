package com.creditapp.bank.dto;

import java.util.List;

public record BankDashboardResponse(
    BankDashboardMetrics metrics,
    List<QuickLink> quickLinks,
    String selectedTimePeriod
) {}
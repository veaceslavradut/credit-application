package com.creditapp.bank.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketAnalysisDTO(
    List<MyBankRateCardDTO> myBankRates,
    List<MarketAverageDTO> marketAverageRates,
    String overallCompetitivePosition,
    LocalDateTime analysisDate,
    Integer bankCount,
    MarketVisualizationDTO visualization
) {}
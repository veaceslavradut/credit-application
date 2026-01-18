package com.creditapp.borrower.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SavingsAnalysisDTO(
    UUID bestOfferId,
    BigDecimal comparedToWorstOffer,
    BigDecimal comparedToAverageOffer,
    String savingsMessage
) {}

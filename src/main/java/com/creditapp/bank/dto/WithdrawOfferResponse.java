package com.creditapp.bank.dto;

import com.creditapp.bank.model.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawOfferResponse {
    private UUID offerId;
    private OfferStatus status;
    private LocalDateTime withdrawnAt;
}
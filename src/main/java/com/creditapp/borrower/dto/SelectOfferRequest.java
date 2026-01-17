package com.creditapp.borrower.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectOfferRequest {

    @NotNull(message = "Offer ID is required")
    private UUID offerId;
}
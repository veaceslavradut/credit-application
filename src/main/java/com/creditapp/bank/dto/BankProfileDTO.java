package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing a bank's profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankProfileDTO {
    private UUID bankId;
    private String name;
    private String registrationNumber;
    private String contactEmail;
    private String phone;
    private BankAddressDTO address;
    private String website;
    private String logoUrl;
    private String rateCardsUrl;
}

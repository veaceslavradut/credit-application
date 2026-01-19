package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;

/**
 * Request DTO for updating bank profile (all fields optional).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankProfileRequest {
    private String name;
    private String registrationNumber;
    private String contactEmail;
    private String phone;
    @Valid
    private BankAddressDTO address;
    private String website;
    private String logoUrl;
}

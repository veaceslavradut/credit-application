package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bank address information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAddressDTO {
    private String street;
    private String city;
    private String state;
    private String zip;
}

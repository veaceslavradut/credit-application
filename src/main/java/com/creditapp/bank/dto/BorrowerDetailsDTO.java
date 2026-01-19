package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for borrower details in application review panel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerDetailsDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
}

package com.creditapp.borrower.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for withdrawing a borrower application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawApplicationRequest {
    @Size(max = 500, message = "Withdrawal reason must not exceed 500 characters")
    private String withdrawalReason;
}

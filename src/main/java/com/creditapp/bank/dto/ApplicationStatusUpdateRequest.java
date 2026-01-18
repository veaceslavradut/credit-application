package com.creditapp.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusUpdateRequest {
    @NotNull(message = "Application status is required")
    private String status;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String comments;
}
package com.creditapp.borrower.dto;

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
public class SubmitApplicationResponse {
    private UUID id;
    private String status;
    private LocalDateTime submittedAt;
    private String message;
    private ApplicationDTO application;
}
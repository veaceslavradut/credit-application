package com.creditapp.compliance.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AuditLogFilterDTO {
    private UUID userId;
    private String action;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String result; // SUCCESS or FAILURE
    private Integer page = 0;
    private Integer size = 100;
}

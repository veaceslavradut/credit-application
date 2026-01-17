package com.creditapp.bank.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class OfferCalculationLogDTO {
    public Long id;
    public UUID applicationId;
    public UUID bankId;
    public String calculationMethod;
    public String calculationType;
    public LocalDateTime timestamp;
}
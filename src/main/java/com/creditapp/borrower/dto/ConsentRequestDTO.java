package com.creditapp.borrower.dto;

import com.creditapp.shared.model.ConsentType;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRequestDTO {
    
    @NotEmpty(message = "Consent types cannot be empty")
    private List<ConsentType> consentTypes;
}
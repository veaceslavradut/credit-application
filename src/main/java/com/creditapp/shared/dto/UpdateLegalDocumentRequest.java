package com.creditapp.shared.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating legal documents
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLegalDocumentRequest {
    
    @NotBlank(message = "Content cannot be blank")
    private String content;
    
    @Builder.Default
    private Boolean isMaterialChange = false;
}
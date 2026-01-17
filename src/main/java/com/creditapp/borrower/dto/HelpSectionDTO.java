package com.creditapp.borrower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpSectionDTO {
    private String heading;
    private String content;
    private Integer order;
}

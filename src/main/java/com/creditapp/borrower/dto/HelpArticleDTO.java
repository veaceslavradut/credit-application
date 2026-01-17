package com.creditapp.borrower.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpArticleDTO {
    private UUID id;
    private String topic;
    private String title;
    private String description;
    private String content;
    private Integer version;
    private String language;
    private List<HelpSectionDTO> sections;
    private List<HelpFAQDTO> faqs;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastUpdated;
}

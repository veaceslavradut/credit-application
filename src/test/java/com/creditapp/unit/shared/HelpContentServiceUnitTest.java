package com.creditapp.unit.shared;

import com.creditapp.borrower.dto.HelpArticleDTO;
import com.creditapp.borrower.exception.HelpArticleNotFoundException;
import com.creditapp.shared.model.HelpArticle;
import com.creditapp.shared.model.HelpArticleStatus;
import com.creditapp.shared.repository.HelpArticleRepository;
import com.creditapp.shared.service.HelpContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class HelpContentServiceUnitTest {

    private HelpArticleRepository repository;
    private HelpContentService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(HelpArticleRepository.class);
        service = new HelpContentService(repository);
    }

    @Test
    void getHelpArticle_ReturnsArticle_WhenLanguageMatch() {
        HelpArticle article = HelpArticle.builder()
                .id(UUID.randomUUID())
                .topic("loan-types")
                .title("Loan Types Overview")
                .description("Overview")
                .content("Personal, Home, Auto")
                .version(1)
                .language("en")
                .status(HelpArticleStatus.PUBLISHED)
                .build();
        Mockito.when(repository.findByTopicAndLanguageAndStatus(eq("loan-types"), eq("en"), eq(HelpArticleStatus.PUBLISHED)))
                .thenReturn(Optional.of(article));

        HelpArticleDTO dto = service.getHelpArticle("loan-types", "en");
        assertNotNull(dto);
        assertEquals("loan-types", dto.getTopic());
        assertEquals("Loan Types Overview", dto.getTitle());
    }

    @Test
    void getHelpArticle_FallsBackToEnglish_WhenLanguageUnavailable() {
        HelpArticle article = HelpArticle.builder()
                .id(UUID.randomUUID())
                .topic("loan-types")
                .title("Loan Types Overview")
                .description("Overview")
                .content("Personal, Home, Auto")
                .version(1)
                .language("en")
                .status(HelpArticleStatus.PUBLISHED)
                .build();
        Mockito.when(repository.findByTopicAndLanguageAndStatus(eq("loan-types"), eq("es"), eq(HelpArticleStatus.PUBLISHED)))
                .thenReturn(Optional.empty());
        Mockito.when(repository.findByTopicAndLanguageAndStatus(eq("loan-types"), eq("en"), eq(HelpArticleStatus.PUBLISHED)))
                .thenReturn(Optional.of(article));

        HelpArticleDTO dto = service.getHelpArticle("loan-types", "es");
        assertEquals("en", dto.getLanguage());
    }

    @Test
    void getHelpArticle_Throws_WhenTopicNotFound() {
        Mockito.when(repository.findByTopicAndLanguageAndStatus(any(), any(), any()))
                .thenReturn(Optional.empty());
        Mockito.when(repository.findByTopicAndLanguageAndStatus(any(), eq("en"), any()))
                .thenReturn(Optional.empty());

        assertThrows(HelpArticleNotFoundException.class, () -> service.getHelpArticle("unknown", "en"));
    }
}

package com.creditapp.shared.service;

import com.creditapp.borrower.dto.*;
import com.creditapp.borrower.exception.HelpArticleNotFoundException;
import com.creditapp.shared.model.HelpArticle;
import com.creditapp.shared.model.HelpArticleStatus;
import com.creditapp.shared.model.HelpFAQ;
import com.creditapp.shared.model.HelpSection;
import com.creditapp.shared.repository.HelpArticleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HelpContentService {
    private final HelpArticleRepository helpArticleRepository;

    public HelpContentService(HelpArticleRepository helpArticleRepository) {
        this.helpArticleRepository = helpArticleRepository;
    }

    @Cacheable(value = "helpArticles", key = "#topic + ':' + #language")
    public HelpArticleDTO getHelpArticle(String topic, String language) {
        Optional<HelpArticle> articleOpt = helpArticleRepository.findByTopicAndLanguageAndStatus(topic, language, HelpArticleStatus.PUBLISHED);
        HelpArticle article = articleOpt.orElseGet(() -> helpArticleRepository
                .findByTopicAndLanguageAndStatus(topic, "en", HelpArticleStatus.PUBLISHED)
                .orElseThrow(() -> new HelpArticleNotFoundException(topic, language)));

        return toDto(article);
    }

    public List<HelpArticleListDTO> listHelpTopics(String language) {
        Page<HelpArticle> page = helpArticleRepository.findByLanguageAndStatus(language, HelpArticleStatus.PUBLISHED, PageRequest.of(0, 100));
        return page.getContent().stream()
                .map(a -> HelpArticleListDTO.builder()
                        .topic(a.getTopic())
                        .title(a.getTitle())
                        .description(a.getDescription())
                        .language(a.getLanguage())
                        .build())
                .collect(Collectors.toList());
    }

    public List<HelpArticleDTO> getHelpArticlesByTopic(String topic) {
        return helpArticleRepository.findByTopic(topic).stream()
                .filter(a -> a.getStatus() == HelpArticleStatus.PUBLISHED)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private HelpArticleDTO toDto(HelpArticle article) {
        List<HelpSectionDTO> sections = Optional.ofNullable(article.getSections()).orElse(List.of()).stream()
                .sorted((s1, s2) -> Integer.compare(s1.getDisplayOrder(), s2.getDisplayOrder()))
                .map(s -> HelpSectionDTO.builder()
                        .heading(s.getHeading())
                        .content(s.getContent())
                        .order(s.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        List<HelpFAQDTO> faqs = Optional.ofNullable(article.getFaqs()).orElse(List.of()).stream()
                .sorted((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
                .map(f -> HelpFAQDTO.builder()
                        .question(f.getQuestion())
                        .answer(f.getAnswer())
                        .order(f.getOrder())
                        .build())
                .collect(Collectors.toList());

        return HelpArticleDTO.builder()
                .id(article.getId())
                .topic(article.getTopic())
                .title(article.getTitle())
                .description(article.getDescription())
                .content(article.getContent())
                .version(article.getVersion())
                .language(article.getLanguage())
                .sections(sections)
                .faqs(faqs)
                .lastUpdated(article.getUpdatedAt())
                .build();
    }
}

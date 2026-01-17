package com.creditapp.shared.repository;

import com.creditapp.shared.model.HelpArticle;
import com.creditapp.shared.model.HelpArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HelpArticleRepository extends JpaRepository<HelpArticle, UUID> {
    Optional<HelpArticle> findByTopicAndLanguageAndStatus(String topic, String language, HelpArticleStatus status);
    
    Page<HelpArticle> findByLanguageAndStatus(String language, HelpArticleStatus status, Pageable pageable);
    
    List<HelpArticle> findByTopic(String topic);
}

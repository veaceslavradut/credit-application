package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.HelpArticleDTO;
import com.creditapp.borrower.dto.HelpArticleListDTO;
import com.creditapp.shared.service.HelpContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelpContentController {

    private final HelpContentService helpContentService;

    public HelpContentController(HelpContentService helpContentService) {
        this.helpContentService = helpContentService;
    }

    @GetMapping("/api/help/topics")
    public ResponseEntity<List<HelpArticleListDTO>> listTopics(@RequestParam(name = "language", defaultValue = "en") String language) {
        return ResponseEntity.ok(helpContentService.listHelpTopics(language));
    }

    @GetMapping("/api/help/{topic}")
    public ResponseEntity<HelpArticleDTO> getArticle(@PathVariable("topic") String topic,
                                                     @RequestParam(name = "language", defaultValue = "en") String language) {
        return ResponseEntity.ok(helpContentService.getHelpArticle(topic, language));
    }
}

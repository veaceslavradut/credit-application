package com.creditapp.integration.borrower;

import com.creditapp.borrower.dto.HelpArticleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.UUID;
import com.creditapp.shared.model.HelpArticle;
import com.creditapp.shared.model.HelpArticleStatus;
import com.creditapp.shared.repository.HelpArticleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HelpContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HelpArticleRepository helpArticleRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        helpArticleRepository.deleteAll();
        
        HelpArticle loanTypes = new HelpArticle();
        loanTypes.setId(UUID.randomUUID());
        loanTypes.setTopic("loan-types");
        loanTypes.setTitle("Loan Types");
        loanTypes.setContent("Different types of loans available");
        loanTypes.setLanguage("en");
        loanTypes.setDescription("Learn about different loan types");
        loanTypes.setStatus(HelpArticleStatus.PUBLISHED);
        loanTypes.setVersion(1);
        loanTypes.setCreatedAt(LocalDateTime.now());
        loanTypes.setUpdatedAt(LocalDateTime.now());
        helpArticleRepository.save(loanTypes);
    }

    @Test
    void listTopics_Returns200AndList() throws Exception {
        mockMvc.perform(get("/api/help/topics").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].topic").exists());
    }

    @Test
    void getArticle_Returns200_ForLoanTypes() throws Exception {
        mockMvc.perform(get("/api/help/loan-types").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("loan-types"))
                .andExpect(jsonPath("$.title").exists());
    }
}

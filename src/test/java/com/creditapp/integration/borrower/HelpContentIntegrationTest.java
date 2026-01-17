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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HelpContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

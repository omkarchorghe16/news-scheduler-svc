package com.stocknews.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DigestTriggerController endpoints.
 * Verifies REST endpoints respond correctly and return expected status codes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.notifications.slack.webhook-url=",
        "app.notifications.telegram.bot-token=",
        "app.notifications.telegram.chat-id=",
        "app.apis.finnhub-key=",
        "app.apis.newsapi-key="
})
class DigestTriggerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/digest/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock News Scheduler Service is running"));
    }

    @Test
    void testDailyDigestEndpointExists() throws Exception {
        mockMvc.perform(post("/api/digest/run"))
                .andExpect(status().isOk());
    }

    @Test
    void testPortfolioDigestEndpointExists() throws Exception {
        mockMvc.perform(post("/api/digest/portfolio"))
                .andExpect(status().isOk());
    }
}

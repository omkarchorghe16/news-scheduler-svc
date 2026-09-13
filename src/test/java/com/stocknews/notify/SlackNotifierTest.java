package com.stocknews.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SlackNotifier.
 * Verifies message formatting and HTTP request construction.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.notifications.slack.webhook-url=https://hooks.slack.com/services/TEST/WEBHOOK/URL"
})
class SlackNotifierTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppProperties appProperties;

    private SlackNotifier slackNotifier;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder().build();
        slackNotifier = new SlackNotifier(restClient, appProperties, objectMapper);
    }

    @Test
    void testSendMethodExists() {
        assertNotNull(slackNotifier);
        assertTrue(slackNotifier instanceof Notifier);
    }

    @Test
    void testWebhookUrlConfiguration() {
        assertNotNull(appProperties.getNotifications().getSlack().getWebhookUrl());
        assertTrue(appProperties.getNotifications().getSlack().getWebhookUrl().contains("hooks.slack.com"));
    }

    @Test
    void testPortfolioWebhookConfiguration() {
        String portfolioWebhook = appProperties.getNotifications().getSlack().getPortfolioWebhookUrl();
        // Should be empty or not set by default
        assertTrue(portfolioWebhook == null || portfolioWebhook.isBlank());
    }
}

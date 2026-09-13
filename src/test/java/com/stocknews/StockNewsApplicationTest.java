package com.stocknews;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test: Spring context loads successfully.
 * Confirms all beans, configurations, and dependencies are wired correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.notifications.slack.webhook-url=",
        "app.notifications.telegram.bot-token=",
        "app.notifications.telegram.chat-id=",
        "app.apis.finnhub-key=",
        "app.apis.newsapi-key="
})
class StockNewsApplicationTest {
    @Test
    void contextLoads() {
        // If we get here without exceptions, Spring context loaded successfully
    }
}

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
 * Unit tests for TelegramNotifier.
 * Verifies configuration and notifier interface contract.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.notifications.telegram.bot-token=TEST_BOT_TOKEN",
        "app.notifications.telegram.chat-id=123456789"
})
class TelegramNotifierTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppProperties appProperties;

    private TelegramNotifier telegramNotifier;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder().build();
        telegramNotifier = new TelegramNotifier(restClient, appProperties, objectMapper);
    }

    @Test
    void testTelegramNotifierInstance() {
        assertNotNull(telegramNotifier);
        assertTrue(telegramNotifier instanceof Notifier);
    }

    @Test
    void testBotTokenConfiguration() {
        assertNotNull(appProperties.getNotifications().getTelegram().getBotToken());
        assertEquals("TEST_BOT_TOKEN", appProperties.getNotifications().getTelegram().getBotToken());
    }

    @Test
    void testChatIdConfiguration() {
        assertNotNull(appProperties.getNotifications().getTelegram().getChatId());
        assertEquals("123456789", appProperties.getNotifications().getTelegram().getChatId());
    }
}

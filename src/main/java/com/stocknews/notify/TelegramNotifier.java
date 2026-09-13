package com.stocknews.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * Telegram notifier.
 * Sends formatted messages to Telegram via Telegram Bot API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotifier implements Notifier {
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final String SEND_MESSAGE_ENDPOINT = "/sendMessage";

    private final RestClient restClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean send(String channelKey, String message) {
        String botToken = appProperties.getNotifications().getTelegram().getBotToken();
        String chatId = appProperties.getNotifications().getTelegram().getChatId();

        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.warn("Telegram bot token or chat ID not configured");
            return false;
        }

        try {
            String url = TELEGRAM_API_BASE + botToken + SEND_MESSAGE_ENDPOINT;
            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", message);
            payload.put("parse_mode", "Markdown");

            restClient.post()
                    .uri(url)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Telegram message sent successfully to chat {}", chatId);
            return true;
        } catch (RestClientException e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
            return false;
        }
    }
}

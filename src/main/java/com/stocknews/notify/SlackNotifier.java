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
 * Slack notifier.
 * Sends formatted messages to Slack via Incoming Webhook URLs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlackNotifier implements Notifier {
    private final RestClient restClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean send(String channelKey, String message) {
        String webhookUrl = getWebhookUrl(channelKey);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Slack webhook URL not configured for channel {}", channelKey);
            return false;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", message);
            payload.put("mrkdwn", true);

            restClient.post()
                    .uri(webhookUrl)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Slack message sent successfully to channel {}", channelKey);
            return true;
        } catch (RestClientException e) {
            log.error("Failed to send Slack message to channel {}: {}", channelKey, e.getMessage());
            return false;
        }
    }

    private String getWebhookUrl(String channelKey) {
        if ("portfolio".equalsIgnoreCase(channelKey)) {
            return appProperties.getNotifications().getSlack().getPortfolioWebhookUrl();
        }
        return appProperties.getNotifications().getSlack().getWebhookUrl();
    }
}

package com.stocknews.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * WhatsApp notifier using Twilio API.
 * NOTE: WhatsApp has strict template approval requirements for messages sent outside a 24-hour
 * session window. Free sandbox deployments require message templates to be pre-approved by Twilio.
 * This implementation is configurable (notifications.whatsapp.enabled: false by default) to handle
 * these limitations gracefully.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotifier implements Notifier {
    private static final String TWILIO_API_BASE = "https://api.twilio.com/2010-04-01/Accounts";
    private static final String TWILIO_MESSAGES_ENDPOINT = "/Messages.json";

    private final RestClient restClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean send(String channelKey, String message) {
        AppProperties.NotificationProperties.WhatsAppProperties whatsapp = appProperties.getNotifications().getWhatsapp();

        if (!whatsapp.isEnabled()) {
            log.debug("WhatsApp notifications are disabled");
            return false;
        }

        String sid = whatsapp.getTwilioSid();
        String token = whatsapp.getTwilioToken();
        String fromNumber = whatsapp.getFromNumber();
        String toNumber = whatsapp.getToNumber();

        if (sid == null || sid.isBlank() || token == null || token.isBlank() ||
            fromNumber == null || fromNumber.isBlank() || toNumber == null || toNumber.isBlank()) {
            log.warn("WhatsApp Twilio credentials not fully configured");
            return false;
        }

        try {
            String url = String.format("%s/%s%s", TWILIO_API_BASE, sid, TWILIO_MESSAGES_ENDPOINT);
            Map<String, Object> payload = new HashMap<>();
            payload.put("From", fromNumber);
            payload.put("To", toNumber);
            payload.put("Body", message);

            String authHeader = basicAuthHeader(sid, token);
            restClient.post()
                    .uri(url)
                    .header("Authorization", authHeader)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("WhatsApp message sent successfully to {}", toNumber);
            return true;
        } catch (RestClientException e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage());
            return false;
        }
    }

    private String basicAuthHeader(String sid, String token) {
        String credentials = sid + ":" + token;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}

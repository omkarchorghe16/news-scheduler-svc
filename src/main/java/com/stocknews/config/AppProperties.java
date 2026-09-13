package com.stocknews.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Root configuration properties bound from application.yml.
 * Centralizes all external config (secrets, API keys, watchlists) in one place.
 */
@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private NotificationProperties notifications = new NotificationProperties();
    private ApisProperties apis = new ApisProperties();
    private WatchlistProperties watchlist = new WatchlistProperties();
    private ScheduleProperties schedule = new ScheduleProperties();

    @Data
    public static class NotificationProperties {
        private SlackProperties slack = new SlackProperties();
        private TelegramProperties telegram = new TelegramProperties();
        private WhatsAppProperties whatsapp = new WhatsAppProperties();

        @Data
        public static class SlackProperties {
            private String webhookUrl;
            private String portfolioWebhookUrl;
        }

        @Data
        public static class TelegramProperties {
            private String botToken;
            private String chatId;
        }

        @Data
        public static class WhatsAppProperties {
            private boolean enabled = false;
            private String twilioSid;
            private String twilioToken;
            private String fromNumber;
            private String toNumber;
        }
    }

    @Data
    public static class ApisProperties {
        private String finnhubKey;
        private String newsapiKey;
        private int timeoutSeconds = 5;
        private int maxRetries = 1;
    }

    @Data
    public static class WatchlistProperties {
        private List<String> usSymbols = List.of("AAPL", "GOOGL", "MSFT", "TSLA", "META");
        private List<String> nseSymbols = List.of("RELIANCE.NS", "INFY.NS", "TCS.NS", "WIPRO.NS", "LT.NS");
        private List<String> portfolioSymbols = List.of();
    }

    @Data
    public static class ScheduleProperties {
        private String cronExpression = "0 0 9 * * MON-FRI"; // Default: 9 AM weekdays America/Chicago
        private String timezone = "America/Chicago";
    }
}

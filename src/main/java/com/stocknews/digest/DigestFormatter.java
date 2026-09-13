package com.stocknews.digest;

import com.stocknews.news.NewsItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formats news items into channel-appropriate messages.
 * Supports Markdown for Telegram, JSON for Slack, plain text for WhatsApp.
 */
@Component
@Slf4j
public class DigestFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Format news items as Markdown for Telegram.
     */
    public String formatForTelegram(List<NewsItem> items, String market) {
        if (items.isEmpty()) {
            return String.format("📈 *%s Market Digest*\n\nNo new articles found today.", market);
        }

        String newsLines = items.stream()
                .map(item -> formatItemMarkdown(item))
                .collect(Collectors.joining("\n\n"));

        return String.format("📈 *%s Market Digest*\n\n%s", market, newsLines);
    }

    /**
     * Format news items for Slack.
     */
    public String formatForSlack(List<NewsItem> items, String market) {
        if (items.isEmpty()) {
            return String.format(":chart_with_upwards_trend: %s Market Digest\n\nNo new articles found today.", market);
        }

        String newsLines = items.stream()
                .map(item -> formatItemSlack(item))
                .collect(Collectors.joining("\n"));

        return String.format(":chart_with_upwards_trend: *%s Market Digest*\n\n%s", market, newsLines);
    }

    /**
     * Format news items as plain text for WhatsApp.
     */
    public String formatForWhatsApp(List<NewsItem> items, String market) {
        if (items.isEmpty()) {
            return String.format("%s Market Digest\n\nNo new articles found today.", market);
        }

        String newsLines = items.stream()
                .map(this::formatItemPlainText)
                .collect(Collectors.joining("\n\n"));

        return String.format("%s Market Digest\n\n%s", market, newsLines);
    }

    private String formatItemMarkdown(NewsItem item) {
        String title = item.title();
        String url = item.url();
        String source = item.source();
        String publishedAt = item.publishedAt().format(DATE_FORMATTER);
        return String.format("[%s](%s)\n_Source: %s | %s_", title, url, source, publishedAt);
    }

    private String formatItemSlack(NewsItem item) {
        String title = item.title();
        String url = item.url();
        String source = item.source();
        String publishedAt = item.publishedAt().format(DATE_FORMATTER);
        return String.format("• <%s|%s> (Source: %s | %s)", url, title, source, publishedAt);
    }

    private String formatItemPlainText(NewsItem item) {
        String title = item.title();
        String url = item.url();
        String source = item.source();
        String publishedAt = item.publishedAt().format(DATE_FORMATTER);
        return String.format("• %s\nLink: %s\nSource: %s | %s", title, url, source, publishedAt);
    }
}

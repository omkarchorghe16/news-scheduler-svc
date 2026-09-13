package com.stocknews.digest;

import com.stocknews.news.NewsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DigestFormatter.
 * Verifies that news items are formatted correctly for different channels.
 */
@SpringBootTest
class DigestFormatterTest {
    private DigestFormatter formatter;
    private List<NewsItem> testItems;

    @BeforeEach
    void setUp() {
        formatter = new DigestFormatter();
        testItems = List.of(
                new NewsItem(
                        "Apple Stock Rises 5%",
                        "https://example.com/apple-5pct",
                        "Reuters",
                        LocalDateTime.of(2024, 9, 13, 10, 30),
                        "AAPL",
                        "US"
                ),
                new NewsItem(
                        "Tech Sector Shows Strength",
                        "https://example.com/tech-strength",
                        "Bloomberg",
                        LocalDateTime.of(2024, 9, 13, 11, 0),
                        null,
                        "US"
                )
        );
    }

    @Test
    void testFormatForTelegram() {
        String result = formatter.formatForTelegram(testItems, "US");
        assertNotNull(result);
        assertTrue(result.contains("US Market Digest"));
        assertTrue(result.contains("Apple Stock Rises 5%"));
        assertTrue(result.contains("Tech Sector Shows Strength"));
        assertTrue(result.contains("https://example.com/apple-5pct"));
        assertTrue(result.contains("Reuters"));
        assertTrue(result.contains("2024-09-13"));
    }

    @Test
    void testFormatForSlack() {
        String result = formatter.formatForSlack(testItems, "US");
        assertNotNull(result);
        assertTrue(result.contains("US Market Digest"));
        assertTrue(result.contains("Apple Stock Rises 5%"));
        assertTrue(result.contains("Tech Sector Shows Strength"));
        assertTrue(result.contains("https://example.com/apple-5pct"));
    }

    @Test
    void testFormatForWhatsApp() {
        String result = formatter.formatForWhatsApp(testItems, "US");
        assertNotNull(result);
        assertTrue(result.contains("US Market Digest"));
        assertTrue(result.contains("Apple Stock Rises 5%"));
        assertTrue(result.contains("Tech Sector Shows Strength"));
    }

    @Test
    void testFormatEmptyList() {
        String result = formatter.formatForTelegram(List.of(), "US");
        assertNotNull(result);
        assertTrue(result.contains("No new articles found"));
    }

    @Test
    void testFormatIndiaMarket() {
        String result = formatter.formatForTelegram(testItems, "India");
        assertNotNull(result);
        assertTrue(result.contains("India Market Digest"));
    }
}

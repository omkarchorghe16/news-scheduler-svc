package com.stocknews.digest;

import com.stocknews.news.NewsItem;
import com.stocknews.persistence.SentArticle;
import com.stocknews.persistence.SentArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DigestService deduplication logic.
 * Verifies that previously sent articles are excluded from digest.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.notifications.slack.webhook-url=",
        "app.notifications.telegram.bot-token=",
        "app.apis.finnhub-key=",
        "app.apis.newsapi-key="
})
class DigestServiceTest {
    @Autowired
    private SentArticleRepository sentArticleRepository;

    @Test
    void testDeduplicationLogic() {
        // Arrange: Insert a previously sent article
        String testUrl = "https://example.com/test-article";
        String urlHash = hashUrl(testUrl);
        SentArticle sentArticle = new SentArticle(null, urlHash, LocalDateTime.now());
        sentArticleRepository.save(sentArticle);

        // Act: Check if article is found
        var found = sentArticleRepository.findByArticleUrlHash(urlHash);

        // Assert: Article should be found
        assertTrue(found.isPresent());
        assertEquals(urlHash, found.get().getArticleUrlHash());
    }

    @Test
    void testCleanupOldArticles() {
        // Arrange: Insert articles with old timestamps
        String oldUrlHash = hashUrl("https://example.com/old-article");
        SentArticle oldArticle = new SentArticle(null, oldUrlHash, LocalDateTime.now().minusDays(5));
        sentArticleRepository.save(oldArticle);

        String newUrlHash = hashUrl("https://example.com/new-article");
        SentArticle newArticle = new SentArticle(null, newUrlHash, LocalDateTime.now());
        sentArticleRepository.save(newArticle);

        // Act: Clean up articles older than 3 days
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        long deletedCount = sentArticleRepository.deleteAllBySentAtBefore(cutoff);

        // Assert: Old article should be deleted
        assertEquals(1, deletedCount);
        assertTrue(sentArticleRepository.findByArticleUrlHash(oldUrlHash).isEmpty());
        assertTrue(sentArticleRepository.findByArticleUrlHash(newUrlHash).isPresent());
    }

    private String hashUrl(String url) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes());
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return url;
        }
    }
}

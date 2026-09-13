package com.stocknews.digest;

import com.stocknews.news.FinnhubNewsClient;
import com.stocknews.news.NewsApiClient;
import com.stocknews.news.NewsItem;
import com.stocknews.notify.SlackNotifier;
import com.stocknews.notify.TelegramNotifier;
import com.stocknews.notify.WhatsAppNotifier;
import com.stocknews.persistence.SentArticle;
import com.stocknews.persistence.SentArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DigestService orchestrates the complete digest workflow:
 * 1. Fetch news from multiple sources (US and India markets separately)
 * 2. Deduplicate against previously sent articles (3-day window)
 * 3. Format into channel-appropriate messages
 * 4. Send to configured notification channels
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DigestService {
    private static final int DEDUP_DAYS = 3;

    private final FinnhubNewsClient finnhubNewsClient;
    private final NewsApiClient newsApiClient;
    private final SlackNotifier slackNotifier;
    private final TelegramNotifier telegramNotifier;
    private final WhatsAppNotifier whatsAppNotifier;
    private final DigestFormatter digestFormatter;
    private final Watchlist watchlist;
    private final SentArticleRepository sentArticleRepository;

    /**
     * Execute the full daily digest: fetch, deduplicate, format, and send.
     */
    public void runDailyDigest() {
        log.info("Starting daily digest job");
        watchlist.logWatchlist();

        // Fetch US market news
        log.info("Fetching US market news...");
        List<NewsItem> usNews = new ArrayList<>();
        usNews.addAll(finnhubNewsClient.fetchGeneralMarketNews("US"));
        usNews.addAll(newsApiClient.fetchGeneralMarketNews("US"));
        log.info("Fetched {} US news items", usNews.size());

        // Fetch India market news
        log.info("Fetching India market news...");
        List<NewsItem> indiaNews = new ArrayList<>();
        indiaNews.addAll(finnhubNewsClient.fetchGeneralMarketNews("INDIA"));
        indiaNews.addAll(newsApiClient.fetchGeneralMarketNews("INDIA"));
        log.info("Fetched {} India news items", indiaNews.size());

        // Deduplicate
        usNews = deduplicateNews(usNews);
        indiaNews = deduplicateNews(indiaNews);
        log.info("After dedup: {} US items, {} India items", usNews.size(), indiaNews.size());

        // Format
        String usDigest = digestFormatter.formatForTelegram(usNews, "US");
        String indiaDigest = digestFormatter.formatForTelegram(indiaNews, "India");

        // Send to Telegram
        log.info("Sending US digest to Telegram");
        telegramNotifier.send("default", usDigest);
        log.info("Sending India digest to Telegram");
        telegramNotifier.send("default", indiaDigest);

        // Send to Slack
        log.info("Sending US digest to Slack");
        slackNotifier.send("default", digestFormatter.formatForSlack(usNews, "US"));
        log.info("Sending India digest to Slack");
        slackNotifier.send("default", digestFormatter.formatForSlack(indiaNews, "India"));

        // Send to WhatsApp (if enabled)
        if (whatsAppNotifier.send("default", digestFormatter.formatForWhatsApp(usNews, "US"))) {
            log.info("Sent US digest to WhatsApp");
        }
        if (whatsAppNotifier.send("default", digestFormatter.formatForWhatsApp(indiaNews, "India"))) {
            log.info("Sent India digest to WhatsApp");
        }

        log.info("Daily digest job completed");
    }

    /**
     * Execute portfolio-specific digest: fetch, deduplicate, format for portfolio webhook, and send.
     */
    public void runPortfolioDigest() {
        log.info("Starting portfolio digest job");
        List<String> portfolioSymbols = watchlist.getPortfolioSymbols();
        if (portfolioSymbols.isEmpty()) {
            log.info("No portfolio symbols configured, skipping");
            return;
        }

        List<NewsItem> portfolioNews = new ArrayList<>();
        for (String symbol : portfolioSymbols) {
            log.info("Fetching news for portfolio symbol: {}", symbol);
            portfolioNews.addAll(finnhubNewsClient.fetchNewsForSymbol(symbol));
        }
        log.info("Fetched {} portfolio news items", portfolioNews.size());

        portfolioNews = deduplicateNews(portfolioNews);
        log.info("After dedup: {} portfolio items", portfolioNews.size());

        if (!portfolioNews.isEmpty()) {
            String portfolioDigest = digestFormatter.formatForSlack(portfolioNews, "Portfolio");
            log.info("Sending portfolio digest to Slack");
            slackNotifier.send("portfolio", portfolioDigest);
        }

        log.info("Portfolio digest job completed");
    }

    /**
     * Deduplicate news items: filter out articles sent in the last DEDUP_DAYS.
     */
    private List<NewsItem> deduplicateNews(List<NewsItem> items) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(DEDUP_DAYS);
        int originalSize = items.size();

        List<NewsItem> deduped = items.stream()
                .filter(item -> !isArticleSent(item))
                .collect(Collectors.toList());

        int filtered = originalSize - deduped.size();
        log.debug("Filtered {} articles already sent within last {} days", filtered, DEDUP_DAYS);

        // Mark new articles as sent
        deduped.forEach(this::markArticleAsSent);

        return deduped;
    }

    private boolean isArticleSent(NewsItem item) {
        String urlHash = hashUrl(item.url());
        return sentArticleRepository.findByArticleUrlHash(urlHash).isPresent();
    }

    private void markArticleAsSent(NewsItem item) {
        String urlHash = hashUrl(item.url());
        SentArticle sentArticle = new SentArticle(null, urlHash, LocalDateTime.now());
        sentArticleRepository.save(sentArticle);
    }

    private String hashUrl(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return url; // Fallback: use URL as-is
        }
    }
}

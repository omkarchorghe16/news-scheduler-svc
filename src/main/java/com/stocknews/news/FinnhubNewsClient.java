package com.stocknews.news;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Finnhub news client.
 * Fetches company-specific and general market news from Finnhub API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FinnhubNewsClient implements NewsSourceClient {
    private static final String FINNHUB_BASE_URL = "https://finnhub.io/api/v1";
    private static final String COMPANY_NEWS_ENDPOINT = "/company-news";
    private static final String GENERAL_NEWS_ENDPOINT = "/news";

    private final RestClient restClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Override
    public List<NewsItem> fetchNewsForSymbol(String symbol) {
        String apiKey = appProperties.getApis().getFinnhubKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Finnhub API key not configured, skipping symbol news fetch for {}", symbol);
            return List.of();
        }

        try {
            String url = String.format("%s%s?symbol=%s&token=%s", FINNHUB_BASE_URL, COMPANY_NEWS_ENDPOINT, symbol, apiKey);
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return parseNewsResponse(response, symbol, "US");
        } catch (RestClientException e) {
            log.error("Failed to fetch Finnhub news for symbol {}: {}", symbol, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<NewsItem> fetchGeneralMarketNews(String market) {
        String apiKey = appProperties.getApis().getFinnhubKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Finnhub API key not configured, skipping general market news fetch");
            return List.of();
        }

        try {
            String category = "general";
            String url = String.format("%s%s?category=%s&token=%s", FINNHUB_BASE_URL, GENERAL_NEWS_ENDPOINT, category, apiKey);
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return parseNewsResponse(response, null, market);
        } catch (RestClientException e) {
            log.error("Failed to fetch Finnhub general market news: {}", e.getMessage());
            return List.of();
        }
    }

    private List<NewsItem> parseNewsResponse(String jsonResponse, String symbol, String market) {
        List<NewsItem> items = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode newsArray = root.get("news");
            if (newsArray == null || !newsArray.isArray()) {
                log.debug("No news array found in Finnhub response");
                return items;
            }

            int count = 0;
            for (JsonNode node : newsArray) {
                if (count >= 10) break;
                String title = node.get("headline") != null ? node.get("headline").asText() : "";
                String url = node.get("url") != null ? node.get("url").asText() : "";
                String source = node.get("source") != null ? node.get("source").asText() : "Finnhub";
                long timestamp = node.get("datetime") != null ? node.get("datetime").asLong() : 0;
                LocalDateTime publishedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp),
                        ZoneId.systemDefault()
                );

                if (!title.isBlank() && !url.isBlank()) {
                    items.add(new NewsItem(title, url, source, publishedAt, symbol, market));
                    count++;
                }
            }
            log.debug("Parsed {} news items from Finnhub for market {}", items.size(), market);
        } catch (Exception e) {
            log.error("Error parsing Finnhub response: {}", e.getMessage());
        }
        return items;
    }
}

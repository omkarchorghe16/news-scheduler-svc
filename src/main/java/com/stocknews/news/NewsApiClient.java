package com.stocknews.news;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * NewsAPI.org client.
 * Fetches general market news from NewsAPI using search queries.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsApiClient implements NewsSourceClient {
    private static final String NEWSAPI_BASE_URL = "https://newsapi.org/v2/everything";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final RestClient restClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Override
    public List<NewsItem> fetchNewsForSymbol(String symbol) {
        // NewsAPI doesn't support per-symbol queries as well, so fetch general market
        String market = symbol.contains("NS") || symbol.contains("BO") ? "INDIA" : "US";
        return fetchGeneralMarketNews(market);
    }

    @Override
    public List<NewsItem> fetchGeneralMarketNews(String market) {
        String apiKey = appProperties.getApis().getNewsapiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("NewsAPI key not configured, skipping market news fetch");
            return List.of();
        }

        try {
            String query = buildQueryForMarket(market);
            String url = String.format("%s?q=%s&sortBy=publishedAt&pageSize=10&apiKey=%s",
                    NEWSAPI_BASE_URL, query, apiKey);

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return parseNewsResponse(response, market);
        } catch (RestClientException e) {
            log.error("Failed to fetch NewsAPI articles for market {}: {}", market, e.getMessage());
            return List.of();
        }
    }

    private String buildQueryForMarket(String market) {
        if ("INDIA".equalsIgnoreCase(market)) {
            return "Nifty+50+stock+market+India";
        } else {
            return "NYSE+NASDAQ+stock+market";
        }
    }

    private List<NewsItem> parseNewsResponse(String jsonResponse, String market) {
        List<NewsItem> items = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode articlesArray = root.get("articles");
            if (articlesArray == null || !articlesArray.isArray()) {
                log.debug("No articles array found in NewsAPI response");
                return items;
            }

            int count = 0;
            for (JsonNode node : articlesArray) {
                if (count >= 10) break;
                String title = node.get("title") != null ? node.get("title").asText() : "";
                String url = node.get("url") != null ? node.get("url").asText() : "";
                String source = node.get("source") != null && node.get("source").get("name") != null
                        ? node.get("source").get("name").asText() : "NewsAPI";
                String publishedAtStr = node.get("publishedAt") != null ? node.get("publishedAt").asText() : "";

                LocalDateTime publishedAt = LocalDateTime.now();
                try {
                    if (!publishedAtStr.isBlank()) {
                        publishedAt = LocalDateTime.parse(publishedAtStr.replace("Z", ""), DateTimeFormatter.ISO_DATE_TIME);
                    }
                } catch (Exception e) {
                    log.debug("Could not parse publishedAt date: {}", publishedAtStr);
                }

                if (!title.isBlank() && !url.isBlank()) {
                    items.add(new NewsItem(title, url, source, publishedAt, null, market));
                    count++;
                }
            }
            log.debug("Parsed {} news items from NewsAPI for market {}", items.size(), market);
        } catch (Exception e) {
            log.error("Error parsing NewsAPI response: {}", e.getMessage());
        }
        return items;
    }
}

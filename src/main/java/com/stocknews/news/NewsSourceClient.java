package com.stocknews.news;

import java.util.List;

/**
 * Interface for news source clients.
 * Implementations fetch news from various APIs (Finnhub, NewsAPI, etc.).
 */
public interface NewsSourceClient {
    /**
     * Fetch news for a specific symbol.
     * @param symbol Market symbol (e.g., "AAPL", "RELIANCE.NS")
     * @return List of NewsItem records
     */
    List<NewsItem> fetchNewsForSymbol(String symbol);

    /**
     * Fetch general market news.
     * @param market Market identifier ("US" or "INDIA")
     * @return List of NewsItem records
     */
    List<NewsItem> fetchGeneralMarketNews(String market);
}

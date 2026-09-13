package com.stocknews.news;

import java.time.LocalDateTime;

/**
 * Immutable record representing a news article.
 * Fields: title, url, source, publishedAt, symbol, market.
 */
public record NewsItem(
        String title,
        String url,
        String source,
        LocalDateTime publishedAt,
        String symbol,
        String market
) {}

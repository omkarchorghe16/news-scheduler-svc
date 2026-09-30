package com.stocknews.dto;

import java.util.List;

/** created = newly inserted rows; skipped = tickers already present in that sector. */
public record BulkStockResponse(List<StockResponse> created, List<String> skipped) {
}

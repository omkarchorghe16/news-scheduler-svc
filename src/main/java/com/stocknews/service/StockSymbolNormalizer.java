package com.stocknews.service;

import java.util.List;
import java.util.Locale;

public final class StockSymbolNormalizer {
    private StockSymbolNormalizer() {
    }

    public static List<String> normalize(List<String> requestedSymbols) {
        return requestedSymbols.stream()
                .map(symbol -> symbol.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}

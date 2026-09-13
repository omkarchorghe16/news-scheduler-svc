package com.stocknews.digest;

import com.stocknews.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Watchlist component that manages US and NSE (India) stock symbols.
 * Bound from configuration: app.watchlist.us-symbols and app.watchlist.nse-symbols.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Watchlist {
    private final AppProperties appProperties;

    public List<String> getUsSymbols() {
        return appProperties.getWatchlist().getUsSymbols();
    }

    public List<String> getNseSymbols() {
        return appProperties.getWatchlist().getNseSymbols();
    }

    public List<String> getPortfolioSymbols() {
        return appProperties.getWatchlist().getPortfolioSymbols();
    }

    public void logWatchlist() {
        log.info("US Watchlist: {}", getUsSymbols());
        log.info("NSE Watchlist: {}", getNseSymbols());
        log.info("Portfolio Watchlist: {}", getPortfolioSymbols());
    }
}

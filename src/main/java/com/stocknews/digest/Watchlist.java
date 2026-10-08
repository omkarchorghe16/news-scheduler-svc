package com.stocknews.digest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Watchlist component that manages US and NSE (India) stock symbols.
 * Loads ticker symbols from the editable classpath stock-watchlist.txt resource.
 */
@Component
@Slf4j
public class Watchlist {
    private List<String> usSymbols;
    private List<String> indiaSymbols;
    private List<String> portfolioSymbols;

    @PostConstruct
    void loadSymbols() {
        List<String> us = new ArrayList<>();
        List<String> india = new ArrayList<>();
        List<String> portfolio = new ArrayList<>();
        List<String> currentSection = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("stock-watchlist.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String entry = line.trim();
                if (entry.isEmpty() || entry.startsWith("#")) {
                    continue;
                }
                if (entry.startsWith("[") && entry.endsWith("]")) {
                    currentSection = switch (entry.substring(1, entry.length() - 1).toUpperCase(Locale.ROOT)) {
                        case "US" -> us;
                        case "INDIA" -> india;
                        case "PORTFOLIO" -> portfolio;
                        default -> throw new IllegalArgumentException("Unknown watchlist section: " + entry);
                    };
                    continue;
                }
                if (currentSection == null) {
                    throw new IllegalArgumentException("Ticker found before a watchlist section: " + entry);
                }
                currentSection.add(entry.toUpperCase(Locale.ROOT));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load classpath resource stock-watchlist.txt", e);
        }

        usSymbols = List.copyOf(us);
        indiaSymbols = List.copyOf(india);
        portfolioSymbols = List.copyOf(portfolio);
        log.info("Loaded watchlist symbols: US={}, India={}, portfolio={}",
                usSymbols.size(), indiaSymbols.size(), portfolioSymbols.size());
    }

    public List<String> getUsSymbols() {
        return usSymbols;
    }

    public List<String> getNseSymbols() {
        return indiaSymbols;
    }

    public List<String> getPortfolioSymbols() {
        return portfolioSymbols;
    }

    public void logWatchlist() {
        log.info("US Watchlist: {}", getUsSymbols());
        log.info("NSE Watchlist: {}", getNseSymbols());
        log.info("Portfolio Watchlist: {}", getPortfolioSymbols());
    }
}

package com.stocknews.service;

import com.stocknews.model.YahooStockProfile;
import com.stocknews.repository.YahooStockProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class YahooStockProfileService {
    private final YahooStockProfileClient client;
    private final YahooStockProfileRepository repository;

    @Transactional
    public List<YahooStockProfile> fetchAndSave(List<String> requestedSymbols) {
        List<String> symbols = StockSymbolNormalizer.normalize(requestedSymbols);
        log.debug("Fetching Yahoo Finance profiles for {} unique symbols", symbols.size());
        List<YahooStockProfile> profiles = client.fetchProfiles(symbols);
        Map<String, YahooStockProfile> existing = new LinkedHashMap<>();
        repository.findAllBySymbolIn(symbols).forEach(profile -> existing.put(profile.getSymbol(), profile));
        profiles.forEach(profile -> {
            YahooStockProfile stored = existing.get(profile.getSymbol());
            if (stored != null) {
                profile.setId(stored.getId());
            }
        });
        List<YahooStockProfile> saved = repository.saveAll(profiles);
        log.info("Saved {} Yahoo Finance stock profiles to PostgreSQL", saved.size());
        return saved;
    }
}

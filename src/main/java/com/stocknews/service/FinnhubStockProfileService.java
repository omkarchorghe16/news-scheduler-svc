package com.stocknews.service;

import com.stocknews.model.FinnhubStockProfile;
import com.stocknews.repository.FinnhubStockProfileRepository;
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
public class FinnhubStockProfileService {
    private final FinnhubStockProfileClient client;
    private final FinnhubStockProfileRepository repository;

    @Transactional
    public List<FinnhubStockProfile> fetchAndSave(List<String> requestedSymbols) {
        List<String> symbols = StockSymbolNormalizer.normalize(requestedSymbols);
        log.debug("Fetching Finnhub profiles for {} unique symbols", symbols.size());
        List<FinnhubStockProfile> profiles = client.fetchProfiles(symbols);
        Map<String, FinnhubStockProfile> existing = new LinkedHashMap<>();
        repository.findAllBySymbolIn(symbols).forEach(profile -> existing.put(profile.getSymbol(), profile));
        profiles.forEach(profile -> {
            FinnhubStockProfile stored = existing.get(profile.getSymbol());
            if (stored != null) {
                profile.setId(stored.getId());
            }
        });
        List<FinnhubStockProfile> saved = repository.saveAll(profiles);
        log.info("Saved {} Finnhub stock profiles to PostgreSQL", saved.size());
        return saved;
    }
}

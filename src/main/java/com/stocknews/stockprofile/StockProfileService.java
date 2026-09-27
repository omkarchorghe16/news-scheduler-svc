package com.stocknews.stockprofile;

import com.stocknews.persistence.FinnhubStockProfile;
import com.stocknews.persistence.FinnhubStockProfileRepository;
import com.stocknews.persistence.YahooStockProfile;
import com.stocknews.persistence.YahooStockProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockProfileService {
    private final FinnhubStockProfileClient finnhubClient;
    private final YahooStockProfileClient yahooClient;
    private final FinnhubStockProfileRepository finnhubRepository;
    private final YahooStockProfileRepository yahooRepository;

    public List<FinnhubStockProfile> fetchAndSaveFinnhubProfiles(List<String> requestedSymbols) {
        List<String> symbols = normalizeSymbols(requestedSymbols);
        List<FinnhubStockProfile> profiles = finnhubClient.fetchProfiles(symbols);
        Map<String, FinnhubStockProfile> existing = new LinkedHashMap<>();
        finnhubRepository.findAllBySymbolIn(symbols).forEach(profile -> existing.put(profile.getSymbol(), profile));
        for (FinnhubStockProfile profile : profiles) {
            FinnhubStockProfile stored = existing.get(profile.getSymbol());
            if (stored != null) {
                profile.setId(stored.getId());
            }
        }
        return finnhubRepository.saveAll(profiles);
    }

    public List<YahooStockProfile> fetchAndSaveYahooProfiles(List<String> requestedSymbols) {
        List<String> symbols = normalizeSymbols(requestedSymbols);
        List<YahooStockProfile> profiles = yahooClient.fetchProfiles(symbols);
        Map<String, YahooStockProfile> existing = new LinkedHashMap<>();
        yahooRepository.findAllBySymbolIn(symbols).forEach(profile -> existing.put(profile.getSymbol(), profile));
        for (YahooStockProfile profile : profiles) {
            YahooStockProfile stored = existing.get(profile.getSymbol());
            if (stored != null) {
                profile.setId(stored.getId());
            }
        }
        return yahooRepository.saveAll(profiles);
    }

    private List<String> normalizeSymbols(List<String> requestedSymbols) {
        return requestedSymbols.stream()
                .map(symbol -> symbol.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}

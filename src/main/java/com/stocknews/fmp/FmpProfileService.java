package com.stocknews.fmp;

import com.stocknews.config.FmpProperties;
import com.stocknews.logging.ExceptionLog;
import com.stocknews.persistence.FmpApiCall;
import com.stocknews.persistence.FmpStockProfile;
import com.stocknews.persistence.postgres.FmpApiCallRepository;
import com.stocknews.persistence.postgres.FmpStockProfileRepository;
import com.stocknews.stockprofile.StockSymbolNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmpProfileService {
    private final FmpProfileClient client;
    private final FmpStockProfileRepository profileRepository;
    private final FmpApiCallRepository apiCallRepository;
    private final FmpProperties properties;

    public synchronized List<FmpStockProfile> fetchAndSave(List<String> requestedSymbols) {
        List<String> symbols = StockSymbolNormalizer.normalize(requestedSymbols);
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "FMP API key is not configured; set FMP_API_KEY");
        }
        if (properties.isUsExchangesOnly()
                && symbols.stream().anyMatch(symbol -> symbol.endsWith(".NS") || symbol.endsWith(".BO"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "FMP Basic profiles support US-exchange symbols only");
        }

        int dailyRequestLimit = properties.getDailyRequestLimit();
        if (dailyRequestLimit < 1) {
            throw new IllegalStateException("FMP daily request limit must be greater than zero");
        }

        Map<String, FmpStockProfile> existing = new LinkedHashMap<>();
        try {
            profileRepository.findAllBySymbolIn(symbols)
                    .forEach(profile -> existing.put(profile.getSymbol(), profile));
        } catch (DataAccessException exception) {
            log.error("Failed to read FMP profiles from PostgreSQL:\n{}",
                    ExceptionLog.stackTrace(exception));
            throw exception;
        }

        int cacheHours = properties.getProfileCacheHours();
        if (cacheHours < 0) {
            throw new IllegalStateException("FMP profile cache hours must not be negative");
        }
        LocalDateTime cacheCutoff = LocalDateTime.now().minusHours(cacheHours);
        List<String> symbolsToFetch = symbols.stream()
                .filter(symbol -> {
                    FmpStockProfile profile = existing.get(symbol);
                    return profile == null || profile.getUpdatedAt() == null
                            || !profile.getUpdatedAt().isAfter(cacheCutoff);
                })
                .toList();

        if (!symbolsToFetch.isEmpty()) {
            checkDailyQuota(symbolsToFetch.size(), dailyRequestLimit);
        }

        List<FmpStockProfile> fetched = symbolsToFetch.stream()
                .map(symbol -> {
                    FmpApiCall call = new FmpApiCall();
                    call.setCalledAt(LocalDateTime.now());
                    try {
                        apiCallRepository.saveAndFlush(call);
                    } catch (DataAccessException exception) {
                        log.error("Failed to record FMP API usage in PostgreSQL:\n{}",
                                ExceptionLog.stackTrace(exception));
                        throw exception;
                    }
                    return client.fetchProfile(symbol);
                })
                .toList();

        fetched.forEach(profile -> {
            FmpStockProfile stored = existing.get(profile.getSymbol());
            if (stored != null) {
                profile.setId(stored.getId());
            }
            existing.put(profile.getSymbol(), profile);
        });

        if (!fetched.isEmpty()) {
            try {
                profileRepository.saveAllAndFlush(fetched);
                log.info("Saved {} FMP stock profiles to PostgreSQL", fetched.size());
            } catch (DataAccessException exception) {
                log.error("Failed to persist FMP profiles to PostgreSQL:\n{}",
                        ExceptionLog.stackTrace(exception));
                throw exception;
            }
        }
        return symbols.stream().map(existing::get).toList();
    }

    private void checkDailyQuota(int requestedCalls, int dailyRequestLimit) {
        LocalDateTime now = LocalDateTime.now();
        long callsInLast24Hours;
        try {
            callsInLast24Hours = apiCallRepository.countByCalledAtAfter(now.minusHours(24));
        } catch (DataAccessException exception) {
            log.error("Failed to read FMP API usage from PostgreSQL:\n{}",
                    ExceptionLog.stackTrace(exception));
            throw exception;
        }
        if (callsInLast24Hours + requestedCalls > dailyRequestLimit) {
            ResponseStatusException exception = new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "FMP daily request limit reached; try again after the quota window resets");
            log.error("FMP daily request limit reached ({} of {} calls used):\n{}",
                    callsInLast24Hours, dailyRequestLimit, ExceptionLog.stackTrace(exception));
            throw exception;
        }
    }
}

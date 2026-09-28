package com.stocknews.stockprofile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import com.stocknews.logging.ExceptionLog;
import com.stocknews.persistence.FinnhubStockProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinnhubStockProfileClient {
    private final RestClient restClient;
    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final FinnhubRequestRateLimiter rateLimiter;

    public List<FinnhubStockProfile> fetchProfiles(List<String> symbols) {
        String apiKey = properties.getApis().getFinnhubKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Finnhub API key is not configured");
        }

        List<FinnhubStockProfile> profiles = new ArrayList<>();
        for (String symbol : symbols) {
            rateLimiter.awaitNextCall();
            try {
                String response = restClient.get()
                        .uri(UriComponentsBuilder.fromUriString(properties.getApis().getFinnhubBaseUrl())
                                .path("/stock/profile2")
                                .queryParam("symbol", symbol)
                                .queryParam("token", apiKey)
                                .build()
                                .encode()
                                .toUri())
                        .retrieve()
                        .body(String.class);
                profiles.add(parseProfile(symbol, response));
            } catch (RestClientException exception) {
                log.error("Finnhub profile request failed for symbol {}:\n{}",
                        symbol, ExceptionLog.stackTrace(exception));
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Finnhub profile lookup failed for " + symbol, exception);
            }
        }
        return profiles;
    }

    private FinnhubStockProfile parseProfile(String symbol, String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root == null || root.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Finnhub returned no profile for " + symbol);
            }

            FinnhubStockProfile profile = new FinnhubStockProfile();
            profile.setSymbol(symbol);
            profile.setName(text(root, "name"));
            profile.setCountry(text(root, "country"));
            profile.setCurrency(text(root, "currency"));
            profile.setExchange(text(root, "exchange"));
            profile.setIpoDate(text(root, "ipo"));
            profile.setMarketCapitalization(decimal(root, "marketCapitalization"));
            profile.setShareOutstanding(decimal(root, "shareOutstanding"));
            profile.setLogo(text(root, "logo"));
            profile.setPhone(text(root, "phone"));
            profile.setWebsite(text(root, "weburl"));
            profile.setIndustry(text(root, "finnhubIndustry"));
            profile.setRawPayload(response);
            return profile;
        } catch (ResponseStatusException exception) {
            log.error("Finnhub profile processing failed for symbol {}:\n{}",
                    symbol, ExceptionLog.stackTrace(exception));
            throw exception;
        } catch (Exception exception) {
            log.error("Could not parse Finnhub profile for symbol {}:\n{}",
                    symbol, ExceptionLog.stackTrace(exception));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not parse Finnhub profile for " + symbol, exception);
        }
    }

    private String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || !value.isNumber() ? null : value.decimalValue();
    }
}

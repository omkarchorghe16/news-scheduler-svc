package com.stocknews.alphavantage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import com.stocknews.logging.ExceptionLog;
import com.stocknews.persistence.AlphaVantageOverview;
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
public class AlphaVantageOverviewClient {
    private final RestClient restClient;
    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    public List<AlphaVantageOverview> fetchOverviews(List<String> symbols) {
        String apiKey = properties.getApis().getAlphaVantageKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Alpha Vantage API key is not configured");
        }

        List<AlphaVantageOverview> overviews = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                // Temporarily disabled: AlphaVantageRateLimiter.reserveRequest().
                String response = restClient.get()
                        .uri(UriComponentsBuilder.fromUriString(properties.getApis().getAlphaVantageBaseUrl())
                                .queryParam("function", "OVERVIEW")
                                .queryParam("symbol", symbol)
                                .queryParam("apikey", apiKey)
                                .build()
                                .encode()
                                .toUri())
                        .retrieve()
                        .body(String.class);
                overviews.add(parseOverview(symbol, response));
            } catch (RestClientException exception) {
                log.error("Alpha Vantage overview request failed for {}:\n{}",
                        symbol, ExceptionLog.stackTrace(exception));
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Alpha Vantage overview lookup failed for " + symbol, exception);
            }
        }
        return overviews;
    }

    private AlphaVantageOverview parseOverview(String symbol, String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.has("Note") || root.has("Information")) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Alpha Vantage rate limit reached; wait before retrying");
            }
            if (root.has("Error Message")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Alpha Vantage did not recognize symbol " + symbol);
            }
            if (root.isEmpty() || !root.hasNonNull("Symbol")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Alpha Vantage returned no overview for " + symbol);
            }

            AlphaVantageOverview overview = new AlphaVantageOverview();
            overview.setSymbol(symbol);
            overview.setName(text(root, "Name"));
            overview.setExchange(text(root, "Exchange"));
            overview.setCurrency(text(root, "Currency"));
            overview.setCountry(text(root, "Country"));
            overview.setSector(text(root, "Sector"));
            overview.setIndustry(text(root, "Industry"));
            overview.setMarketCapitalization(decimal(root, "MarketCapitalization"));
            overview.setEbitda(decimal(root, "EBITDA"));
            overview.setPeRatio(decimal(root, "PERatio"));
            overview.setPegRatio(decimal(root, "PEGRatio"));
            overview.setBookValue(decimal(root, "BookValue"));
            overview.setDividendPerShare(decimal(root, "DividendPerShare"));
            overview.setDividendYield(decimal(root, "DividendYield"));
            overview.setEps(decimal(root, "EPS"));
            overview.setProfitMargin(decimal(root, "ProfitMargin"));
            overview.setOperatingMarginTtm(decimal(root, "OperatingMarginTTM"));
            overview.setReturnOnAssetsTtm(decimal(root, "ReturnOnAssetsTTM"));
            overview.setReturnOnEquityTtm(decimal(root, "ReturnOnEquityTTM"));
            overview.setRevenueTtm(decimal(root, "RevenueTTM"));
            overview.setGrossProfitTtm(decimal(root, "GrossProfitTTM"));
            overview.setDescription(text(root, "Description"));
            overview.setRawPayload(response);
            return overview;
        } catch (ResponseStatusException exception) {
            log.error("Alpha Vantage overview processing failed for {}:\n{}",
                    symbol, ExceptionLog.stackTrace(exception));
            throw exception;
        } catch (Exception exception) {
            log.error("Could not parse Alpha Vantage overview for {}:\n{}",
                    symbol, ExceptionLog.stackTrace(exception));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not parse Alpha Vantage overview for " + symbol, exception);
        }
    }

    private String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode root, String field) {
        String value = text(root, field);
        if (value == null || value.equalsIgnoreCase("none") || value.equals("-")) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            log.error("Could not parse Alpha Vantage numeric field {}:\n{}",
                    field, ExceptionLog.stackTrace(exception));
            return null;
        }
    }
}

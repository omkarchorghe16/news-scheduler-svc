package com.stocknews.stockprofile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import com.stocknews.persistence.YahooStockProfile;
import lombok.RequiredArgsConstructor;
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
public class YahooStockProfileClient {
    private final RestClient restClient;
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public List<YahooStockProfile> fetchProfiles(List<String> symbols) {
        List<YahooStockProfile> profiles = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                String response = restClient.get()
                        .uri(UriComponentsBuilder.fromUriString(properties.getApis().getYahooBaseUrl())
                                .pathSegment("v10", "finance", "quoteSummary", symbol)
                                .queryParam("modules", "assetProfile,summaryProfile,summaryDetail,price")
                                .build()
                                .encode()
                                .toUri())
                        .header("User-Agent", "Mozilla/5.0")
                        .retrieve()
                        .body(String.class);
                profiles.add(parseProfile(symbol, response));
            } catch (RestClientException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Yahoo Finance profile lookup failed for " + symbol, exception);
            }
        }
        return profiles;
    }

    private YahooStockProfile parseProfile(String symbol, String response) {
        try {
            JsonNode result = objectMapper.readTree(response)
                    .path("quoteSummary")
                    .path("result")
                    .path(0);
            if (result.isMissingNode() || result.isNull()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Yahoo Finance returned no profile for " + symbol);
            }

            JsonNode asset = result.path("assetProfile");
            JsonNode summary = result.path("summaryProfile");
            JsonNode price = result.path("price");
            JsonNode summaryDetail = result.path("summaryDetail");
            YahooStockProfile profile = new YahooStockProfile();
            profile.setSymbol(symbol);
            profile.setName(text(price, "longName", text(price, "shortName", null)));
            profile.setCountry(text(asset, "country", text(summary, "country", null)));
            profile.setCity(text(asset, "city", text(summary, "city", null)));
            profile.setState(text(asset, "state", text(summary, "state", null)));
            profile.setAddress(text(asset, "address1", text(summary, "address1", null)));
            profile.setPostalCode(text(asset, "zip", text(summary, "zip", null)));
            profile.setPhone(text(asset, "phone", text(summary, "phone", null)));
            profile.setWebsite(text(asset, "website", text(summary, "website", null)));
            profile.setSector(text(asset, "sector", text(summary, "sector", null)));
            profile.setIndustry(text(asset, "industry", text(summary, "industry", null)));
            profile.setFullTimeEmployees(longValue(asset, "fullTimeEmployees"));
            profile.setCurrency(text(price, "currency", null));
            profile.setMarketCapitalization(decimal(price.path("marketCap"), decimal(summaryDetail.path("marketCap"), null)));
            profile.setBusinessSummary(text(asset, "longBusinessSummary", text(summary, "longBusinessSummary", null)));
            profile.setRawPayload(response);
            return profile;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not parse Yahoo Finance profile for " + symbol, exception);
        }
    }

    private String text(JsonNode root, String field, String fallback) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? fallback : value.asText();
    }

    private Long longValue(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        JsonNode raw = value.isObject() ? value.get("raw") : value;
        return raw == null || !raw.isNumber() ? null : raw.longValue();
    }

    private BigDecimal decimal(JsonNode value, BigDecimal fallback) {
        if (value == null || value.isNull()) {
            return fallback;
        }
        JsonNode raw = value.isObject() ? value.get("raw") : value;
        return raw == null || !raw.isNumber() ? fallback : raw.decimalValue();
    }
}

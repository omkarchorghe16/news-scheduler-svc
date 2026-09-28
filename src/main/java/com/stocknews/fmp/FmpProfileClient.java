package com.stocknews.fmp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.FmpProperties;
import com.stocknews.logging.ExceptionLog;
import com.stocknews.persistence.FmpStockProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class FmpProfileClient {
    private final RestClient restClient;
    private final FmpProperties properties;
    private final ObjectMapper objectMapper;

    public FmpStockProfile fetchProfile(String symbol) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "FMP API key is not configured; set FMP_API_KEY");
        }

        try {
            String response = restClient.get()
                    .uri(UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                            .pathSegment("profile")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build()
                            .encode()
                            .toUri())
                    .retrieve()
                    .body(String.class);
            return parseProfile(symbol, response);
        } catch (RestClientResponseException exception) {
            log.error("FMP profile request failed for symbol {} with HTTP status {}:\n{}",
                    symbol, exception.getStatusCode().value(), ExceptionLog.stackTrace(exception));
            HttpStatus status = exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : HttpStatus.BAD_GATEWAY;
            throw new ResponseStatusException(status, "FMP profile lookup failed for " + symbol, exception);
        } catch (ResponseStatusException exception) {
            log.error("FMP profile response was invalid for symbol {}:\n{}",
                    symbol, ExceptionLog.stackTrace(exception));
            throw exception;
        } catch (Exception exception) {
            log.error("FMP profile request failed for symbol {}:\n{}",
                    symbol, ExceptionLog.stackTrace(exception));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "FMP profile lookup failed for " + symbol, exception);
        }
    }

    private FmpStockProfile parseProfile(String symbol, String response) {
        try {
            JsonNode profiles = objectMapper.readTree(response);
            if (!profiles.isArray() || profiles.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "FMP returned no profile for " + symbol);
            }

            JsonNode item = profiles.get(0);
            FmpStockProfile profile = new FmpStockProfile();
            profile.setSymbol(symbol);
            profile.setCompanyName(text(item, "companyName"));
            profile.setCurrency(text(item, "currency"));
            profile.setCik(text(item, "cik"));
            profile.setIsin(text(item, "isin"));
            profile.setCusip(text(item, "cusip"));
            profile.setExchange(text(item, "exchange"));
            profile.setExchangeShortName(text(item, "exchangeShortName"));
            profile.setIndustry(text(item, "industry"));
            profile.setWebsite(text(item, "website"));
            profile.setMarketCap(decimal(item, "marketCap"));
            profile.setPrice(decimal(item, "price"));
            profile.setBeta(decimal(item, "beta"));
            profile.setLastDividend(decimal(item, "lastDividend"));
            profile.setPriceRange(text(item, "range"));
            profile.setChange(decimal(item, "change"));
            profile.setChangePercentage(decimal(item, "changePercentage"));
            profile.setVolume(longValue(item, "volume"));
            profile.setAverageVolume(longValue(item, "volAvg"));
            profile.setCeo(text(item, "ceo"));
            profile.setSector(text(item, "sector"));
            profile.setCountry(text(item, "country"));
            profile.setFullTimeEmployees(longValue(item, "fullTimeEmployees"));
            profile.setPhone(text(item, "phone"));
            profile.setAddress(text(item, "address"));
            profile.setCity(text(item, "city"));
            profile.setState(text(item, "state"));
            profile.setZip(text(item, "zip"));
            profile.setIpoDate(text(item, "ipoDate"));
            profile.setActivelyTrading(booleanValue(item, "isActivelyTrading"));
            profile.setEtf(booleanValue(item, "isEtf"));
            profile.setFund(booleanValue(item, "isFund"));
            profile.setAdr(booleanValue(item, "isAdr"));
            profile.setDelisted(booleanValue(item, "isDelisted"));
            profile.setDescription(text(item, "description"));
            profile.setRawPayload(objectMapper.writeValueAsString(item));
            return profile;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not parse FMP profile for " + symbol, exception);
        }
    }

    private String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isNumber() ? value.decimalValue() : new BigDecimal(value.asText());
    }

    private Long longValue(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isNumber() ? value.longValue() : Long.parseLong(value.asText());
    }

    private Boolean booleanValue(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() || !value.isBoolean() ? null : value.booleanValue();
    }
}

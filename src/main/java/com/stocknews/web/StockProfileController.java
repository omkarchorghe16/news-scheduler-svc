package com.stocknews.web;

import com.stocknews.persistence.FinnhubStockProfile;
import com.stocknews.persistence.AlphaVantageOverview;
import com.stocknews.persistence.YahooStockProfile;
import com.stocknews.alphavantage.AlphaVantageOverviewService;
import com.stocknews.stockprofile.StockProfileService;
import com.stocknews.stockprofile.StockSymbolsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stock Profiles", description = "Fetch and store provider company profiles")
public class StockProfileController {
    private final StockProfileService stockProfileService;
    private final AlphaVantageOverviewService alphaVantageOverviewService;

    @PostMapping("/finnhub/profiles")
    @Operation(summary = "Fetch Finnhub company profiles", description = "Fetches company profiles and stores them in PostgreSQL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profiles fetched and stored"),
            @ApiResponse(responseCode = "400", description = "Invalid symbol list"),
            @ApiResponse(responseCode = "503", description = "Finnhub API key is not configured"),
            @ApiResponse(responseCode = "502", description = "Finnhub request failed")
    })
    public List<FinnhubStockProfile> fetchFinnhubProfiles(@Valid @RequestBody StockSymbolsRequest request) {
        log.info("Received Finnhub stock profile request for {} symbols", request.symbols().size());
        List<FinnhubStockProfile> profiles = stockProfileService.fetchAndSaveFinnhubProfiles(request.symbols());
        log.info("Returned {} Finnhub stock profiles", profiles.size());
        return profiles;
    }

    @PostMapping("/yahoo/profiles")
    @Operation(summary = "Fetch Yahoo Finance company profiles", description = "Fetches business summaries and profile details and stores them in PostgreSQL.")
    public List<YahooStockProfile> fetchYahooProfiles(@Valid @RequestBody StockSymbolsRequest request) {
        log.info("Received Yahoo Finance stock profile request for {} symbols", request.symbols().size());
        List<YahooStockProfile> profiles = stockProfileService.fetchAndSaveYahooProfiles(request.symbols());
        log.info("Returned {} Yahoo Finance stock profiles", profiles.size());
        return profiles;
    }

    @PostMapping("/alphavantage/overview")
    @Operation(summary = "Fetch Alpha Vantage company overviews",
            description = "Calls the Alpha Vantage OVERVIEW function for each symbol and stores the results in PostgreSQL. Provider limits apply: 25 calls per day and 5 calls per minute.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overviews fetched and stored"),
            @ApiResponse(responseCode = "400", description = "Invalid symbol list"),
            @ApiResponse(responseCode = "429", description = "Provider request quota exceeded"),
            @ApiResponse(responseCode = "503", description = "Alpha Vantage API key is not configured"),
            @ApiResponse(responseCode = "502", description = "Alpha Vantage request failed")
    })
    public List<AlphaVantageOverview> fetchAlphaVantageOverviews(@Valid @RequestBody StockSymbolsRequest request) {
        log.info("Received Alpha Vantage overview request for {} symbols", request.symbols().size());
        List<AlphaVantageOverview> overviews = alphaVantageOverviewService.fetchAndSave(request.symbols());
        log.info("Returned {} Alpha Vantage overviews", overviews.size());
        return overviews;
    }
}

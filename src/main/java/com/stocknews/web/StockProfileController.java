package com.stocknews.web;

import com.stocknews.persistence.FinnhubStockProfile;
import com.stocknews.persistence.YahooStockProfile;
import com.stocknews.stockprofile.StockProfileService;
import com.stocknews.stockprofile.StockSymbolsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StockProfileController {
    private final StockProfileService stockProfileService;

    @PostMapping("/finnhub/profiles")
    public List<FinnhubStockProfile> fetchFinnhubProfiles(@Valid @RequestBody StockSymbolsRequest request) {
        log.info("Received Finnhub stock profile request for {} symbols", request.symbols().size());
        List<FinnhubStockProfile> profiles = stockProfileService.fetchAndSaveFinnhubProfiles(request.symbols());
        log.info("Returned {} Finnhub stock profiles", profiles.size());
        return profiles;
    }

    @PostMapping("/yahoo/profiles")
    public List<YahooStockProfile> fetchYahooProfiles(@Valid @RequestBody StockSymbolsRequest request) {
        log.info("Received Yahoo Finance stock profile request for {} symbols", request.symbols().size());
        List<YahooStockProfile> profiles = stockProfileService.fetchAndSaveYahooProfiles(request.symbols());
        log.info("Returned {} Yahoo Finance stock profiles", profiles.size());
        return profiles;
    }
}

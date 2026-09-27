package com.stocknews.web;

import com.stocknews.persistence.FinnhubStockProfile;
import com.stocknews.persistence.YahooStockProfile;
import com.stocknews.stockprofile.StockProfileService;
import com.stocknews.stockprofile.StockSymbolsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockProfileController {
    private final StockProfileService stockProfileService;

    @PostMapping("/finnhub/profiles")
    public List<FinnhubStockProfile> fetchFinnhubProfiles(@Valid @RequestBody StockSymbolsRequest request) {
        return stockProfileService.fetchAndSaveFinnhubProfiles(request.symbols());
    }

    @PostMapping("/yahoo/profiles")
    public List<YahooStockProfile> fetchYahooProfiles(@Valid @RequestBody StockSymbolsRequest request) {
        return stockProfileService.fetchAndSaveYahooProfiles(request.symbols());
    }
}

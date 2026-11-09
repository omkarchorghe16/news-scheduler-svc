package com.stocknews.controller;

import com.stocknews.model.FmpStockProfile;
import com.stocknews.service.FmpProfileService;
import com.stocknews.service.StockSymbolsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fmp")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FMP Profiles", description = "Fetch and store Financial Modeling Prep company profiles")
public class FmpProfileController {

    private final FmpProfileService fmpProfileService;

    @GetMapping("/profiles")
    @Operation(summary = "Get saved FMP stock profiles",
            description = "Returns full matching profiles already saved in PostgreSQL. Unknown symbols are omitted; no FMP provider calls are made.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saved FMP stock profiles"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid symbol list")
    })
    public List<FmpStockProfile> getSavedProfiles(
            @Parameter(description = "Ticker symbols, repeated or comma-separated", example = "AAPL,MSFT")
            @RequestParam("symbols")
            @NotEmpty List<@NotBlank @Pattern(regexp = "(?i)[A-Z0-9][A-Z0-9.-]{0,19}") String> symbols) {
        List<FmpStockProfile> profiles = fmpProfileService.findSavedProfiles(symbols);
        logReturnedProfiles("GET", profiles);
        return profiles;
    }

    @PostMapping("/profiles")
    @Operation(summary = "Fetch Financial Modeling Prep company profiles",
            description = "Fetches US stock company profiles using FMP, upserts records by symbol, then reads and returns the saved profiles from PostgreSQL. Recently fetched profiles are reused for the configured cache duration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profiles fetched and stored"),
            @ApiResponse(responseCode = "400", description = "Invalid symbol list or non-US symbol on the Basic plan"),
            @ApiResponse(responseCode = "404", description = "FMP returned no profile for a symbol"),
            @ApiResponse(responseCode = "429", description = "FMP daily request quota exceeded"),
            @ApiResponse(responseCode = "503", description = "FMP API key is not configured"),
            @ApiResponse(responseCode = "502", description = "FMP request failed")
    })
    public List<FmpStockProfile> fetchProfiles(@Valid @RequestBody StockSymbolsRequest request) {
        log.info("Received FMP stock profile request for {} symbols", request.symbols().size());
        fmpProfileService.fetchAndSave(request.symbols());
        List<FmpStockProfile> profiles = fmpProfileService.findSavedProfiles(request.symbols());
        logReturnedProfiles("POST", profiles);
        return profiles;
    }

    private void logReturnedProfiles(String method, List<FmpStockProfile> profiles) {
        log.info("{} /api/fmp/profiles returning {} persisted profiles", method, profiles.size());
        profiles.forEach(profile -> log.info(
                "{} /api/fmp/profiles response profile: id={}, symbol={}, companyName={}, country={}, "
                        + "currency={}, exchange={}, industry={}, price={}, marketCap={}, sector={}, updatedAt={}",
                method, profile.getId(), profile.getSymbol(), profile.getCompanyName(), profile.getCountry(),
                profile.getCurrency(), profile.getExchange(), profile.getIndustry(), profile.getPrice(),
                profile.getMarketCap(), profile.getSector(), profile.getUpdatedAt()));
    }
}

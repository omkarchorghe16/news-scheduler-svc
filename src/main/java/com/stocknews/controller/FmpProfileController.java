package com.stocknews.controller;

import com.stocknews.model.FmpStockProfile;
import com.stocknews.service.FmpProfileService;
import com.stocknews.service.StockSymbolsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fmp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FMP Profiles", description = "Fetch and store Financial Modeling Prep company profiles")
public class FmpProfileController {

    private final FmpProfileService fmpProfileService;

    @PostMapping("/profiles")
    @Operation(summary = "Fetch Financial Modeling Prep company profiles",
            description = "Fetches US stock company profiles using FMP and stores all returned profile fields and the raw provider record in PostgreSQL.")
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
        List<FmpStockProfile> profiles = fmpProfileService.fetchAndSave(request.symbols());
        log.info("Returned {} FMP stock profiles", profiles.size());
        return profiles;
    }
}

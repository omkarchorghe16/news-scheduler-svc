package com.stocknews.controller;

import com.stocknews.dto.BulkStockRequest;
import com.stocknews.dto.BulkStockResponse;
import com.stocknews.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-symbols")
@RequiredArgsConstructor
@Tag(name = "Stock Symbols", description = "Add stock symbols to a sector")
public class StockSymbolController {

    private final StockService stockService;

    @PostMapping
    @Operation(summary = "Add multiple stock symbols to a sector",
            description = "Creates stock records for the supplied ticker symbols. Existing tickers in the sector are returned as skipped.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock symbols processed"),
            @ApiResponse(responseCode = "400", description = "Invalid sector ID or ticker list"),
            @ApiResponse(responseCode = "404", description = "Sector not found")
    })
    public ResponseEntity<BulkStockResponse> create(@Valid @RequestBody BulkStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.createBulk(request));
    }
}

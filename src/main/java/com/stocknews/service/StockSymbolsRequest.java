package com.stocknews.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ticker symbols to fetch from the selected provider")
public record StockSymbolsRequest(
        @Schema(description = "One or more symbols, for example AAPL or RELIANCE.NS")
        @NotEmpty List<@NotBlank @Pattern(regexp = "(?i)[A-Z0-9][A-Z0-9.-]{0,19}") String> symbols
) {
}

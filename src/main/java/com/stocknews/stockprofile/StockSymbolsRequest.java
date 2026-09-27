package com.stocknews.stockprofile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record StockSymbolsRequest(
        @NotEmpty List<@NotBlank @Pattern(regexp = "(?i)[A-Z0-9][A-Z0-9.-]{0,19}") String> symbols
) {
}

package com.stocknews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkStockRequest(
        @NotNull(message = "sectorId is required")
        Integer sectorId,

        @NotEmpty(message = "tickers must contain at least one value")
        List<@NotBlank @Size(max = 20) String> tickers) {
}

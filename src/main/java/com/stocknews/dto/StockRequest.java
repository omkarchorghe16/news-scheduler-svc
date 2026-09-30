package com.stocknews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockRequest(
        @NotBlank(message = "ticker is required")
        @Size(max = 20, message = "ticker must be at most 20 characters")
        String ticker,

        @NotNull(message = "sectorId is required")
        Integer sectorId) {
}

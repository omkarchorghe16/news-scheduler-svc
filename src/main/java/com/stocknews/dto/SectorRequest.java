package com.stocknews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectorRequest(
        @NotBlank(message = "sectorName is required")
        @Size(max = 100, message = "sectorName must be at most 100 characters")
        String sectorName) {
}

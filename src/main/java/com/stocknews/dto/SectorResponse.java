package com.stocknews.dto;

import com.stocknews.model.Sector;

import java.time.LocalDateTime;

public record SectorResponse(Integer id, String sectorName, LocalDateTime createdAt) {

    public static SectorResponse from(Sector sector) {
        return new SectorResponse(sector.getId(), sector.getSectorName(), sector.getCreatedAt());
    }
}

package com.stocknews.dto;

import com.stocknews.model.Stock;

import java.time.LocalDateTime;

public record StockResponse(
        Integer id,
        String ticker,
        Integer sectorId,
        String sectorName,
        LocalDateTime createdAt) {

    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getTicker(),
                stock.getSector().getId(),
                stock.getSector().getSectorName(),
                stock.getCreatedAt());
    }
}

package com.stocknews.service;

import com.stocknews.dto.BulkStockRequest;
import com.stocknews.dto.BulkStockResponse;
import com.stocknews.dto.StockRequest;
import com.stocknews.dto.StockResponse;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.Sector;
import com.stocknews.model.Stock;
import com.stocknews.repository.SectorRepository;
import com.stocknews.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final SectorRepository sectorRepository;

    public StockService(StockRepository stockRepository, SectorRepository sectorRepository) {
        this.stockRepository = stockRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional
    public StockResponse create(StockRequest request) {
        Sector sector = getSectorOrThrow(request.sectorId());
        String ticker = normalize(request.ticker());
        if (stockRepository.existsByTickerIgnoreCaseAndSectorId(ticker, sector.getId())) {
            throw new DuplicateResourceException(
                    ticker + " already exists in sector " + sector.getSectorName());
        }
        Stock stock = new Stock();
        stock.setTicker(ticker);
        stock.setSector(sector);
        return StockResponse.from(stockRepository.save(stock));
    }

    /** Adds many tickers to one sector; tickers already in that sector are skipped. */
    @Transactional
    public BulkStockResponse createBulk(BulkStockRequest request) {
        Sector sector = getSectorOrThrow(request.sectorId());
        Set<String> tickers = new LinkedHashSet<>();
        request.tickers().forEach(t -> tickers.add(normalize(t)));

        List<StockResponse> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (String ticker : tickers) {
            if (stockRepository.existsByTickerIgnoreCaseAndSectorId(ticker, sector.getId())) {
                skipped.add(ticker);
                continue;
            }
            Stock stock = new Stock();
            stock.setTicker(ticker);
            stock.setSector(sector);
            created.add(StockResponse.from(stockRepository.save(stock)));
        }
        return new BulkStockResponse(created, skipped);
    }

    @Transactional(readOnly = true)
    public List<StockResponse> find(Integer sectorId, String ticker) {
        List<Stock> stocks;
        if (sectorId != null && ticker != null && !ticker.isBlank()) {
            stocks = stockRepository.findByTickerIgnoreCaseAndSectorId(normalize(ticker), sectorId);
        } else if (sectorId != null) {
            stocks = stockRepository.findBySectorId(sectorId);
        } else if (ticker != null && !ticker.isBlank()) {
            stocks = stockRepository.findByTickerIgnoreCase(normalize(ticker));
        } else {
            stocks = stockRepository.findAll();
        }
        return stocks.stream().map(StockResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StockResponse findById(Integer id) {
        return StockResponse.from(getOrThrow(id));
    }

    @Transactional
    public StockResponse update(Integer id, StockRequest request) {
        Stock stock = getOrThrow(id);
        Sector sector = getSectorOrThrow(request.sectorId());
        String ticker = normalize(request.ticker());

        boolean changed = !stock.getTicker().equalsIgnoreCase(ticker)
                || !stock.getSector().getId().equals(sector.getId());
        if (changed && stockRepository.existsByTickerIgnoreCaseAndSectorId(ticker, sector.getId())) {
            throw new DuplicateResourceException(
                    ticker + " already exists in sector " + sector.getSectorName());
        }
        stock.setTicker(ticker);
        stock.setSector(sector);
        return StockResponse.from(stockRepository.save(stock));
    }

    @Transactional
    public void delete(Integer id) {
        stockRepository.delete(getOrThrow(id));
    }

    private Stock getOrThrow(Integer id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + id));
    }

    private Sector getSectorOrThrow(Integer sectorId) {
        return sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found: " + sectorId));
    }

    private String normalize(String ticker) {
        return ticker.trim().toUpperCase(Locale.ROOT);
    }
}

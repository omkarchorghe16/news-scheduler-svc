package com.stocknews.controller;

import com.stocknews.dto.BulkStockRequest;
import com.stocknews.dto.BulkStockResponse;
import com.stocknews.dto.StockRequest;
import com.stocknews.dto.StockResponse;
import com.stocknews.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "Manage stocks within sectors")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    @Operation(summary = "Create a stock")
    public ResponseEntity<StockResponse> create(@Valid @RequestBody StockRequest request) {
        StockResponse created = stockService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create stocks in bulk")
    public ResponseEntity<BulkStockResponse> createBulk(@Valid @RequestBody BulkStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.createBulk(request));
    }

    /** Optional filters: /api/stocks?sectorId=3&ticker=NVDA */
    @GetMapping
    @Operation(summary = "List stocks, optionally filtered by sector or ticker")
    public List<StockResponse> find(@RequestParam(required = false) Integer sectorId,
                                    @RequestParam(required = false) String ticker) {
        return stockService.find(sectorId, ticker);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a stock by ID")
    public StockResponse findById(@PathVariable Integer id) {
        return stockService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a stock")
    public StockResponse update(@PathVariable Integer id, @Valid @RequestBody StockRequest request) {
        return stockService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stock")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        stockService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

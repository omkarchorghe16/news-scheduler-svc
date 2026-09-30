package com.stocknews.controller;

import com.stocknews.dto.BulkStockRequest;
import com.stocknews.dto.BulkStockResponse;
import com.stocknews.dto.StockResponse;
import com.stocknews.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockSymbolController.class)
class StockSymbolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    void createStoresSuppliedTickersForSector() throws Exception {
        when(stockService.createBulk(new BulkStockRequest(3, List.of("AAPL", "MSFT"))))
                .thenReturn(new BulkStockResponse(
                        List.of(stock(11, "AAPL"), stock(12, "MSFT")), List.of()));

        mockMvc.perform(post("/api/stock-symbols")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectorId\":3,\"tickers\":[\"AAPL\",\"MSFT\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.created.length()").value(2))
                .andExpect(jsonPath("$.created[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$.skipped.length()").value(0));

        verify(stockService).createBulk(new BulkStockRequest(3, List.of("AAPL", "MSFT")));
    }

    @Test
    void createRejectsEmptyTickerList() throws Exception {
        mockMvc.perform(post("/api/stock-symbols")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectorId\":3,\"tickers\":[]}"))
                .andExpect(status().isBadRequest());
    }

    private StockResponse stock(Integer id, String ticker) {
        return new StockResponse(id, ticker, 3, "Technology", LocalDateTime.now());
    }
}

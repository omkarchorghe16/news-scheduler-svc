package com.stocknews.controller;

import com.stocknews.dto.BulkStockRequest;
import com.stocknews.dto.BulkStockResponse;
import com.stocknews.dto.StockRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    void createReturnsCreatedResourceAndLocation() throws Exception {
        when(stockService.create(new StockRequest("AAPL", 3)))
                .thenReturn(stock(11, "AAPL"));

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"AAPL\",\"sectorId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/stocks/11"))
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.sectorId").value(3));
    }

    @Test
    void createBulkReturnsCreatedStocksAndSkippedTickers() throws Exception {
        when(stockService.createBulk(new BulkStockRequest(3, List.of("AAPL", "MSFT"))))
                .thenReturn(new BulkStockResponse(
                        List.of(stock(11, "AAPL"), stock(12, "MSFT")), List.of()));

        mockMvc.perform(post("/api/stocks/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectorId\":3,\"tickers\":[\"AAPL\",\"MSFT\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.created.length()").value(2))
                .andExpect(jsonPath("$.skipped.length()").value(0));
    }

    @Test
    void findPassesOptionalFiltersToService() throws Exception {
        when(stockService.find(3, "AAPL")).thenReturn(List.of(stock(11, "AAPL")));

        mockMvc.perform(get("/api/stocks")
                        .param("sectorId", "3")
                        .param("ticker", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"));

        verify(stockService).find(3, "AAPL");
    }

    @Test
    void findByIdDelegatesToService() throws Exception {
        when(stockService.findById(11)).thenReturn(stock(11, "AAPL"));

        mockMvc.perform(get("/api/stocks/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));

        verify(stockService).findById(11);
    }

    @Test
    void updateReturnsUpdatedStock() throws Exception {
        when(stockService.update(11, new StockRequest("MSFT", 3)))
                .thenReturn(stock(11, "MSFT"));

        mockMvc.perform(put("/api/stocks/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"MSFT\",\"sectorId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("MSFT"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/stocks/11"))
                .andExpect(status().isNoContent());

        verify(stockService).delete(11);
    }

    @Test
    void createRejectsMissingTickerAndSector() throws Exception {
        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\" \",\"sectorId\":null}"))
                .andExpect(status().isBadRequest());
    }

    private StockResponse stock(Integer id, String ticker) {
        return new StockResponse(id, ticker, 3, "Technology", LocalDateTime.now());
    }
}

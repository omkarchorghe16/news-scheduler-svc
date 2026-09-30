package com.stocknews.controller;

import com.stocknews.alphavantage.AlphaVantageOverviewService;
import com.stocknews.model.AlphaVantageOverview;
import com.stocknews.model.FinnhubStockProfile;
import com.stocknews.model.YahooStockProfile;
import com.stocknews.service.FinnhubStockProfileService;
import com.stocknews.service.YahooStockProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockProfileController.class)
class StockProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinnhubStockProfileService finnhubStockProfileService;

    @MockitoBean
    private YahooStockProfileService yahooStockProfileService;

    @MockitoBean
    private AlphaVantageOverviewService alphaVantageOverviewService;

    @Test
    void fetchFinnhubProfilesAcceptsSymbolList() throws Exception {
        when(finnhubStockProfileService.fetchAndSave(List.of("AAPL")))
                .thenReturn(List.of(new FinnhubStockProfile()));

        mockMvc.perform(post("/api/stocks/finnhub/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[\"AAPL\"]}"))
                .andExpect(status().isOk());

        verify(finnhubStockProfileService).fetchAndSave(List.of("AAPL"));
    }

    @Test
    void fetchYahooProfilesAcceptsSymbolList() throws Exception {
        when(yahooStockProfileService.fetchAndSave(List.of("AAPL", "MSFT")))
                .thenReturn(List.of(new YahooStockProfile(), new YahooStockProfile()));

        mockMvc.perform(post("/api/stocks/yahoo/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[\"AAPL\",\"MSFT\"]}"))
                .andExpect(status().isOk());

        verify(yahooStockProfileService).fetchAndSave(List.of("AAPL", "MSFT"));
    }

    @Test
    void fetchAlphaVantageOverviewsAcceptsSymbolList() throws Exception {
        when(alphaVantageOverviewService.fetchAndSave(List.of("AAPL")))
                .thenReturn(List.of(new AlphaVantageOverview()));

        mockMvc.perform(post("/api/stocks/alphavantage/overview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[\"AAPL\"]}"))
                .andExpect(status().isOk());

        verify(alphaVantageOverviewService).fetchAndSave(List.of("AAPL"));
    }

    @Test
    void rejectsEmptySymbolList() throws Exception {
        mockMvc.perform(post("/api/stocks/finnhub/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[]}"))
                .andExpect(status().isBadRequest());
    }
}

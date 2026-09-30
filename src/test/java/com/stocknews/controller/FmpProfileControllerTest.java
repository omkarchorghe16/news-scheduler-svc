package com.stocknews.controller;

import com.stocknews.model.FmpStockProfile;
import com.stocknews.service.FmpProfileService;
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

@WebMvcTest(FmpProfileController.class)
class FmpProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FmpProfileService fmpProfileService;

    @Test
    void fetchProfilesAcceptsSymbolList() throws Exception {
        when(fmpProfileService.fetchAndSave(List.of("AAPL")))
                .thenReturn(List.of(new FmpStockProfile()));

        mockMvc.perform(post("/api/fmp/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[\"AAPL\"]}"))
                .andExpect(status().isOk());

        verify(fmpProfileService).fetchAndSave(List.of("AAPL"));
    }

    @Test
    void fetchProfilesRejectsEmptySymbolList() throws Exception {
        mockMvc.perform(post("/api/fmp/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[]}"))
                .andExpect(status().isBadRequest());
    }
}

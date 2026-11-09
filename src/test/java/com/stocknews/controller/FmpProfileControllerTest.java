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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FmpProfileController.class)
class FmpProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FmpProfileService fmpProfileService;

    @Test
    void fetchProfilesAcceptsSymbolList() throws Exception {
        FmpStockProfile profile = new FmpStockProfile();
        profile.setSymbol("AAPL");
        profile.setCompanyName("Apple Inc.");
        profile.setCountry("US");
        when(fmpProfileService.findSavedProfiles(List.of("AAPL"))).thenReturn(List.of(profile));

        mockMvc.perform(post("/api/fmp/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[\"AAPL\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].companyName").value("Apple Inc."))
                .andExpect(jsonPath("$[0].country").value("US"));

        var serviceCalls = inOrder(fmpProfileService);
        serviceCalls.verify(fmpProfileService).fetchAndSave(List.of("AAPL"));
        serviceCalls.verify(fmpProfileService).findSavedProfiles(List.of("AAPL"));
    }

    @Test
    void fetchProfilesRejectsEmptySymbolList() throws Exception {
        mockMvc.perform(post("/api/fmp/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbols\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSavedProfilesAcceptsCommaSeparatedSymbolsAndReturnsFullProfiles() throws Exception {
        FmpStockProfile aapl = new FmpStockProfile();
        aapl.setSymbol("AAPL");
        aapl.setCompanyName("Apple Inc.");
        aapl.setCountry("US");
        FmpStockProfile msft = new FmpStockProfile();
        msft.setSymbol("MSFT");
        msft.setCompanyName("Microsoft Corporation");
        msft.setCountry("US");
        when(fmpProfileService.findSavedProfiles(List.of("AAPL", "MSFT")))
                .thenReturn(List.of(aapl, msft));

        mockMvc.perform(get("/api/fmp/profiles").param("symbols", "AAPL,MSFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].country").value("US"))
                .andExpect(jsonPath("$[0].companyName").value("Apple Inc."))
                .andExpect(jsonPath("$[1].symbol").value("MSFT"));

        verify(fmpProfileService).findSavedProfiles(List.of("AAPL", "MSFT"));
    }

    @Test
    void getSavedProfilesRequiresSymbols() throws Exception {
        mockMvc.perform(get("/api/fmp/profiles"))
                .andExpect(status().isBadRequest());
    }
}

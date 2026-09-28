package com.stocknews.fmp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.FmpProperties;
import com.stocknews.persistence.FmpStockProfile;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FmpProfileClientTest {
    @Test
    void fetchesAndMapsProfileWhilePreservingFullRawRecord() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        FmpProperties properties = new FmpProperties();
        properties.setApiKey("test-api-key");
        FmpProfileClient client = new FmpProfileClient(
                restClientBuilder.build(), properties, new ObjectMapper());
        server.expect(requestTo("https://financialmodelingprep.com/stable/profile"
                        + "?symbol=AAPL&apikey=test-api-key"))
                .andRespond(withSuccess("""
                        [{
                          "symbol": "AAPL",
                          "companyName": "Apple Inc.",
                          "price": 210.5,
                          "marketCap": 3200000000000,
                          "fullTimeEmployees": "161000",
                          "isActivelyTrading": true,
                          "customProviderField": "preserved"
                        }]
                        """, MediaType.APPLICATION_JSON));

        FmpStockProfile profile = client.fetchProfile("AAPL");

        assertEquals("AAPL", profile.getSymbol());
        assertEquals("Apple Inc.", profile.getCompanyName());
        assertEquals(0, profile.getPrice().compareTo(new java.math.BigDecimal("210.5")));
        assertEquals(161000L, profile.getFullTimeEmployees());
        assertTrue(profile.getActivelyTrading());
        assertTrue(profile.getRawPayload().contains("\"customProviderField\":\"preserved\""));
        server.verify();
    }
}

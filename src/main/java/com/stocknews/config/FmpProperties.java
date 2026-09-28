package com.stocknews.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fmp")
@Data
public class FmpProperties {
    private String baseUrl = "https://financialmodelingprep.com/stable";
    private String apiKey;
    private int dailyRequestLimit = 250;
    private boolean usExchangesOnly = true;
    private int profileCacheHours = 24;
}

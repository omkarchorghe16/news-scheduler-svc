package com.stocknews.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuration for Spring 6.1+ RestClient with timeout and retry settings.
 */
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final AppProperties appProperties;

    @Bean
    public RestClient restClient() {
        int timeoutMs = appProperties.getApis().getTimeoutSeconds() * 1000;
        return RestClient.builder()
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                    {
                        setConnectTimeout(Duration.ofMillis(timeoutMs));
                        setReadTimeout(Duration.ofMillis(timeoutMs));
                    }
                })
                .build();
    }
}

package com.stocknews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Stock News Scheduler Service application.
 * Fetches daily stock market news for US and India markets,
 * deduplicates, and sends formatted digests to Slack, Telegram, and WhatsApp.
 */
@SpringBootApplication
@EnableScheduling
public class StockNewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockNewsApplication.class, args);
    }
}

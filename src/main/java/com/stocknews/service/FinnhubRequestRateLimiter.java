package com.stocknews.service;

import org.springframework.http.HttpStatus;
import com.stocknews.logging.ExceptionLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class FinnhubRequestRateLimiter {
    private static final long MINIMUM_INTERVAL_NANOS = 1_000_000_000L;
    private long nextAllowedRequestNanos;

    public synchronized void awaitNextCall() {
        long waitNanos = nextAllowedRequestNanos - System.nanoTime();
        if (waitNanos > 0) {
            try {
                long millis = waitNanos / 1_000_000L;
                int nanos = (int) (waitNanos % 1_000_000L);
                Thread.sleep(millis, nanos);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for Finnhub rate limit:\n{}",
                        ExceptionLog.stackTrace(exception));
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Interrupted while waiting for Finnhub rate limit", exception);
            }
        }
        nextAllowedRequestNanos = System.nanoTime() + MINIMUM_INTERVAL_NANOS;
    }
}

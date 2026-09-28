package com.stocknews.alphavantage;

import com.stocknews.persistence.AlphaVantageApiCall;
import com.stocknews.persistence.postgres.AlphaVantageApiCallRepository;
import com.stocknews.logging.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlphaVantageRateLimiter {
    private static final int MAX_PER_MINUTE = 500;
    private static final int MAX_PER_DAY = 2500;
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final AlphaVantageApiCallRepository callRepository;

    public synchronized void reserveRequest() {
        LocalDateTime startOfToday = LocalDate.now(ZONE).atStartOfDay();
        if (callRepository.countByCalledAtAfter(startOfToday.minusNanos(1)) >= MAX_PER_DAY) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Alpha Vantage daily request limit of 2500 has been reached");
        }

        LocalDateTime now = LocalDateTime.now(ZONE);
        while (callRepository.countByCalledAtAfter(now.minusMinutes(1)) >= MAX_PER_MINUTE) {
            LocalDateTime oldestRecentCall = callRepository
                    .findFirstByCalledAtAfterOrderByCalledAtAsc(now.minusMinutes(1))
                    .orElseThrow()
                    .getCalledAt();
            long waitMillis = Duration.between(now, oldestRecentCall.plusMinutes(1)).toMillis();
            try {
                Thread.sleep(Math.max(waitMillis, 1));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for Alpha Vantage rate limit:\n{}",
                        ExceptionLog.stackTrace(exception));
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Interrupted while waiting for Alpha Vantage rate limit", exception);
            }
            now = LocalDateTime.now(ZONE);
        }

        AlphaVantageApiCall call = new AlphaVantageApiCall();
        call.setCalledAt(now);
        callRepository.saveAndFlush(call);
    }
}

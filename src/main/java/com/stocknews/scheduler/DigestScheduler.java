package com.stocknews.scheduler;

import com.stocknews.digest.DigestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for running the daily digest.
 * Cron expression is configurable via app.schedule.cron-expression (default: weekdays 9 AM America/Chicago).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DigestScheduler {
    private final DigestService digestService;

    /**
     * Scheduled job that runs the daily digest.
     * Default: 9:00 AM on weekdays (Mon-Fri) in America/Chicago timezone.
     * Configure via: app.schedule.cron-expression
     */
    @Scheduled(cron = "${app.schedule.cron-expression:0 0 9 * * MON-FRI}", zone = "${app.schedule.timezone:America/Chicago}")
    public void runScheduledDigest() {
        log.info("Executing scheduled daily digest");
        try {
            digestService.runDailyDigest();
        } catch (Exception e) {
            log.error("Scheduled digest job failed", e);
        }
    }
}

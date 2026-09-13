package com.stocknews.web;

import com.stocknews.digest.DigestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for on-demand digest triggers.
 * Provides endpoints to manually trigger daily and portfolio digests without waiting for scheduled execution.
 */
@RestController
@RequestMapping("/api/digest")
@RequiredArgsConstructor
@Slf4j
public class DigestTriggerController {
    private final DigestService digestService;

    /**
     * POST /api/digest/run - Trigger the full daily digest immediately.
     */
    @PostMapping("/run")
    public ResponseEntity<String> triggerDailyDigest() {
        log.info("On-demand daily digest triggered via REST endpoint");
        try {
            digestService.runDailyDigest();
            return ResponseEntity.ok("Daily digest executed successfully");
        } catch (Exception e) {
            log.error("Error executing daily digest", e);
            return ResponseEntity.status(500).body("Error executing digest: " + e.getMessage());
        }
    }

    /**
     * POST /api/digest/portfolio - Trigger the portfolio-specific digest.
     */
    @PostMapping("/portfolio")
    public ResponseEntity<String> triggerPortfolioDigest() {
        log.info("On-demand portfolio digest triggered via REST endpoint");
        try {
            digestService.runPortfolioDigest();
            return ResponseEntity.ok("Portfolio digest executed successfully");
        } catch (Exception e) {
            log.error("Error executing portfolio digest", e);
            return ResponseEntity.status(500).body("Error executing digest: " + e.getMessage());
        }
    }

    /**
     * GET /api/digest/health - Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Stock News Scheduler Service is running");
    }
}

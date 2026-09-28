package com.stocknews.web;

import com.stocknews.digest.DigestService;
import com.stocknews.logging.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for on-demand digest triggers.
 * Provides endpoints to manually trigger daily and portfolio digests without waiting for scheduled execution.
 */
@RestController
@RequestMapping("/api/digest")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Digest", description = "Trigger scheduled news digests")
public class DigestTriggerController {
    private final DigestService digestService;

    /**
     * POST /api/digest/run - Trigger the full daily digest immediately.
     */
    @PostMapping("/run")
    @Operation(summary = "Run the daily digest")
    public ResponseEntity<String> triggerDailyDigest() {
        log.info("On-demand daily digest triggered via REST endpoint");
        try {
            digestService.runDailyDigest();
            return ResponseEntity.ok("Daily digest executed successfully");
        } catch (Exception e) {
            log.error("Error executing daily digest:\n{}", ExceptionLog.stackTrace(e));
            return ResponseEntity.status(500).body("Error executing digest: " + e.getMessage());
        }
    }

    /**
     * POST /api/digest/portfolio - Trigger the portfolio-specific digest.
     */
    @PostMapping("/portfolio")
    @Operation(summary = "Run the portfolio digest")
    public ResponseEntity<String> triggerPortfolioDigest() {
        log.info("On-demand portfolio digest triggered via REST endpoint");
        try {
            digestService.runPortfolioDigest();
            return ResponseEntity.ok("Portfolio digest executed successfully");
        } catch (Exception e) {
            log.error("Error executing portfolio digest:\n{}", ExceptionLog.stackTrace(e));
            return ResponseEntity.status(500).body("Error executing digest: " + e.getMessage());
        }
    }

    /**
     * GET /api/digest/health - Health check endpoint.
     */
    @GetMapping("/health")
    @Operation(summary = "Check the digest service")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Stock News Scheduler Service is running");
    }
}

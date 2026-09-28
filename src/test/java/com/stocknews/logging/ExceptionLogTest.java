package com.stocknews.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExceptionLogTest {
    @Test
    void includesStackTraceAndRedactsCredentialsFromRequestUrls() {
        IllegalStateException exception = new IllegalStateException(
                "Request failed: https://api.example.test/data?token=secret&symbol=AAPL "
                        + "https://api.telegram.org/bottelegram-secret/sendMessage "
                        + "https://hooks.slack.com/services/T123/B456/secret");

        String stackTrace = ExceptionLog.stackTrace(exception);

        assertTrue(stackTrace.contains("java.lang.IllegalStateException"));
        assertTrue(stackTrace.contains("ExceptionLogTest"));
        assertTrue(stackTrace.contains("?[REDACTED]"));
        assertTrue(stackTrace.contains("/bot[REDACTED]/"));
        assertTrue(stackTrace.contains("https://hooks.slack.com/services/[REDACTED]"));
        assertFalse(stackTrace.contains("telegram-secret"));
        assertFalse(stackTrace.contains("T123/B456/secret"));
        assertFalse(stackTrace.contains("token=secret"));
    }
}

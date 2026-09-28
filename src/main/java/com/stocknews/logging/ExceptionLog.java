package com.stocknews.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionLog {
    private ExceptionLog() {
    }

    public static String stackTrace(Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString()
                .replaceAll("(?i)(https?://[^\\s?]+)\\?[^\\s]+", "$1?[REDACTED]")
                .replaceAll("(?i)(/bot)[^/\\s]+", "$1[REDACTED]")
                .replaceAll("(?i)(https://hooks\\.slack\\.com/services/)[^\\s]+", "$1[REDACTED]")
                .replaceAll("(?i)(authorization\\s*[:=]\\s*)(?:basic|bearer)\\s+[^\\s,;]+", "$1[REDACTED]")
                .replaceAll("(?i)((?:api[_-]?key|token|password)=)[^&\\s]+", "$1[REDACTED]");
    }
}

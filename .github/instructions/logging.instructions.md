---
applyTo: "**/*.java"
---

# Logging standards

- Use SLF4J through Lombok `@Slf4j` or an existing class logger; do not use `System.out` or
  `System.err`.
- Log useful lifecycle events at INFO, recoverable provider/configuration problems at WARN, and
  unexpected failures at ERROR. Use DEBUG for diagnostic detail that is too noisy for normal operation.
- Include operation/provider and safe context such as symbol count, saved-record count, or status.
- Never log API keys, authorization headers, Telegram bot tokens, full URLs containing credentials,
  raw provider responses, business-summary text, or other sensitive payloads.
- Do not log the same exception at multiple layers. Log an error where it can be handled or acted upon,
  and rethrow/propagate it when the request must fail.
- Keep log messages parameterized (`log.info("Fetched {} profiles", count)`).
- Avoid logging entire request objects or lists when a count or a sanitized identifier is sufficient.
- Log provider throttling and persistence mirror failures with provider/database context, but omit
  API keys and credentials even when they appear in query parameters or exception messages.

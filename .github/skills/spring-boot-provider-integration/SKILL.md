---
name: spring-boot-provider-integration
description: Add or change a third-party HTTP provider integration, including RestClient behavior, typed parsing, rate limits, persistence, safe errors, and tests.
---

# External provider integration workflow

Use this skill for Finnhub, Yahoo Finance, Alpha Vantage, FMP, news providers, notification APIs,
or another remote service.

## Project conventions

- Keep provider-specific HTTP and response parsing in that provider's client package/class; keep
  orchestration, normalization, and persistence in a service.
- Reuse the configured Spring `RestClient` bean from `RestClientConfig`, which applies the shared
  timeout settings. Do not create clients with unbounded timeouts.
- Bind provider URLs, credentials, limits, and feature switches using `AppProperties` or the
  provider-specific configuration properties class. Document names/defaults in
  `src/main/resources/application.yml`; keep real values in environment variables, the ignored
  local `.env` used by Docker Compose, or the ignored root-level `application-secrets.yml` for direct
  local runs. Treat the existing root-level `application-secrets.yml` as persistent developer-owned
  configuration: never overwrite, regenerate, or relocate its values during routine changes.
  `src/main/resources/application-local.yml` is tracked and must not contain actual credentials or
  be edited to move/copy local secrets. Ensure the active profile does not override imported secrets
  with empty placeholders; restart the app after editing the secrets file.
- Normalize stock symbols once at the service boundary with the existing normalizer when applicable.
- Enforce provider rate limits before issuing requests. Use persisted usage records for durable
  quotas when the provider service already follows that pattern.
- Map provider responses into typed models. Keep error/status translation close to the provider
  client and propagate failures rather than returning empty success-shaped data.
- Keep provider-specific HTTP APIs in dedicated controllers when separating them improves API
  clarity. `POST /api/fmp/profiles` accepts a `symbols` list, upserts fetched/cached profile data by
  ticker symbol, then reads and returns the persisted full profiles. `GET /api/fmp/profiles?symbols=AAPL,MSFT`
  reads full saved profiles without making provider requests.
- Logs must not contain credentials, authorization headers, full credential-bearing URLs, raw
  provider responses, business-summary text, or unsanitized exception details that can reveal them.

## Implementation sequence

1. Inspect the provider's properties, client, service, persistence model/repository, related tests,
   and documented API limits.
2. Add or update configuration with safe defaults and no committed credentials.
3. Implement request construction, response parsing, and provider error mapping in the client.
4. Put normalization, caching/quota checks, and persistence orchestration in the service, preserving
   transaction boundaries and avoiding transactions around slow network calls unless required.
5. Add deterministic tests using the existing `MockRestServiceServer`, WireMock, or service mocks.
   Cover success parsing, empty/malformed responses, HTTP failure, missing configuration, and quota
   boundaries as relevant. Never make live provider requests in tests.
6. Update controller OpenAPI docs and Postman examples if the external integration is reachable
   through a public endpoint.
7. Run the provider-specific tests and then compile/package the application.
8. Review every `.github/skills/*/SKILL.md`; update all skills affected by provider behavior or
   shared conventions. Update `README.md` and directly related API, configuration, rate-limit, or
   operational documentation.

## Verification checklist

- Timeout and retry behavior are bounded and intentional.
- Rate limits and cache behavior match the provider plan and cannot be reset accidentally on restart
  if the limit is meant to be durable.
- Failure status is actionable and does not expose upstream payloads or credentials.
- Tests verify mapping and error cases without relying on network access.

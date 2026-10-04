---
name: spring-boot-rest-api
description: Implement or change REST endpoints in this Spring Boot service, including validation, OpenAPI, MockMvc coverage, and Postman requests.
---

# Spring Boot REST API workflow

Use this skill when adding or changing HTTP routes, request/response contracts, status codes, or
controller tests.

## Project conventions

- Use Java 21 and Spring Boot 3.5.x.
- Put controllers in `src/main/java/com/stocknews/controller` and map request/response types in
  `com.stocknews.dto`.
- Keep controllers limited to HTTP mappings, boundary validation, service delegation, and response
  construction. Do not call repositories or provider clients from a controller.
- Use typed request/response DTOs and Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`,
  `@NotEmpty`, `@Size`, etc.). Do not duplicate business rules in the controller.
- Use explicit status codes. Existing create endpoints use `201 Created`, response bodies, and a
  `Location` header; deletes use `204 No Content`.
- Document endpoints with springdoc OpenAPI `@Operation`, `@ApiResponse`/`@ApiResponses`, and
  `@Tag` where appropriate.
- Preserve the existing `GlobalExceptionHandler` behavior and ProblemDetail response format.
- Provider-specific profile endpoints may use dedicated controllers and route roots; the FMP profile
  API is `POST /api/fmp/profiles`. Bulk stock-symbol ingestion is `POST /api/stock-symbols` and
  requires `sectorId` plus a non-empty `tickers` list.

## Implementation sequence

1. Inspect the matching controller, service, DTOs, exception handler, OpenAPI setup, and nearby tests.
2. Define or update typed DTO validation and keep the service as the owner of use-case rules.
3. Add or update the controller mapping and OpenAPI documentation.
4. Add `@WebMvcTest`/MockMvc tests for normal status/body/header behavior, service delegation, and
   invalid request validation. Mock the service, not its repositories.
5. Update `postman/stock-news-scheduler.postman_collection.json` for public API contract changes.
   Use `{{baseUrl}}` and non-secret request data. Keep any generated/local Postman exports in sync
   only when those files are intentionally maintained by the repository. Share the same collection
   across imported local, AWS Dev, and AWS Production environments by setting `baseUrl`.
6. Run the affected controller tests, then `mvn -q -DskipTests package` if the changed API affects
   compilation outside the test slice.
7. Review every `.github/skills/*/SKILL.md`; update all skills affected by the API or shared
   conventions. Update `README.md`, OpenAPI, Postman, and any directly related API documentation
   so their contracts agree with the implementation.

## Verification checklist

- Route and HTTP method are correct and do not conflict with sibling mappings.
- Required values are validated at the HTTP boundary; service rules still work for non-HTTP callers.
- Success and failure status codes match the existing API behavior.
- OpenAPI and Postman describe the actual DTO fields and behavior.
- Tests do not call live databases or external providers.
- No credentials, provider payloads, or sensitive request values are added to logs or fixtures.

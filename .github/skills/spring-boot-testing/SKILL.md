---
name: spring-boot-testing
description: Design and run focused JUnit 5 tests for this Spring Boot application, including MockMvc controller tests and HTTP-client/service tests.
---

# Spring Boot testing workflow

Use this skill when adding behavior, fixing regressions, changing endpoints, or validating a
provider/database workflow.

## Project conventions

- Use the existing Maven test stack: JUnit 5, Spring Boot Test, Mockito, MockMvc, H2, and WireMock.
  Do not add a new test framework or dependency for routine coverage.
- Controller tests use `@WebMvcTest`, MockMvc, and mocked service dependencies. Assert response
  status, JSON contract, headers, validation, and service delegation as applicable.
- When a route moves to another controller, move/update its controller tests and ensure the old
  route is not left documented as current. Add contract coverage for new routes such as
  `POST /api/stock-symbols` using a mocked `StockService`.
- Provider HTTP client tests should isolate requests with the existing `MockRestServiceServer` or
  WireMock; tests must never require live provider credentials or internet access.
- Database tests use the `test` profile and H2 configured in `src/test/resources/application.yml`.
  Do not require a developer's local PostgreSQL instance.
- The GitHub Actions workflow deploys `main` to ECS after tests pass on pushes and manual dispatches
  of `main`, then checks readiness, health, metrics, and Prometheus endpoints over the configured
  CloudFormation `ApplicationUrl` (HTTP or HTTPS). Pull requests and manual runs from other branches
  do not assume AWS credentials or deploy.
- Keep test fixtures synthetic. Do not use real tokens, API keys, personal data, or full live
  provider payloads.
- The Postman collection is shared across local, AWS Dev, and AWS Production environments through
  `{{baseUrl}}`. Its mutation, digest-delivery, and provider requests can have real side effects;
  avoid running those requests against production unless explicitly intended.

## Implementation sequence

1. Locate nearby tests and preserve their naming, package, and setup conventions.
2. Identify the behavior boundary: controller contract, service rule, provider mapping, or
   persistence behavior. Test at that boundary instead of loading the entire application unless
   the integration itself is under test.
3. Cover normal behavior and the most important invalid/error path, especially status codes and
   validation when the public contract is involved.
4. Prefer assertions on externally meaningful results and interactions; avoid brittle private
   implementation assertions.
5. Run only the affected test classes first, for example:
   `mvn -q -Dtest=SectorControllerTest,StockControllerTest test`
6. If a test fails, distinguish product regressions from environment/profile or test setup failures;
   do not weaken assertions merely to make the suite pass.
7. For the feature under test, review every `.github/skills/*/SKILL.md` and update affected skills,
   `README.md`, and directly related documentation so test guidance and documented behavior stay
   aligned with the implementation.

## Verification checklist

- Tests are deterministic and isolated from external services.
- New public API behavior has MockMvc coverage.
- Changed provider behavior has request/response mapping and failure coverage.
- The targeted Maven test command exits successfully before reporting the change as verified.

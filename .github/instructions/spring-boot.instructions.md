---
applyTo: "**/*.java"
---

# Spring Boot standards

- This project uses Java 21 and Spring Boot 3.5.x. Preserve the Maven dependency-management model
  and use existing dependencies unless a requirement cannot be met otherwise.
- Use Spring-managed components with the narrowest appropriate stereotype (`@RestController`,
  `@Service`, `@Component`, or `@Repository`).
- Prefer constructor injection and immutable dependencies.
- Use Spring configuration properties for configurable behavior; avoid reading environment variables
  directly from business logic.
- Map request bodies to typed DTOs and validate them with `@Valid` and Jakarta Validation.
- Return intentional HTTP status codes and consistent error behavior; let established exception
  handling propagate rather than hiding failures.
- Keep blocking provider calls within configured timeouts and respect documented provider rate limits.
- Use JPA annotations and repository methods consistent with existing entities; make upsert behavior
  explicit and safe for repeated requests.
- PostgreSQL is the application datasource; H2 is used by tests through
  `src/test/resources/application.yml`. Do not imply or implement a production H2 mirror without
  an explicitly approved architecture change.
- Test web mappings with MockMvc and isolate provider/database behavior in appropriate tests.
- Document every REST API in OpenAPI annotations and keep `/v3/api-docs` and Swagger UI current.
- External-provider rate limits must be enforced before issuing calls. Count durable daily quotas
  from persisted calls where possible so application restarts do not reset the daily allowance.
- Use `postman/stock-news-scheduler.postman_collection.json` for API examples and assertions; never
  include credentials in the collection or local environment export.
- When behavior or functionality changes, review every skill in `.github/skills/` and update
  affected skills, `README.md`, and directly related API/configuration/operations documentation.

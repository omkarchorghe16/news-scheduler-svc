---
applyTo: "**/*.java"
---

# Spring Boot standards

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
- Keep dev-only tools such as the H2 console disabled by default and enable them only in local config.
- Test web mappings with MockMvc and isolate provider/database behavior in appropriate tests.
- Document every REST API in OpenAPI annotations and keep `/v3/api-docs` and Swagger UI current.
- Configure PostgreSQL as the primary JPA datasource and H2 as a separate mirror datasource; avoid
  relying on implicit Spring Boot datasource auto-configuration for the mirror.
- External-provider rate limits must be enforced before issuing calls. Count durable daily quotas
  from persisted calls where possible so application restarts do not reset the daily allowance.

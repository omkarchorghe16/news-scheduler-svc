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

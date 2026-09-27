---
applyTo: "**/*.java"
---

# Java coding standards

- Use Java 21 and existing Spring Boot, Lombok, and project conventions.
- Prefer small, cohesive classes and methods with explicit types and clear names.
- Keep controller methods limited to HTTP mapping, validation, delegation, and response handling.
- Use constructor injection; use Lombok `@RequiredArgsConstructor` where consistent with nearby code.
- Validate external input at the boundary with Jakarta Bean Validation and explicit constraints.
- Preserve type safety; avoid unchecked casts, `Object`-typed APIs, and unnecessary abstractions.
- Handle expected provider failures explicitly. Do not swallow exceptions, return empty success-shaped
  values for errors, or add broad catches.
- Keep API keys and credentials outside source-controlled files. Read configuration from environment
  variables or an ignored local secrets file; do not expose secrets in logs, tests, fixtures, or Postman.
- Add focused tests for new behavior and update relevant documentation.
- Run the smallest relevant existing build/test commands after changing code.

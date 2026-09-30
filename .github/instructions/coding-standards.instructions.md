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
- Keep package names descriptive, lowercase, and consistent with their source directories.
- Add provider integrations behind provider-specific clients; represent returned fields using typed
  persistence/domain models rather than unstructured maps.
- Keep endpoint changes in sync across the controller, OpenAPI annotations, MockMvc tests, and the
  Postman collection when the public API changes.
- For each new or changed feature, review every skill under `.github/skills/` and update all skills
  affected by the implementation or conventions. Keep `README.md` and directly related project
  documentation current; do not leave relevant skill or user-facing documentation stale.
- Do not add secrets or live provider credentials to source, tests, Postman requests/environments,
  logs, or committed configuration.
- Run the smallest relevant existing build/test commands after changing code.

---
applyTo: "**/*.java"
---

# Architecture standards

- Keep the application layered: `controller` handles HTTP, `service`/feature packages orchestrate
  use cases, provider clients handle external APIs, and `model`/`repository` own JPA entities and
  database access.
- Controllers must not call provider clients or repositories directly; delegate through a service.
- Keep provider-specific API parsing and error translation in that provider's client.
- Keep JPA entities focused on persistence. Do not put HTTP behavior or orchestration in entities.
- Use Spring Data repositories for database access and service-level transactions when a workflow
  needs atomic persistence.
- Normalize and validate symbols once at the service boundary before calling providers or querying.
- Bind external configuration using the existing `AppProperties` or provider-specific
  `@ConfigurationProperties` classes; document defaults and environment variable names in
  `application.yml`. Do not hard-code credentials.
- Keep provider schemas and persistence tables separate when fields or units differ.
- Do not create cyclic dependencies between web, service, client, and persistence layers.
- Use descriptive lowercase package names (`notification`, not abbreviations) and keep source
  directories aligned with package declarations.
- PostgreSQL is the application datasource. H2 is configured for tests only; do not introduce a
  production H2 mirror or treat test H2 data as authoritative without an explicit architecture
  decision.
- Put transaction boundaries on service workflows, not controllers or repositories, unless an
  existing repository convention requires otherwise.

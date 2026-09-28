---
applyTo: "**/*.java"
---

# Architecture standards

- Keep the application layered: `web` handles HTTP, domain/service packages orchestrate use cases,
  provider clients handle external APIs, and `persistence` owns JPA entities and repositories.
- Controllers must not call provider clients or repositories directly; delegate through a service.
- Keep provider-specific API parsing and error translation in that provider's client.
- Keep JPA entities focused on persistence. Do not put HTTP behavior or orchestration in entities.
- Use Spring Data repositories for database access and service-level transactions when a workflow
  needs atomic persistence.
- Normalize and validate symbols once at the service boundary before calling providers or querying.
- Make external dependencies configurable through `AppProperties` and `application.yml`; never
  hard-code deployment-specific URLs or credentials.
- Keep provider schemas and persistence tables separate when fields or units differ.
- Do not create cyclic dependencies between web, service, client, and persistence layers.
- Use descriptive lowercase package names (`notification`, not abbreviations) and keep source
  directories aligned with package declarations.
- PostgreSQL is the primary/read database. Mirror every persisted business record to H2 using
  `H2DatabaseMirror`; do not make H2 the source of truth.
- Dual writes are not a distributed transaction. Propagate mirror failures; never silently report
  success when only one database was updated. Document the consistency trade-off.
- Keep PostgreSQL and H2 credentials/URLs independently configurable and environment-specific.

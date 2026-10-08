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
- Local development uses the PostgreSQL service in `compose.yaml`; the application connects to
  `postgres:5432` inside Compose and `localhost:5433` when run from the host. The workflow in
  `.github/workflows/ci-cd.yml` packages/tests the application and builds a Docker image, but does
  not publish it or deploy because Docker Desktop Kubernetes is local-only. The local deploy script
  applies the image and smoke-tests readiness.
- Docker Desktop Kubernetes under `k8s/` is an optional local-only workflow. Keep it separate from
  the default Compose workflow and do not commit Kubernetes Secrets or local application credentials.
- Put transaction boundaries on service workflows, not controllers or repositories, unless an
  existing repository convention requires otherwise.
- For every feature or behavior change, review all files in `.github/skills/` and update every skill
  whose workflow or project guidance is affected. Keep shared conventions consistent across skills;
  do not churn unrelated skill content.
- Keep `README.md` and relevant API, configuration, and operational documentation synchronized with
  implemented behavior. Public API changes also require synchronized OpenAPI and Postman examples.

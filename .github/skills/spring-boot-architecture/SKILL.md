---
name: spring-boot-architecture
description: Plan or change application architecture, module boundaries, cross-cutting behavior, persistence topology, or shared Spring Boot conventions in this repository.
---

# Spring Boot architecture workflow

Use this skill when a change spans multiple layers/packages, moves responsibilities, changes
application-wide conventions, affects deployment topology, or introduces a new subsystem.

## Current application boundaries

- `controller` owns HTTP mappings, request validation, and response construction.
- `service` and feature packages coordinate use cases and transaction boundaries.
- Provider clients own external HTTP calls, provider-specific parsing, and provider error mapping.
- `model` contains JPA entities/domain persistence records; `repository` contains Spring Data access.
- `config` binds typed settings and configures shared infrastructure such as `RestClient`.
- `scheduler` triggers scheduled workflows; `digest` coordinates news fetching, deduplication,
  formatting, and notification delivery; `notification` owns channel-specific delivery.
- PostgreSQL is the application datasource. H2 is configured for tests only; there is no production
  H2 mirror in the current architecture.
- For local macOS development, `compose.yaml` runs only the Spring Boot container and connects it to
  host PostgreSQL through `host.docker.internal`; it mounts the ignored `application-secrets.yml`
  read-only and must never copy that file into the image.
- `k8s/` provides a Docker Desktop Kubernetes deployment that uses the Mac-hosted PostgreSQL through
  `host.docker.internal`; it keeps one replica because scheduling is instance-local. The local
  deployment helper syncs the ignored `application-secrets.yml` into a Kubernetes Secret mounted
  read-only at the path already imported by Spring, and GHCR image pulls use a separate pull Secret.
- The CI workflow builds/tests on GitHub-hosted runners, publishes multi-platform GHCR images, and
  deploys `main` through a trusted self-hosted macOS ARM64 runner to Docker Desktop Kubernetes.
  The protected `staging` environment supplies only the path to the runner's local secrets file;
  credentials remain on the Mac until synced into the cluster Secret. Post-deploy smoke tests run on
  that runner against the Kubernetes Service, not a separate local application process.

## Architecture rules

- Preserve one-way dependencies: controllers -> services/features -> clients/repositories. Avoid
  cycles and prevent controllers/entities from acquiring infrastructure responsibilities.
- Keep provider-specific transport and response interpretation behind provider clients. Keep HTTP
  transport for notification channels within channel notifier components.
- Use `AppProperties` or provider-specific `@ConfigurationProperties` for configurable settings.
  Keep credentials external to tracked files and redact secrets from logs and error responses.
- Put transaction boundaries around database workflows at the service/use-case layer. Avoid holding
  database transactions open across slow provider or notification calls unless atomicity explicitly
  requires it.
- Treat provider quotas, timeout, retries, caching, idempotency, and failure behavior as explicit
  design constraints. Preserve durable quota tracking when a provider limit is persisted today.
- Keep public transport DTOs distinct from persistence entities; update OpenAPI and Postman whenever
  public API behavior changes.
- Keep provider-specific API routes in appropriately scoped controllers (for example,
  `FmpProfileController` under `/api/fmp`) and stock-management routes separate from provider-fetch
  workflows (for example, `StockSymbolController` under `/api/stock-symbols`).
- Do not introduce another datasource, production H2 mirroring, migration framework, security
  mechanism, or new architecture pattern without confirming compatibility and the intended trade-off.
- Keep the local Docker/PostgreSQL split intact: Compose must not add a PostgreSQL service or volume
  for the Mac-hosted database. Keep local secrets out of the build context and image.
- Keep the Kubernetes deployment at one replica unless scheduled-work coordination is introduced;
  never commit live Kubernetes Secrets or credentials.

## Implementation sequence

1. Trace the end-to-end flow and inspect the affected controllers, services, clients, configuration,
   models, repositories, tests, and documentation before editing.
2. State the existing boundary and the smallest responsible layer for the change; keep behavior in
   that layer and avoid unrelated refactoring.
3. Identify impacts on runtime configuration, persistence/schema, transactions, public API, secrets,
   scheduling, provider limits, and failure/observability behavior.
4. Implement the smallest coherent change and add tests at the affected layer boundaries.
5. Review **every** skill in `.github/skills/`. Update all skills affected by new or changed shared
   behavior, conventions, or workflows; ensure the set remains mutually consistent without
   needlessly editing unrelated guidance.
6. Update `README.md` and all directly related API, configuration, architecture, deployment, or
   operational documentation. Keep OpenAPI and the Postman collection in sync for public APIs.
7. Run the smallest existing tests/build that exercise the change and report any remaining
   documentation or operational decision that needs owner input.

## Architecture review checklist

- Responsibilities and package placement match existing boundaries.
- Configuration is typed, environment-specific, and safe by default.
- Persistence and transaction semantics are explicit; PostgreSQL remains authoritative.
- Network failures, quotas, and external side effects have defined behavior.
- Tests cover relevant component boundaries without live external dependencies.
- README, relevant docs, and all affected skills match actual code; no secrets are exposed.

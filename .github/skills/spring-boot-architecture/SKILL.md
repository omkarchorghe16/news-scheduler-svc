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
- For local development, `compose.yaml` runs Spring Boot with the official PostgreSQL image from
  Docker Hub, persists database state in a named volume, and binds the host port to loopback. The app
  uses Compose DNS (`postgres:5432`); Mac database clients use `localhost:5433` by default. The
  ignored `.env` supplies datasource and provider/notification configuration as container environment
  variables. Never commit this file or copy it into the image.
- `k8s/` and `scripts/deploy-local-k8s.sh` remain an optional single-user Docker Desktop workflow
  using PostgreSQL on the Mac host. It keeps one replica because scheduling is instance-local and
  syncs the ignored local secrets file into a Kubernetes Secret.
- `infra/aws/cloudformation.yml` provisions the AWS deployment: public HTTPS ALB, ECS Fargate,
  private encrypted RDS PostgreSQL, ECR, Secrets Manager, CloudWatch Logs, and a repository-scoped
  `GitHubActionsRole` OIDC role. Its trust policy admits this repository's immutable GitHub OIDC
  owner/repository IDs on `main` for image publishing and the `staging` environment for deployment;
  `infra/aws/github-oidc-trust-policy.json` is the matching policy for an existing role. The
  template can reference an existing account-level
  GitHub OIDC provider or create one. The Fargate task uses public subnets for outbound provider calls, but its security
  group permits inbound application traffic only from the ALB. RDS remains private.
- The CI workflow builds and tests on GitHub-hosted runners, pushes a uniquely tagged image to ECR,
  and deploys `main` to ECS using short-lived OIDC credentials. Post-deploy smoke tests use the
  configured public HTTPS URL. The service remains at one task because scheduled jobs are
  instance-local; ECS deployments stop the old task before starting its replacement to avoid
  duplicate digests.
- Runtime provider/notification values and the generated application database login are stored in
  Secrets Manager. RDS's generated master credential is reserved for setup/migration. An optional
  temporary SSM-only EC2 host provides a private tunnel for importing the local Compose database;
  remove it when setup is complete. No AWS credentials or live application secrets belong in GitHub
  workflow files or the repository.

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
- Keep local Compose PostgreSQL isolated in its named volume and bound to host loopback; do not
  conflate it with the separate Mac-hosted PostgreSQL used by the Kubernetes configuration. Keep
  local `.env` secrets out of the build context and image.
- Keep Docker Desktop Kubernetes and ECS at one application instance unless scheduled-work
  coordination is introduced; never commit Kubernetes Secrets, AWS credentials, or application
  credentials.

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

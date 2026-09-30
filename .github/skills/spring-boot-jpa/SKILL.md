---
name: spring-boot-jpa
description: Add or change JPA entities, Spring Data repositories, transactions, and database-backed services in this PostgreSQL-backed Spring Boot application.
---

# Spring Data JPA workflow

Use this skill when changing persisted data, repository queries, or transactional service workflows.

## Project conventions

- PostgreSQL is the application datasource configured in `src/main/resources/application.yml`.
  H2 is configured for tests in `src/test/resources/application.yml`; there is no production H2
  mirror in the current architecture.
- JPA entities live in `com.stocknews.model`; Spring Data repositories live in
  `com.stocknews.repository`; use-case transactions belong in service classes.
- Preserve existing entity/table/column naming and typed models. Keep provider-specific records
  separate when their schemas or units differ.
- Keep HTTP concerns out of entities and repository interfaces.
- Use `@Transactional(readOnly = true)` for read workflows and `@Transactional` for mutations or
  multi-step workflows that need one database transaction.
- Match existing repository naming patterns and fetch requirements. Avoid introducing N+1 behavior
  when converting entity relations into response DTOs.
- The stock-symbol ingestion workflow delegates to `StockService.createBulk`; symbols are normalized,
  existing tickers in the same sector are skipped, and newly created records are returned.

## Implementation sequence

1. Inspect the entity, service, repository, datasource configuration, and existing tests that touch
   the data.
2. Make schema and relationship changes explicit. Check nullability, uniqueness, foreign keys,
   indexes, and entity lifecycle behavior.
3. Put query selection and business decisions in the service; keep repository methods focused on
   database access.
4. Add focused unit or repository/service tests using the existing test profile and H2. Keep tests
   independent from a developer's local PostgreSQL database.
5. If the data is exposed over HTTP, update its DTOs, OpenAPI documentation, MockMvc tests, and
   Postman collection as needed.
6. Run targeted tests and a Maven package/compile.
7. Review every `.github/skills/*/SKILL.md`; update all skills affected by the data model or shared
   conventions. Update `README.md` and relevant schema, setup, API, or operational documentation.

## Verification checklist

- Production configuration continues to use PostgreSQL; tests activate the test configuration.
- Transaction boundaries cover the intended write workflow without broadening transactions around
  network calls unnecessarily.
- Repository queries match the model and do not leak lazy entities to the web layer.
- Duplicate, missing-reference, and not-found behavior is explicit.
- No assumption of cross-database mirroring or distributed atomicity is introduced.

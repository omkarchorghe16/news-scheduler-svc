# Repository instructions

For every code change, follow the standards in:

- `.github/instructions/coding-standards.instructions.md`
- `.github/instructions/architecture.instructions.md`
- `.github/instructions/spring-boot.instructions.md`
- `.github/instructions/logging.instructions.md`

These are mandatory project conventions. Keep changes focused, preserve existing behavior unless
the request requires a behavior change, and add or update tests for changed behavior. Never commit
credentials or log secrets, request tokens, or full provider payloads.

## Copilot skills

Reusable task workflows are in `.github/skills/*/SKILL.md`. Load all relevant skills when working on
REST endpoints, architecture, JPA persistence, external providers, scheduled digests/notifications,
or tests. These skills supplement the repository-wide instructions above; the instruction files
remain authoritative for rules that apply to every code change.

## Documentation and skill synchronization

For every new or changed code behavior or functionality:

1. Review every skill in `.github/skills/`, not only the skill selected for the task. Update all
   skills whose instructions, examples, workflows, or project conventions are affected; keep shared
   guidance consistent across the full set. Do not make unrelated wording-only edits to unaffected
   skills.
2. Update `README.md` when features, setup, configuration, API usage, architecture, operations, or
   contributor workflows change.
3. Update any directly related documentation, including OpenAPI annotations, the Postman collection,
   configuration examples, and operational/deployment instructions, when their behavior or contract
   changes.
4. In the final handoff, identify any documentation intentionally not changed and why when a
   documentation surface is relevant but unchanged.

Do not treat this requirement as satisfied by updating code alone. Keep docs and skills accurate,
actionable, and consistent with the implemented application.

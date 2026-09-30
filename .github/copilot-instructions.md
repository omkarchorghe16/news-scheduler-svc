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

Reusable task workflows are in `.github/skills/*/SKILL.md`. Load the relevant skill when working on
REST endpoints, JPA persistence, external providers, scheduled digests/notifications, or tests. These
skills supplement the repository-wide instructions above; the instruction files remain authoritative
for rules that apply to every code change.

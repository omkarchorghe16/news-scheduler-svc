---
name: spring-boot-scheduling-notifications
description: Change scheduled digest jobs, news orchestration, message formatting, or Slack, Telegram, and WhatsApp delivery in this application.
---

# Digest and notification workflow

Use this skill for scheduled/on-demand digest changes, news-source orchestration, formatting, or
delivery-channel behavior.

## Project conventions

- `DigestScheduler` triggers `DigestService`; `DigestTriggerController` is the on-demand HTTP entry
  point. Keep scheduling separate from orchestration and delivery.
- `DigestService` composes news clients, deduplication, `DigestFormatter`, and notifier components.
  Keep provider-specific transport in news clients and channel-specific transport in notifier
  implementations.
- Add new delivery channels behind the existing `Notifier` interface where that contract fits.
  Existing notifier implementations report delivery success as a boolean; preserve that behavior
  unless deliberately changing and testing it.
- Keep schedules, time zones, watchlists, and notification/provider settings in `AppProperties` and
  `application.yml`. For Docker Compose local runs, supported schedule and WhatsApp settings can be
  overridden through the ignored `.env` file.
- Docker Compose runs a single application container locally. Avoid introducing multiple concurrent
  scheduler instances unless scheduled digest execution is coordinated across instances.
- Never log complete message bodies, raw provider responses, webhook URLs, bot tokens, or auth
  headers. Log operation and safe counts/status instead.
- Preserve the scheduler's resilience boundary and make failures visible through established
  logging/monitoring rather than silently treating them as successful delivery.

## Implementation sequence

1. Trace the path from scheduled or HTTP trigger through `DigestService` to the news clients,
   formatter, persistence, and notifier.
2. Implement behavior in the narrowest layer; do not move provider/channel HTTP logic into the
   scheduler or controller.
3. Add focused unit tests with mocked news clients, repositories, or `RestClient` transport. Cover
   empty inputs, duplicate articles, disabled/unconfigured channels, and delivery failures when
   relevant.
4. Update API docs and Postman only when the public HTTP contract changes.
5. Run the affected JUnit tests and a Maven compile/package.
6. Review every `.github/skills/*/SKILL.md`; update all skills affected by the behavior or shared
   conventions. Update `README.md` and directly related API, configuration, scheduling, or
   operational documentation.

## Verification checklist

- Scheduler settings and timezone remain configurable and test scheduling remains disabled.
- A failed source or delivery is not reported as successful without an explicit existing policy.
- Tests do not contact Slack, Telegram, WhatsApp, or news providers.
- Logs and test output contain no secrets or complete message/provider payloads.

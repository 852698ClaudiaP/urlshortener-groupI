# Feature: Redirect and click log

## Decision

**Owner module**: `links` (redirect) + `clicks` (log) | **Event-coupled**: Y (`ClickLoggedEvent`)

Look up a short URL by hash, return **307** to the target, and append a permanent click-log row via `ClickLoggedEvent` → `ClickRecorder`. Unknown hashes return **404**.

**Traps**: stateless redirect (no `HttpSession`); click logging must not block the redirect path; `ClickLoggedEvent` is an in-process event — cross-replica delivery requires Level 4.

**Technology hints**: `ClickRecorder` as `@ApplicationModuleListener`; `307 Temporary Redirect` with `Location` header; Spring Modulith event in `clicks` base package.

---

- **Weight:** 0 (seed — not in agreement)
- **Event-coupled:** Y

---

## Later

- **ADR:** [`docs/adr/0001-modulith-and-hexagon.md`](../adr/0001-modulith-and-hexagon.md), [`docs/adr/0002-atomic-upserts.md`](../adr/0002-atomic-upserts.md)

### Acceptance criteria

- [x] `GET /{hash}` → **307** with `Location` = target — `LinkFlowTests`
- [ ] Unknown hash → **404** — handler exists; **no automated test yet**
- [x] Successful redirect leads to click side-effect — `LinkFlowTests` reaches `totalClicks=1` (via analytics)
- [x] A redelivered `ClickLoggedEvent` (same `eventId`) is logged once — `RecordClickIdempotency(Postgres)Tests`

### Scale evidence

- [x] Level 3 — in-process `ClickLoggedEvent` event
- [ ] `docker compose --profile load run --rm k6` not run yet — concurrent `POST /api/link` and `GET /{hash}` through the load balancer. A failed run may lower the scalability score of every grown feature

### Qualities (self-assessed)

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | **0** | Redirect happy path green; claimed **404** untested → **0**. Re-test: unknown hash → 404; then **5**. |
| **Scalability** | **0** | No run data — architecture tests are Engineering, not Scalability. |
| **Engineering** | **1.5** | ADR `0001` + `ModularityTests` green; `./gradlew check` green; AI disclosure present. |

**Indicative total:** 0 + 0 + 1.5 = **1.5 / 10**.

### AI disclosure

- **Used for:** Adapting the seed feature from 25-26 course code to the 26-27 course code.
- **Human-reviewed:** The instructor reviewed the code and verified the correctness of the adaptation.
- **Cursor agent (Claude):** `eventId` on `ClickLoggedEvent`, idempotent `RecordClickService` (unique `click.event_id`), `RecordClickIdempotency(Postgres)Tests`.

# Feature: Link stats

## Decision

**Owner module**: `analytics` | **Event-coupled**: Y (`ShortUrlCreatedEvent`, `ClickLoggedEvent`)

Maintain per-hash `LinkStats` (not the raw click log; the only other table is a pruned set of processed click ids, see ADR 0002): zeroed on `ShortUrlCreatedEvent`, incremented on `ClickLoggedEvent`. Expose totals via `GET /api/stats/{hash}`. Unknown hashes return **404**.

**Traps**: `analytics` must never import `links` or `clicks` internals — it only consumes events from their base packages; computing totals on every request is O(N), materialise a read model instead.

**Technology hints**: `@ApplicationModuleListener` on both events; atomic HQL upsert (create-if-missing + increment, see ADR 0002) on the JPA read model (`LinkStats` entity); `GET /api/stats/{hash}` → 200 or 404.

---

- **Weight:** 0 (seed — not in agreement)
- **Event-coupled:** Y

---

## Later

- **ADR:** [`docs/adr/0001-modulith-and-hexagon.md`](../adr/0001-modulith-and-hexagon.md), [`docs/adr/0002-atomic-upserts.md`](../adr/0002-atomic-upserts.md)

### Acceptance criteria

- [x] `ShortUrlCreatedEvent` → zeroed stats — create then eventually readable stats in flow
- [x] `ClickLoggedEvent` → increment — `LinkFlowTests` asserts `"totalClicks\":1` after redirect
- [x] Concurrent clicks are all counted; a late `ShortUrlCreatedEvent` does not reset them — `LinkStatsConcurrencyTests`
- [x] A redelivered click (same `eventId`), sequential or concurrent, is counted once — `LinkStatsConcurrencyTests`
- [x] Processed click ids older than the configured retention are pruned daily — `ProcessedClickEventPruneTests`
- [ ] Unknown hash → **404** on `GET /api/stats/{hash}` — handler returns 404; **no automated test yet**

### Scale evidence

- [x] Level 3 — in-process events from `links` and `clicks`
- [ ] `docker compose --profile load run --rm k6` not run yet — concurrent `POST /api/link` and `GET /{hash}` through the load balancer. A failed run may lower the scalability score of every grown feature

### Qualities (self-assessed)

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | **0** | Happy path stats=1 automated; claimed **404** untested → **0**. Re-test: `GET /api/stats/missing` → 404; then **5**. |
| **Scalability** | **0** | No k6 run yet. Every project is tested with `docker compose --profile load run --rm k6`. |
| **Engineering** | **1.5** | ADR `0001` + `ModularityTests` green; `./gradlew check` green; AI disclosure present. |

**Indicative total:** 0 + 0 + 1.5 = **1.5 / 10**.

### AI disclosure

- **Used for:** Adapting the seed feature from 25-26 course code to the 26-27 course code.
- **Cursor agent (Claude)** was used for the atomic counter (ADR 0002): `JpaLinkStatsRepository`, `JpaLinkStatsStore`, `LinkStatsService`, processed-event ids (`ProcessedClickEventEntity`) and their scheduled prune (`ProcessedClickEventPruner`), `LinkStatsConcurrencyTests`, `ProcessedClickEventPruneTests`, and this card's updates.
- **Human-reviewed:** The instructor reviewed the code and verified the correctness of the adaptation.

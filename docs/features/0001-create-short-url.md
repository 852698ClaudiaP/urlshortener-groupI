# Feature: Create short URL

## Decision

**Owner module**: `links` | **Event-coupled**: Y (`ShortUrlCreatedEvent`)

Accept a target URL, validate it, mint a new base62 code from `short_url_seq`, persist a `ShortUrl`, and publish `ShortUrlCreatedEvent` so other modules can react.

**Traps**: invalid URLs must be rejected before minting; the same URL gets a new code on every create; the sequence must live in the database so restarts and both app replicas do not repeat codes.

**Technology hints**: `CommonsUrlValidator`; `short_url_seq` starting at `62^3` (4-character codes); `Base62`; Spring Modulith application event. See [ADR 0003](../adr/0003-sequence-short-codes.md).

---

- **Weight:** 0 (seed — not in agreement)
- **Event-coupled:** Y

---

## Later

- **ADR:** [`docs/adr/0001-modulith-and-hexagon.md`](../adr/0001-modulith-and-hexagon.md), [`docs/adr/0002-atomic-upserts.md`](../adr/0002-atomic-upserts.md), [`docs/adr/0003-sequence-short-codes.md`](../adr/0003-sequence-short-codes.md)

### Acceptance criteria

- [x] `POST /api/link` with form `url=` returns **201** and JSON `hash` + location — covered by `LinkFlowTests`
- [ ] Invalid URL → **400**
- [x] Publishes `ShortUrlCreatedEvent` — implied by stats bootstrap in `LinkFlowTests` + `LinkStatsListener`
- [x] Same URL (also concurrently) mints a distinct code of at least 4 characters — `CreateShortUrlConcurrencyTests`

### Scale evidence

- [x] Level 3 — in-process events via `ShortUrlCreatedEvent`
- [ ] `docker compose --profile load run --rm k6` not run yet — concurrent `POST /api/link` and `GET /{hash}` through the load balancer. A failed run may lower the scalability score of every grown feature

### Qualities (self-assessed)

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | **0** | No test for invalid URL. |
| **Scalability** | **0** | No run data available. |
| **Engineering** | **1.5** | ADR + `ModularityTests` green; `./gradlew check` green; AI disclosure present. |

**Indicative total:** 0 + 0 + 1.5 = **1.5 / 10**.

### AI disclosure

- **Tools / skills:** Model selected by Cursor Auto.
- **Used for:** Adapting the seed feature from 25-26 course code to the 26-27 course code.
- **Cursor agent (Grok):** sequence-backed base62 codes (`short_url_seq` from `62^3`), one new code per create, Guava removed.
- **Human-reviewed:** The instructor reviewed the code and verified the correctness of the adaptation.

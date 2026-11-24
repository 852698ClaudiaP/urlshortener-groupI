# Feature: URL Analytics

## Decision (required by 2 October)

**Owner module**: analytics | **Event-coupled**:  Y (ClickLoggedEvent)

Extends the seed GET /api/stats/{hash} endpoint with per-URL richer aggregates: unique
visitors, click rate per time window, and last-click timestamp. The event type ClickLoggedEvent
lives in clicks. links publishes it from LinkService on redirect, and analytics consumes it to
keep its own read model. The read model is what the stats endpoint serves. Scope is always scoped
to a single hash — not system-wide

**Traps**: computing totals on every request is O(N) — materialise a read model; mixing Micrometer
metrics with business analytics blurs module boundaries; unique visitor counting with plain IP
storage is a GDPR liability [11].

**Technology hints**: @ApplicationModuleListener on ClickLoggedEvent; @Scheduled timebucket roll-up; optional HyperLogLog [12] via Redis for unique counts.


---

- **Weight:** 13
- **Event-coupled:** Y

---

## Later (after the 2 October agreement)

- **ADR:** [docs/adr/0007-url-analytics.md](../adr/0007-url-analytics.md)

### Acceptance criteria

- [ ] **AC1: Initial stats for newly created short URL**: `GET /api/stats/{hash}` for an existing short URL with no redirects responds `200 OK`, and the JSON body contains `"totalClicks": 0`, `"uniqueVisitors": 0`, `"lastClick": null`, and `"recentClickRate": 0.0`.
- [ ] **AC2: Updated stats after redirect**: After a redirect `GET /{hash}` is performed, `GET /api/stats/{hash}` responds `200 OK`, and the JSON body contains `"totalClicks": 1`, `"uniqueVisitors": 1`, and a valid ISO-8601 `"lastClick"` timestamp.
- [ ] **AC3: Unique visitor deduplication**: When multiple redirects `GET /{hash}` occur from the same client IP or visitor token, `GET /api/stats/{hash}` responds `200 OK` with `"totalClicks"` incremented accordingly while `"uniqueVisitors"` remains `1`.
- [ ] **AC4: Time-window click rate**: `GET /api/stats/{hash}` responds `200 OK` including a time-window aggregation metric (such as click count or rate over the last rolling window) updated by the analytics read model.
- [ ] **AC5: Non-existent hash (Error case)**: `GET /api/stats/{hash}` with an unknown or non-existent hash responds `404 Not Found` with an RFC 9457 `ProblemDetail` body containing `status: 404` and `title: "Stats Not Found"`.

### Scale evidence

Fill this section with the evidence of the scalability of the feature.

### Qualities

Fill **Assessed** with the grade you claim; **How to test** must falsify that number if it failed.

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | 0 \| 5 | Acceptance tests for this feature |
| **Scalability** | 0–3.5 | Scale evidence above. Not `ModularityTests`. |
| **Engineering** | 0 \| 0.75 \| 1.5 | Hard gates: AI disclosure + `./gradlew check`. Then ADR + `ModularityTests`: both → **1.5**, one → **0.75**. |

**Indicative total:** _ / 10

### AI disclosure

- **Tools / skills:** …
- **Used for:** …
- **Human-reviewed:** …
- Or: **No AI assistance** was used for this feature.

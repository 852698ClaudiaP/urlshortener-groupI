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

- **ADR:** none | docs/adr/NNNN-….md *(by when a choice locks a module, the broker, or a library)*

### Acceptance criteria

- [ ] …
- [ ] …

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

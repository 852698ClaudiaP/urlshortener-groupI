# Feature cards (agreement)

Seed cards `0001`–`0003` are provided and ignored in the agreement. Teams add **grown** cards from `0004-…` upward.

## How to create a card (October 2)

1. Pick a feature from the [Feature Catalogue](https://moodle.unizar.es/add/mod/resource/view.php?id=12167300), or write a new catalogue entry in the same shape.
2. Copy [`0000-TEMPLATE.md`](0000-TEMPLATE.md) → `NNNN-short-title.md`.
3. Paste the catalogue entry into `## Decision` and choose a **weight** (5, 8, 13, or 21).
4. Leave `## Later` empty — fill it before the feature is graded.
5. Link the card in [`AGREEMENT.md`](../../AGREEMENT.md).

Personal names and ownership belong in your local `TEAM.md` (git-ignored), not in the card.

## Rules (locked)

- Weight ∈ {**5, 8, 13, 21**} (Fibonacci = ExpectedLevel 1–4)
- **Σ weights = 84** (spend the full budget)
- **≥ 4** grown features (one per member of a team of 4); **≥ 2** event-coupled
- One card per file: `NNNN-short-title.md`
- Owner module must match `AGENTS.md` (or a new module + ADR)
- ADR only when the feature **locks** a decision — see [`../adr/README.md`](../adr/README.md)
- **AI disclosure** and **`./gradlew check` green** are required before grading (not for the agreement)

## Seed (provided, not in agreement)

| ID | Feature | Owner | Event-coupled | Tests |
| --- | --- | --- | --- | --- |
| [0001](0001-create-short-url.md) | Create short URL | `links` | `ShortUrlCreatedEvent` | `POST /api/link`, `LinkFlowTests`, `CreateShortUrlConcurrency(Postgres)Tests` |
| [0002](0002-redirect-and-click-log.md) | Redirect and click log | `links` + `clicks` | `ClickLoggedEvent` | `GET /{hash}`, `LinkFlowTests`, `RecordClickIdempotency(Postgres)Tests` |
| [0003](0003-link-stats.md) | Link stats | `analytics` | `ShortUrlCreatedEvent`, `ClickLoggedEvent` | `GET /api/stats/{hash}`, `LinkFlowTests`, `LinkStatsConcurrency(Postgres)Tests`, `ProcessedClickEventPrune(Postgres)Tests` |

## Grown (index)

| ID | Feature | Owner module | Weight | Event-coupled | ADR |
| --- | --- | --- | --- | --- | --- |
| — | *(add from 0004-…)* | | | | |

**Budget used:** 0 / 84 · **Grown:** 0 / ≥4 · **Event-coupled:** 0 / ≥2

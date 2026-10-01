# Agent guidance — urlshortener

## Product

**Product = Boot jar(s) + this Compose file**, not "jar on localhost only."

## Commands

```bash
./gradlew bootRun              # Day-to-day: in-memory HSQLDB on :8080 (stop before Compose; same port)
./gradlew check                # All gates: ktlint + detekt + test + JaCoCo (60%)
./gradlew test                 # Modulith verify + hexagonal ArchUnit + flow test; JaCoCo per module under build/reports/jacoco/<module>/html
./gradlew test --tests '*PostgresTests'  # Concurrency cases on Postgres via Testcontainers (needs Docker; skipped without it)
./gradlew ktlintFormat         # Auto-fix style issues
./gradlew bootJar
docker compose up --build      # Graded product: Postgres + app1 + app2 + nginx LB on :8080
docker compose --profile load run --rm k6
# or from host:
BASE_URL=http://localhost:8080 k6 run scripts/load.k6.js
./scripts/scale-baseline.sh
```

## Module map (provided)

Each package below is a **Modulith application module**. Own your feature in one owner module; talk to others **only via events** in the base package (never import another module’s `domain` / `application` / `adapters`).

### Ownership

| Module      | Own                                                                                 | Do not                       |
|-------------|-------------------------------------------------------------------------------------|------------------------------|
| `links`     | Create/store/redirect short URLs (`ShortUrl`, `CreateShortUrl`, `RedirectShortUrl`) | Click history or `LinkStats` |
| `clicks`    | Append-only click log (`Click`); type `ClickLoggedEvent` lives here                  | Totals / `/api/stats`; publishing the click event |
| `analytics` | Per-hash counters (`LinkStats`, `UpdateLinkStats`, `GetLinkStats`)                  | Raw click rows               |

### HTTP API

| Module      | Endpoints                       |
|-------------|---------------------------------|
| `links`     | `POST /api/link` (form or multipart `url`, not JSON), `GET /{hash}` |
| `analytics` | `GET /api/stats/{hash}`         |
| `clicks`    | —           |

### Events

| Event | Type package | Publisher | When | Payload |
| --- | --- | --- | --- | --- |
| `ShortUrlCreatedEvent` | `links` | `links` (`LinkService.create`) | After a short URL is persisted | `hash`, `target`, `createdAt` |
| `ClickLoggedEvent` | `clicks` | `links` (`LinkService.redirect`) | After a successful redirect lookup | `hash`, `occurredAt`, `eventId` (consumers ignore repeats; [ADR 0002](docs/adr/0002-atomic-upserts.md)) |

| Module | Publishes | Consumes |
| --- | --- | --- |
| `links` | `ShortUrlCreatedEvent`, `ClickLoggedEvent` | — |
| `clicks` | — | `ClickLoggedEvent` via `ClickRecorder` |
| `analytics` | — | `ShortUrlCreatedEvent`, `ClickLoggedEvent` via `LinkStatsListener` |

**Do not confuse:** `clicks` = permanent log (many rows per hash); `analytics` = one `LinkStats` row per hash. Sole exception: `analytics` keeps processed `ClickLoggedEvent` ids (no hash) for deduplication, pruned after `urlshortener.analytics.processed-click-retention` ([ADR 0002](docs/adr/0002-atomic-upserts.md)). Growing a feature? Put it in the module that owns that data, or add a new module + ADR.

Each Modulith module is a **closed** application module with an internal hexagon:

- **Public API** = base package only (integration events such as `ShortUrlCreatedEvent`, `ClickLoggedEvent`)
- **Internal** = `domain` (where present), `application` (use cases + jMolecules ports), `adapters.*` (web, persistence, events, tech)
- **Hexagon** = jMolecules stereotypes; verified via `ensureHexagonal(SEMI_STRICT)` in `ModularityTests`

Do **not** break `ApplicationModules.verify()`. Prefer events over cross-module bean injection. Do not import another module’s `domain`, `application`, or `adapters` packages.

## Decisions

Grown changes that affect modules, hexagon enforcement, events, or specialised tech need an ADR under [`docs/adr/`](docs/adr/README.md) (template [`0000-TEMPLATE.md`](docs/adr/0000-TEMPLATE.md)). Seed rationale: [`0001-modulith-and-hexagon.md`](docs/adr/0001-modulith-and-hexagon.md).

## Grown (students)

Technologies beyond baseline HTTP (Call-Return, Event-Based, Data Flow), each in a distinct grown feature. Web UI is not graded. Level-4 broker path (`--profile broker` starts the stub only; apps are not wired to it). **Feature cards** under [`docs/features/`](docs/features/README.md). Defence packet: filled feature cards, ADR log, and a live Compose demo.

## Engineering quality ticks

**Hard gates** (any missing → **Engineering = 0**):

1. **AI disclosure** — required on the feature card (tools used **or** explicit “no AI assistance”)
2. **QA gate** — `./gradlew check` green (ktlint + detekt + test + JaCoCo ≥60%); CI must not be red

**With both gates present**, score the architecture ticks (an ADR records a locked choice — module, event, broker, or tech — not a mandatory new event on every card):

| ADR filed | `ModularityTests` green | Engineering |
| --- | --- | --- |
| Yes | Yes (`ApplicationModules.verify()` + `ensureHexagonal(SEMI_STRICT)`) | **1.5** |
| Exactly one | — | **0.75** |
| No | No | **0** |

## Feature cards

Seed (weight 0): [`0001`](docs/features/0001-create-short-url.md)–[`0003`](docs/features/0003-link-stats.md). Grown: copy [`0000-TEMPLATE.md`](docs/features/0000-TEMPLATE.md) → `0004-…`; update [`docs/features/README.md`](docs/features/README.md) index (Σ weights = 84, ≥ 4 grown, ≥ 2 event-coupled).

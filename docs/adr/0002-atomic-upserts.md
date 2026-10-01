# ADR 0002: Atomic HQL upserts and idempotent click handling

- **Status:** Accepted
- **Date:** 2026-09-24
- **Deciders:** Team / feature cards [`0001`](../features/0001-create-short-url.md), [`0002`](../features/0002-redirect-and-click-log.md), [`0003`](../features/0003-link-stats.md)

## Context

`analytics` kept `LinkStats.totalClicks` with read-then-write (`findByHash` + `save`), so concurrent `ClickLoggedEvent`s lost updates. `@ApplicationModuleListener` is async, so a click can also arrive before `ShortUrlCreatedEvent`; `onCreated` then reset the counter to 0.

`links` saved `ShortUrl` with `save` (assigned id → `merge`: `SELECT`, then `INSERT`). Concurrent creates of one URL both inserted → primary-key violation → 500. A different URL with the same hash silently overwrote the target.

Spring Modulith delivers at least once: its completion interceptor (`HIGHEST_PRECEDENCE + 10`) marks a publication completed *after* the listener's transaction commits. A crash in between leaves the publication incomplete; resubmitting it counted and logged the click twice.

## Decision

1. `LinkStatsStore` has `insertIfAbsent(hash)` and `incrementClicks(hash, eventId)`; `save` is removed (it overwrote `totalClicks`).
2. Each write is a Hibernate HQL upsert (`insert … on conflict (hash) do update`) in the listener's transaction. No native SQL; Hibernate renders it per dialect (`MERGE` on HSQLDB, `ON CONFLICT` on Postgres).
3. Increment: `values (:hash, 1) … do update set totalClicks = totalClicks + 1`.
4. `insertIfAbsent`: `values (:hash, 0)` with a no-op `do update set totalClicks = totalClicks`, never overwriting clicks. `do nothing` is not used: on HSQLDB Hibernate renders it as a plain `insert`, which fails on concurrent creation.
5. `LinkStatsListener` keeps `@ApplicationModuleListener`; `LinkStatsService` update methods stay `@Transactional`.
6. Superseded for minting by [ADR 0003](0003-sequence-short-codes.md). `ShortUrl` rows are inserted with a fresh code; the target is not unique.
7. `ClickLoggedEvent` gains `eventId` (random UUID, set when `links` publishes). Consumers skip ids already processed, in the same transaction as their write:
   - `analytics`: `processed_click_event(event_id PK)`; `incrementClicks` checks the id, inserts it, then upserts.
   - `clicks`: unique `click.event_id`; `RecordClickService` checks the id, then saves.
   - Copies racing past the check fail on the key; their whole transaction (increment or log row) rolls back.
8. `processed_click_event` is pruned: `ProcessedClickEventPruner` (`analytics.adapters.scheduling`, `@Scheduled`) calls the `PruneProcessedClickEvents` port, which runs a JPQL `delete … where processedAt < now - retention`. Settings: `urlshortener.analytics.processed-click-retention` (default `7d`) and `…processed-click-prune-cron` (default 03:00 daily). The job runs on every replica; the delete is idempotent.
9. **Exception to the `analytics` ownership rule** ("one `LinkStats` row per hash; no raw click rows"): `processed_click_event` holds only event ids and timestamps, for deduplication. It is not a click log (no hash). The only read is `incrementClicks`'s `existsById` check; no API returns the rows. It is kept only for the retention window. `clicks` stays the only permanent click log.

## Consequences

- No lost or duplicated increments; created/click order no longer matters; one connection and one transaction per event.
- A racing duplicate fails its listener call; that publication stays incomplete, and a resubmit is skipped by the id check.
- `processed_click_event` holds one row per click within the retention window. The retention must be longer than an incomplete publication can wait before it is resubmitted; a copy resubmitted after its id was pruned is counted again. `clicks` does not prune, so its unique `event_id` guards forever.
- `click.event_id` is a nullable column (`UUID?`). New writes always set it. Nulls are only expected on rows logged before this change (`ddl-auto: update`).
- Depends on Hibernate HQL `on conflict` (6.5+), not portable JPQL.
- The no-op updates still write (and lock) an existing row.
- Short-code minting moved to [ADR 0003](0003-sequence-short-codes.md). `redirect` still loads the code it was given.

## Alternatives considered

| Option | Why rejected |
| --- | --- |
| Keep `@Transactional` read-then-write | Loses concurrent increments |
| Native upsert (`MERGE` / `ON CONFLICT`) | Team rule: no native queries; SQL differs per database |
| JPQL `UPDATE`, then insert on miss in a `REQUIRES_NEW` transaction | Deadlocked on HSQLDB (table lock held by the outer transaction); starved the pool (two connections per event) under `LinkStatsConcurrencyTests` |
| Optimistic locking (`@Version`) + retry | Retry storms on hot hashes; still needs insert-race handling |
| Pessimistic lock (`SELECT … FOR UPDATE`) | Cannot lock a row that does not exist yet |
| Mark Modulith completion inside the listener transaction | Needs a plain `@TransactionalEventListener` and a global transaction-interceptor order ahead of Modulith's; relies on internals |
| Accept at-least-once double counting | Counts and click log drift on every resubmit |
| Keep processed ids forever | Unbounded table; breaks the "one row per hash" shape of `analytics` for no benefit after the resubmit window |
| Ask `clicks` whether the id was logged | Cross-module call; `analytics` must only consume events |

## Evidence (defence)

- [x] `LinkStatsConcurrencyTests`: 16 concurrent first clicks → 16; created event racing 16 clicks → 16; same event 3× → 1; 16 concurrent copies of one event → 1
- [x] `RecordClickIdempotencyTests`: same event 3× → one `click` row; 3 distinct events → 3 rows
- [x] `CreateShortUrlConcurrencyTests`: covered minting collisions until [ADR 0003](0003-sequence-short-codes.md); that ADR replaces this case
- [x] `ProcessedClickEventPruneTests`: ids younger than the retention are kept; older ids are removed
- [x] Same cases on Postgres 16 (Testcontainers): `*PostgresTests` rerun the cases above and are skipped when Docker is unavailable. They do not capture SQL. Hibernate renders the hash upserts as `insert … on conflict(hash) do update …`. The processed-id write is a plain `insert`, not `on conflict`.
- [x] Completion ordering read from `spring-modulith-events-core` 2.0.7 `CompletionRegisteringAdvisor`
- [x] `./gradlew check` green (ktlint + detekt + test + JaCoCo)

# ADR 0003: Sequence-backed short codes

- **Status:** Accepted
- **Date:** 2026-09-25
- **Deciders:** Team / feature card [`0001`](../features/0001-create-short-url.md)

## Context

MurmurHash of the target yields one code per URL (or three salted candidates). A repeat of the same URL must be allowed to mint another short URL, including millions of them. An in-memory counter resets on restart and is not shared by `app1` and `app2`.

## Decision

1. `short_url_seq` is the only id source. `JpaShortCodeSource` creates it when missing, via the Hibernate dialect (`start with 238328`, `increment by 1`), then reads `nextval` / `next value for`.
2. `238328` is `62^3`, so `Base62.encode` starts at `1000` (4 characters) and grows only when the id no longer fits.
3. Each `create` inserts a new `short_url` row (`hash` = that code, `target` not unique) and publishes `ShortUrlCreatedEvent`. Redirect stays a lookup by `hash`.
4. Guava (Murmur only) is removed.

## Consequences

- The same URL can have as many short URLs as inserts. Codes are not a function of the target.
- Restarting an app does not rewind the sequence. Both replicas share Postgres `pgdata`.
- HSQLDB (default profile and unit tests) uses the same dialect helpers. The `docker` profile uses Postgres.
- Dropping `pgdata`, or `ddl-auto: create`, starts the sequence over and can repeat codes.
- A rolled-back insert still consumes a sequence value (a gap, not a repeat).

## Alternatives considered

| Option | Why rejected |
| --- | --- |
| Hash the URL | One code per target, plus a collision ceiling |
| In-memory counter, even with a random start or a hash-sized step | Repeats after restart and across `app1` / `app2` |
| Fixed-width permute | Every code is longer than the id requires |

## Evidence (defence)

- [x] `Base62Tests`: `238328` encodes to `1000`
- [x] `CreateShortUrlConcurrencyTests`: two creates of one URL differ; 16 concurrent creates are 16 codes, each at least 4 characters
- [x] Same class on Postgres 16 (`CreateShortUrlConcurrencyPostgresTests`), skipped without Docker

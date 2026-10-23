# ADR 0006: URL-expiration

- **Status:** Proposed
- **Date:** 2026-10-23
- **Deciders:** Team / feature card [`0006`](../features/0006-URL-expiration.md)

## Context

The URL Expiration feature requires URLs to become inaccessible after an optional expiration date and time. The application will check expiration when a short URL is accessed and return HTTP 410 Gone if it is expired.

## Decision

I will implement URL Expiration inside the existing links module and store an optional expiration timestamp in the current data structure.
I have some tests to ensure the program works without specifying an expiration date, with specifying an expiration date in the future and one that checks for the Code 410 with a given expiration date in the past.

## Consequences

Positive: Expired URLs are rejected automatically when accessed, and the implementation stays simple by keeping the logic in the existing links module.

Negative: Expired records remain in the database because no background cleanup is implemented yet.


## Alternatives considered

| Option | Why rejected |
| Mandatory expiration parameter | Less flexibility and would interfere with current functionality |


## Evidence (defence)

./gradlew test

docker compose up --build

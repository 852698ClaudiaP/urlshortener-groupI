# ADR 0004: Live Click Stream Proof of Concept via Server-Sent Events

- **Status:** Accepted
- **Date:** 2026-10-23
- **Deciders:** Team / feature card [`0007-Live-Click-Stream-(SSE-WebSocket).md`](../features/0007-Live-Click-Stream-(SSE-WebSocket).md)

## Context

Users want to observe link click events in real time. Standard polling (`GET /api/stats/{hash}`) generates redundant HTTP traffic and database queries.
Feature card `0007` (Weight 21, Level 4) specifies a real-time click stream using SSE or WebSockets.
For the **23 October Proof of Concept (PoC)** milestone, the requirement is to wire modules, introduce a **new domain event** published by one module and consumed by another, and verify the delivery with an automated test. Full external broker fan-out across multiple replicas will be completed for the Working Prototype (27 November).

## Decision

1. **Modules involved:**
   - `clicks`: Publishes the new domain event once a click is validated and saved.
   - `streaming` (`es.unizar.urlshortener.streaming`): A new Spring Modulith module that consumes click events and manages real-time client subscriptions.

2. **New Event:**
   - `es.unizar.urlshortener.clicks.LiveClickEvent(hash, occurredAt, eventId)`
   - Published by `RecordClickService` in `clicks` when a new click is recorded (after verifying idempotency).
   - Replaces relying only on seed events (`ClickLoggedEvent` / `ShortUrlCreatedEvent`), fulfilling the Level 3/4 event-coupling criteria.

3. **Hexagonal Architecture in `streaming`:**
   - **Application layer (`streaming.application`):**
     - Ports: `RegisterLiveClickListener` and `BroadcastLiveClick`.
     - Service: `LiveClickStreamService` maintaining thread-safe subscriber callbacks (`CopyOnWriteArrayList` and `ConcurrentHashMap`).
   - **Primary Web Adapter (`streaming.adapters.web`):**
     - `StreamingController`: Exposes `GET /api/streaming/clicks` (all clicks) and `GET /api/streaming/clicks/{hash}` (per-hash filter) producing `text/event-stream` using Spring MVC's `SseEmitter`.
     - Automatically cleans up disconnected or timed-out clients.
   - **Primary Event Adapter (`streaming.adapters.events`):**
     - `LiveClickListener`: Annotated with `@ApplicationModuleListener` to asynchronously consume `LiveClickEvent` from `clicks` and call `BroadcastLiveClick`.

4. **Libraries added:**
   - None for the PoC baseline: uses Spring Web MVC's built-in `SseEmitter` and Spring Modulith's event registry. (RabbitMQ broker integration will be added for multi-replica fan-out in the prototype).

## Consequences

- Real-time push capability without polling.
- Spring Modulith module boundaries are strictly preserved; `streaming` depends only on `clicks`'s public event API, with zero cyclic dependencies.
- jMolecules hexagonal stereotypes (`@Application`, `@PrimaryPort`, `@PrimaryAdapter`) verified by `ModularityTests` and `HexagonalArchitectureTests`.
- SseEmitters run within the single JVM during the PoC; multi-replica fan-out will hook into this same `BroadcastLiveClick` port via RabbitMQ for the Level 4 deliverable.

## Alternatives considered

| Option | Why rejected |
| --- | --- |
| Polling `GET /api/stats/{hash}` | High latency, wasteful CPU/network overhead on database |
| WebSocket (`ws://`) | Requires bidirectional framing and connection upgrade complexity; SSE (`text/event-stream`) is standard unidirectional HTTP, ideal for click streams |
| Ingest directly from `links` | Bypasses `clicks` module ownership of click persistence, idempotency, and deduplication |

## Evidence (defence)

- [x] Code in `es.unizar.urlshortener.streaming` and `es.unizar.urlshortener.clicks.LiveClickEvent`
- [x] Test command: `./gradlew test --tests "es.unizar.urlshortener.streaming.LiveClickStreamTests"`
- [x] Full quality gates command: `./gradlew check`
- [x] Modulith diagram in `build/spring-modulith-docs/components.puml` showing `Streaming` listening to `Clicks`

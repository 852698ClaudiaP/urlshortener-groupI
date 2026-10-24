# Feature: Live Click Stream (SSE / WebSocket)
## Decision
**Owner module**: new streaming + ADR | **Event-coupled**: Y

A client connected to replica A receives click events originating from replica B in real time. Requires fan-out via an external broker — in-process events are invisible across JVMs.

**Traps**: Spring's SseEmitter is per-JVM; without a broker, a client on replica A never sees events from replica B; backpressure handling when clients disconnect.

**Technology hints**: SseEmitter or Spring WebFlux Flux<ServerSentEvent>; broker consumer -> push to all connected emitters; --profile broker.

---

* Weight: 21
* Event-coupled: Y

---

## Later (after the 2 October agreement)

- **ADR:** [docs/adr/0005-live-click-stream-poc.md](../adr/0005-live-click-stream-poc.md)

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

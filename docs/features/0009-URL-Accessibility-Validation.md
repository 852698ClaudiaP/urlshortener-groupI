# Feature: URL Accessibility Validation

## Decision (required by 2 October)

**Owner module**: links | **Event-coupled**:  N (synchronous HTTP check before storing — no
event consumed)

Validates that a target URL returns an acceptable HTTP status code before storing the hash.
Errors use RFC 9457 ProblemDetail [3].

**Traps**: servers that reject HEAD (fall back to GET); treating redirects as success; per-replica
timeout configuration drifting; making synchronous validation a redirect-path bottleneck (Level 2+
should consider async pre-check with a retry / state machine).

**Technology hints**: RestTemplate or WebClient; @ControllerAdvice + Spring Boot
ProblemDetail; Bean Validation @URL; circuit breaker [13] for Level 2+.

---

- **Weight:** 8
- **Event-coupled:** N

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

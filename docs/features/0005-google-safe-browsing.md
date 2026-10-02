# Feature: Google safe browsing

## Decision (required by 2 October)

**Owner module**: links | **Event-coupled**: N (synchronous API call before storing — no cross-module event required)

Integrates with Google Safe Browsing API v4 to block malicious URLs at creation time. Flagged URLs are rejected with 400 + RFC 9457 Problem Detail; safe results are cached to respect API rate limits.

**Traps**: the external call adds latency to POST /api/link — cache aggressively; unavailability must not block URL creation; the fail-open vs fail-closed policy must be documented in an ADR.

**Technology hints**: Google API Client Library; Spring @Cacheable with TTL; RestTemplate or
WebClient; test with Google’s malware test URL.

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

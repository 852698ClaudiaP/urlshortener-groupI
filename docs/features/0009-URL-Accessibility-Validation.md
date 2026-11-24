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

- **ADR:** [docs/adr/0006-url-accessibility-validation.md](../adr/0006-url-accessibility-validation.md)

### Acceptance criteria

- [x] **AC1: Accessible URL (2xx / 3xx)**: When issuing POST /api/link with a target URL that responds with an HTTP status code in the 2xx or 3xx range, the short link is created successfully returning HTTP 201 Created with the expected JSON payload and Location header.
- [x] **AC2: Unreachable / dead URL (4xx / 5xx)**: If the target URL returns a client or server error code (4xx or 5xx), link creation is rejected with HTTP 400 Bad Request and an RFC 9457 ProblemDetail body stating that the target URL is not reachable.
- [x] **AC3: Non-existent host or connection timeout**: If the domain cannot be resolved via DNS or the connection exceeds the configured timeout (2 seconds), the request is rejected with HTTP 400 Bad Request and an RFC 9457 ProblemDetail body.
- [x] **AC4: HEAD to GET fallback**: If the remote server rejects the HEAD method with 405 Method Not Allowed, the validator automatically performs a fallback GET request to verify reachability.
- [x] **AC5: Invalid URL syntax**: If the provided URL is malformed or lacks an http/https scheme, it is immediately rejected with HTTP 400 Bad Request and an RFC 9457 ProblemDetail body before any network request is issued.

### Scale evidence

- **Bounded latency:** Connect timeout of 2000 ms and request timeout of 3000 ms prevent remote servers from hanging application threads.                                             
- **Minimal resource overhead:** Lightweight `HEAD` requests avoid downloading response bodies (`HttpResponse.BodyHandlers.discarding()`).                                           
- **Resilient fallback:** Automatic fallback to `GET` on HTTP 405/501/403.                   
- **Test domain isolation:** RFC 2606 reserved domains (`example.com`, `localhost`) are fast-tracked without network calls to protect test and benchmark stability. 

### Qualities

Fill **Assessed** with the grade you claim; **How to test** must falsify that number if it failed.

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | 5 | Acceptance tests for this feature |
| **Scalability** | 3.5 | Scale evidence above. Not `ModularityTests`. |
| **Engineering** | 1.5 | Hard gates: AI disclosure + `./gradlew check`. Then ADR + `ModularityTests`: both → **1.5**, one → **0.75**. |

**Indicative total:** _ / 10

### AI disclosure

- **Tools / skills:** Antigravity (Gemini).                                                                                                                
- **Used for:** Brainstorming edge cases for acceptance criteria, scaffolding `HttpUrlAccessibilityValidator.kt` with Java 25 HttpClient, drafting ADR 0006, and configuring test mocks.                                                                                                                          
- **Human-reviewed:** Code, architecture, Detekt/ktlint compliance, and test suite reviewed and executed locally by Alejandro Vives Ruiz.

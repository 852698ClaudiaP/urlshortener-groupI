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

- [ ] **AC1: URL accesible (2xx / 3xx)**: Al enviar POST /api/link con una URL que responde con código HTTP 2xx o 3xx, el enlace se crea correctamente devolviendo HTTP 201 Created con el cuerpo JSON esperado y la cabecera Location.
- [ ] **AC2: URL inaccesible (4xx / 5xx)**: Si la URL devuelve un código de error de cliente o servidor (4xx o 5xx), la creación es rechazada con HTTP 400 Bad Request y un cuerpo RFC 9457 ProblemDetail describiendo que la URL no es alcanzable.
- [ ] **AC3: Host inexistente o timeout de conexión**: Si el dominio no resuelve por DNS o la conexión excede el tiempo de espera configurado (timeout de 2 s), se rechaza con HTTP 400 Bad Request y cuerpo ProblemDetail.
- [ ] **AC4: Fallback de HEAD a GET**: Si el servidor remoto rechaza el método HEAD con 405 Method Not Allowed, el validador ejecuta un fallback automático realizando una petición GET para verificar la accesibilidad.
- [ ] **AC5: Formato sintáctico inválido**: Si la URL no es válida sintácticamente o carece de esquema http/https, se rechaza con HTTP 400 Bad Request y ProblemDetail antes de emitir cualquier petición de red.

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

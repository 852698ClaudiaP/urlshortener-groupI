# ADR NNNN: Title

> **Not part of the 2 October agreement.** ADRs are written when a choice later locks a new module, the external broker, or a specialised library — not at agreement time. The feature card says `none` for ADR until that decision is made.

- **Status:** Proposed | Accepted | Superseded by ADR-XXXX | Deprecated
- **Date:** YYYY-MM-DD
- **Deciders:** Team … / feature card …

## Context

What problem or force requires a choice? (Load, module boundary, tech constraint, feature card, scale evidence, …)

## Decision

What we will do. Be concrete (packages, events, libraries, Compose services, test strategy).

## Consequences

Positive and negative. What becomes easier/harder? What must stay true in CI (`./gradlew test`, Compose, k6)?

## Alternatives considered

| Option | Why rejected |
| --- | --- |
| … | … |
| … | … |

## Evidence (defence)

- [ ] Code / PR link
- [ ] Test or command that proves the decision (`./gradlew test`, k6, `docker compose …`)
- [ ] Diagram or Modulith doc snippet (`build/spring-modulith-docs/`) if structure changed

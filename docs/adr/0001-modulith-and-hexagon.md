# ADR 0001: Modulith modules with jMolecules hexagon

- **Status:** Accepted (seed)
- **Date:** 2026-09-07
- **Deciders:** Course seed (IW 2627)

## Context

The product is a modular monolith graded as **Boot jar(s) + Compose**. We need Modulith module boundaries and an enforceable ports/adapters shape inside each module, aligned with Spring Modulith’s documented verification path.

## Decision

1. Use **Spring Modulith** application modules: `links`, `clicks`, `analytics` (closed; events in base packages).
2. Inside each module, keep **domain / application / adapters** packages.
3. Mark hexagon roles with **jMolecules** stereotypes (`@Application`, `@PrimaryPort` / `@SecondaryPort`, `@PrimaryAdapter` / `@SecondaryAdapter`).
4. Enforce with `ApplicationModules.verify(VerificationOptions…​.withAdditionalVerifications(ensureHexagonal(SEMI_STRICT)))` plus light ArchUnit guards (domain framework-free; application ↛ adapters).
5. Driving adapters depend on **primary ports**; driven adapters implement **secondary ports**.

## Consequences

- Defence can cite Modulith + jMolecules docs and failing `./gradlew test`.
- `SEMI_STRICT` allows secondary adapters to use application/domain types on port signatures (practical for JPA mappers). Stricter `STRICT` remains an optional grown ADR.
- Students must ADR any move to jMolecules-off, `STRICT`, or nested `@ApplicationModule` layers.

## Alternatives considered

| Option | Why not (for the seed) |
| --- | --- |
| ArchUnit layers only | Works, but not the Modulith-documented hexagon path |
| `ensureHexagonal(STRICT)` | Requires DTOs on every port edge; heavier for weight-0 seed |
| Global hexagon like `urlshortener-main` | Conflicts with Modulith-as-primary teaching goal |
| Flat Modulith packages | Weaker ports/adapters teaching signal |

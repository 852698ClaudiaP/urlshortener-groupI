# Architecture Decision Records (ADRs)

Grown work must **record decisions**, not only ship code. Put ADRs here; cite them in the defense packet.

## Rules

1. One decision per file: `NNNN-short-title.md` (monotonic numbers).
2. Write the ADR **before** (or in the same PR as) the code that locks it in.
3. Every ADR must state **alternatives considered** and **why they were rejected**.
4. Mark hexagon roles with jMolecules; keep `ensureHexagonal` green in `ModularityTests`.
5. Link evidence: tests (`ModularityTests`, `HexagonalArchitectureTests`, k6), Modulith docs under `build/spring-modulith-docs/`, Compose/LB demos.
6. Seed file [`0001-modulith-and-hexagon.md`](0001-modulith-and-hexagon.md) is provided (weight 0). Copy [`0000-TEMPLATE.md`](0000-TEMPLATE.md) for team decisions.
7. Feature cards live under [`../features/`](../features/README.md) — not every feature needs an ADR; ADR when you lock architecture/events/tech (table below).

## Minimum grown set (defence)

Fill at least one ADR when you change any of:

| Topic | Example decision |
| --- | --- |
| Module boundaries | New Modulith module vs extend `links` / `clicks` / `analytics` |
| Hexagon enforcement | Keep / change jMolecules `SEMI_STRICT` (or adopt `STRICT`) |
| Cross-module coupling | Events only vs named interface / allowed dependency |
| Scalability level | In-process events vs broker (Level 4) |
| Persistence / tech | New store, cache, or specialised library |

Empty “we followed the seed” is not enough when you grow features or change structure.

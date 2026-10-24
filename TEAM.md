# TEAM.md — local only, do not commit

This file is listed in `.gitignore`. Copy it to `TEAM.md`, fill in the real values, and keep it on your machine only. It must never be pushed to the repository.

It is the only place personal information about the team is recorded.

---

## Members

| Personal name | UNIZAR email (`…@unizar.es`) | git `user.name` | GitHub id | Owned features | Integrator |
| --- | --- | --- | --- | --- | --- |
| Pavón Calcerrada, Claudia | 852698@unizar.es | Claudia Pavon | @852698ClaudiaP | 0004-CSV-upload.md | **yes** |
| Hanisch, Niklas | 1005479@unizar.es | TurboV0lt | @191168781 | 0005-URL-expiration, 0006-Google-Safe-Browsing | no |
| Di Candia , Angelo Lorenzo | 991792@unizar.es | Angelo Lorenzo Di Candia | @AngeLorenzo04 | docs/features/0007-Live-Click-Stream-(SSE-WebSocket).md | no |
| Vives Ruiz, Alejandro | 849504@unizar.es | Alejandro Vives Ruiz | @Vaivss | 0008-URL-Analytics.md, 0009-URL-Accessibility-Validation.md | no |

---

## Notes

- **UNIZAR email** (`…@unizar.es`) must be set as `user.email` in git for commits in this repo:
  ```bash
  git config user.email xxxxxx@unizar.es
  git config user.name "Surname Given"
  ```
- Exactly **one** member is the integrator (responsible for CI, `docker compose up`, and the cross-module event path at defence).
- **Owned features** are the cards each member owns.

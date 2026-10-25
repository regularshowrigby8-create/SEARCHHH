# Hackathon compliance & demo script

This document maps the build to the FNB App Academy style of judging: a working
prototype, mostly imported code, a coherent theme, and evidence that it runs.

## 1. Rule-by-rule

### "The app must be functional"

Every screen is wired to real data and real state:

| Screen | Data source | Failure path |
| --- | --- | --- |
| Home | `GET /api/v1/search` (empty query = discovery) + `/categories` + `/health` | cached page, typed error state, retry |
| Results | `GET /api/v1/search?q&category&sort&limit&offset` | last-good cache with an "Offline" banner |
| Detail | `GET /api/v1/search/{id}` + `POST /api/v1/insights` | heuristic insight, retry button |
| Saved | Room | never fails (local) |
| Settings | DataStore + `/health` | shows the exact error enum |

No TODO screens, no placeholder buttons, no lorem ipsum.

### "At least 90% of the code should be imported"

`STACK.md` lists **121 verified third-party tools**. Our own surface area:

| Our code | Lines |
| --- | --- |
| Kotlin (app source) | ~4.1k |
| Python (backend) | ~1.7k |
| Tests + tooling | ~0.8k |

Everything else - UI toolkit, layout, navigation, state, persistence, HTTP,
serialization, crash reporting, notifications, build system - is upstream code.
`scripts/scan_stack.py` proves each dependency is real; CI fails otherwise.

### "Use a modern, native Android stack"

Kotlin 2.0.21 · Jetpack Compose + Material 3 · Navigation Compose · ViewModel +
StateFlow · Room · DataStore · WorkManager · Retrofit + OkHttp +
kotlinx.serialization · Coil · Firebase (FCM/Crashlytics/Analytics/Perf) ·
Sentry · Gradle version catalog + KSP.

### "Cloud / backend"

FastAPI service with a hybrid ranking engine, optional Supabase persistence,
JWT auth, rate limiting, Prometheus metrics, structured logs, OpenAPI docs.

### "Security & POPIA"

* Telemetry is opt-out, defaults to **no PII** (`send_default_pii = False`,
  Crashlytics collection toggled by a user setting).
* Release builds forbid cleartext traffic; tokens would live in
  `EncryptedSharedPreferences` (security-crypto is wired).
* `.env`, keystores, `google-services.json` and credentials are git-ignored.
* Rate limits: 60/min on search, 120/min global.
* Errors never leak stack traces to the client (RFC 7807 problem details only).

### "Testing / CI"

70 automated tests (47 backend, 22 Android JVM) plus a Compose instrumentation
smoke test. CI runs lint, tests, an 80% coverage gate and a dependency audit, and
publishes an installable APK artifact on every push.

## 2. Live demo script (4 minutes)

1. **Cold start** (10 s) - splash → Home. Point at the green "Live ·
   SEARCHHH API v0.1.0" badge: the app is talking to a real backend.
2. **Typo search** (30 s) - type `cofee`. Show that "Campus Coffee Run" still
   ranks first, and expand the chip row of server suggestions.
3. **Facets + sort** (30 s) - tap `food`, then sort by "Cheapest". Show the
   result count and the `ms` timing in the top bar.
4. **AI insight** (30 s) - open an entry. The card shows `heuristic` in
   fallback mode; add a Gemini key to `.env`, restart, and it becomes `gemini`.
5. **Offline** (45 s) - turn on airplane mode. Saved still opens instantly;
   Results shows the "Offline: showing your last results" banner instead of an
   error page. Turn the network back on and hit Retry.
6. **Telemetry** (20 s) - Settings → "Test error". Show the event in Sentry /
   Crashlytics (or explain that it is skipped without a DSN - by design).
7. **Push** (20 s) - send a test message from the Firebase console with
   `data.query = "housing"`; the notification opens the app on that search.
8. **Proof, not promises** (25 s) - open `STACK.md` and `gh run list`: 121
   verified tools, green CI, APK artifact.

## 3. Judge-facing talking points

* **"Why not just use a search SaaS?"** - Because the demo has to run with zero
  infrastructure. The BM25 + RapidFuzz hybrid is ~250 lines, sub-5 ms, and every
  weight is explainable. Swapping in Postgres `tsvector` later keeps the same
  ranking model.
* **"How do you handle flaky venue Wi-Fi?"** - Three layers: Room for saved
  data, a last-known-good response cache in the repository, and typed errors that
  render as a banner with retry instead of a crash.
* **"Is it production-ready?"** - Not claiming that. `docs/ARCHITECTURE.md` §7
  lists exactly what would change: real auth, index in Postgres, Paging3,
  baseline profiles, FCM-driven delta sync.
* **"Where did you write code?"** - The product logic: ranking, repository
  fallbacks, state machines, design system tokens, and the tooling that audits
  the dependency list.

## 4. Submission checklist

- [x] Installable APK (CI artifact + GitHub Release)
- [x] Backend runs with `make dev`, no credentials
- [x] README with one-command quick start
- [x] Architecture + setup + security docs
- [x] 70 automated tests, CI green
- [x] Dependency inventory generated from the environment
- [x] No secrets committed (`.gitignore` covers `.env`, keystores,
      `google-services.json`)
- [ ] Swap `backend/app/data/seed.json` for the real product catalogue
- [ ] Add a product name/logo pass (strings + `ic_launcher_foreground.xml`)

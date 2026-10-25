# SEARCHHH

**A search-first Android app + FastAPI backend, assembled almost entirely from
battle-tested open-source libraries.** Built to be demoed live, on a phone,
with bad Wi-Fi, with no cloud account.

<p>
<img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF">
<img alt="Compose" src="https://img.shields.io/badge/Compose-BOM%202024.10.01-4285F4">
<img alt="Python" src="https://img.shields.io/badge/Python-3.11-3776AB">
<img alt="tools" src="https://img.shields.io/badge/imported%20tools-121-success">
<img alt="tests" src="https://img.shields.io/badge/backend%20tests-47%20passing-success">
</p>

---

## What it does

| Screen | Behaviour |
| --- | --- |
| **Home** | Instant search field with debounced suggestions, category facets, "trending" discovery feed, live backend health badge |
| **Results** | BM25 + fuzzy ranked results, sort chips (relevance / rating / cheapest / priciest / newest), offset pagination, offline fallback to the last good response |
| **Detail** | Full entry, AI-generated "why this suits you" card (Gemini → OpenAI → deterministic fallback), bookmark, share sheet, deep link out, feedback dialog |
| **Saved** | Room-backed library that opens **with no network**, instant remove/clear |
| **Settings** | Dark mode, telemetry opt-out, notification toggle, editable API base URL, live health check, push token, test-error button, build metadata |

The domain catalogue (`backend/app/data/seed.json`) is a Bloemfontein student
services marketplace: 30 entries across 14 categories. Swap that file for your
own data - nothing else needs to change, because the ranking engine is
schema-driven and the client renders whatever the API returns.

---

## Architecture in one picture

```
┌──────────────────────────── Android (Kotlin, Compose M3) ────────────────────────────┐
│  MainActivity ─ SearchhhTheme ─ SearchhhNavGraph (Navigation Compose)                 │
│      │                                                                               │
│      ├── ui/home        HomeViewModel      ── StateFlow<HomeUiState>                  │
│      ├── ui/results     ResultsViewModel   ── paging + sort + offline flag           │
│      ├── ui/detail      DetailViewModel    ── insight + feedback                     │
│      ├── ui/saved       SavedViewModel     ── Room Flow                              │
│      └── ui/settings    SettingsViewModel  ── DataStore + health + telemetry toggles  │
│                                                                                      │
│  data/repository   SearchRepository (cache + fallbacks)   SavedRepository            │
│  data/remote       Retrofit + OkHttp + kotlinx.serialization (+ Sentry interceptor)  │
│  data/local        Room (saved, recents) · DataStore (settings) · WorkManager sync   │
│  telemetry         Timber · Sentry · Firebase Crashlytics/Analytics/FCM (guarded)    │
└───────────────────────────────────────┬──────────────────────────────────────────────┘
                                        │ HTTPS/JSON  /api/v1/*
┌───────────────────────────────────────▼──────────────────────────────────────────────┐
│                             FastAPI backend (Python 3.11)                            │
│  api/routes/search.py     BM25 + RapidFuzz hybrid ranking, facets, suggestions        │
│  api/routes/insights.py   Gemini → OpenAI → heuristic, TTL-cached                    │
│  api/routes/feedback.py   Supabase PostgREST or in-memory sink                        │
│  api/routes/auth.py       JWT (PyJWT HS256) demo tokens                              │
│  services/search.py       tokenise → expand synonyms → BM25 → fuzzy → phrase bonus    │
│  services/repository.py   Supabase ↔ in-memory (same protocol, one-line swap)         │
│  core/*                   structlog JSON logs, RFC-7807 errors, slowapi limits,       │
│                           Sentry, Prometheus /metrics, security headers              │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

Full detail: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## Quick start (2 minutes, no accounts needed)

```bash
# 1. Backend
make install          # creates .venv and installs everything
make dev              # http://localhost:8000  (docs at /docs)

# 2. Android
cd android
./gradlew :app:assembleDebug     # requires JDK 17 + Android SDK 35
adb install app/build/outputs/apk/debug/app-debug.apk
```

The app ships pointed at `http://10.0.2.2:8000/` (host loopback as seen from
the emulator). On a physical phone, open **Settings → Change URL** and enter
your laptop's LAN address, e.g. `http://192.168.0.10:8000/`.

Nothing else is required: no Supabase project, no Firebase file, no Sentry DSN,
no API keys. Each integration activates itself when its credentials appear in
`.env` / `local.properties` (see [`.env.example`](.env.example)).

---

## The 121 imported tools

This project is deliberately built from other people's code. Every dependency is
listed in [`stack/dependencies.yml`](stack/dependencies.yml), and a script
**proves** each one exists instead of trusting a README:

```bash
make stack     # regenerates STACK.md from the live environment
```

`scripts/scan_stack.py` resolves pip packages through `importlib.metadata`,
matches Gradle coordinates against `android/gradle/libs.versions.toml`,
and greps the workflows for every GitHub Action. CI runs it in `--check` mode and
fails if any entry disappears.

Current inventory: **121 tools** across 13 categories - Compose (12), Android
platform (6), lifecycle/navigation (6), persistence (5), network/serialization
(11), Firebase (5), observability (2), testing (4), build toolchain (9),
Python backend (26), Python tooling (12), transitive runtime (15), CI (8).

Generated, always-current table: [`STACK.md`](STACK.md).

---

## Verification

```bash
make test      # 47 backend tests
make cov       # coverage gate at 80% (currently 85%)
make lint      # ruff
make stack     # dependency audit
cd android && ./gradlew :app:testDebugUnitTest   # 22 JVM tests (fakes, MockWebServer)
```

* Backend: FastAPI `TestClient` covers every route, error shape, rate limit and
  security header.
* Android: 22 JVM tests with hand-written fakes (no mocking framework),
  `StandardTestDispatcher`, and MockWebServer contract tests that decode the
  *real* backend payloads.
* CI: [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs both suites,
  lints, and uploads an installable debug APK on every push.

---

## Hackathon compliance (FNB App Academy rules)

| Requirement | How this repo answers it |
| --- | --- |
| Functional prototype, not slides | Installable APK + running API, every screen wired to real data |
| ≥ 90% imported code | 121 verified third-party tools; ~4k lines of our own Kotlin on top of AndroidX/Compose/Firebase |
| Works for judges on first run | Zero-credential demo mode (in-memory catalogue, heuristic insights) |
| Android-native, modern stack | Kotlin 2.0, Compose M3, Navigation, ViewModel + StateFlow, Room, DataStore, WorkManager |
| Cloud + push + crash reporting | Supabase, FCM, Crashlytics, Sentry - all optional, all wired |
| Security & privacy | JWT, rate limiting, POPIA-aware telemetry (no default PII), encrypted prefs, HTTPS-only release config |
| Tests & CI | 70 automated tests (47 backend, 22 Android JVM, 1 instrumented), lint, coverage gate, APK artifact per push |

Demo script, judging talking points and the full checklist: [`docs/HACKATHON.md`](docs/HACKATHON.md).

---

## Repository layout

```
backend/            FastAPI service (app/, tests/, requirements*.txt)
android/            Gradle project (app/src/main/kotlin/com/searchhh/app/...)
stack/              dependency manifest + manifest generator
scripts/            scan_stack.py (verification + STACK.md renderer)
docs/               architecture, setup, security, release, hackathon notes
.github/workflows/  ci.yml · release.yml · codeql.yml · instrumented.yml
Makefile            install · dev · test · cov · lint · stack · apk
```

## Docs

* [Setup](docs/SETUP.md) - prerequisites, emulator vs device, LAN debugging
* [Architecture](docs/ARCHITECTURE.md) - layers, data flow, ranking maths, decisions
* [Hackathon checklist](docs/HACKATHON.md) - rule-by-rule mapping + demo script
* [Security](docs/SECURITY.md) - secrets, cleartext dev config, privacy stance
* [Release](docs/RELEASE.md) - signing, versioning, publishing an APK
* [STACK](STACK.md) - every imported tool, verified

## Licence

MIT for the code in this repository. Third-party components keep their own
licences; run `make licenses` (pip-licenses) for the authoritative Python report.

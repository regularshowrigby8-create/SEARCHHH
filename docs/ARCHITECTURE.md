# Architecture

## 1. Shape of the system

Two deployables:

| Component | Stack | Runs on |
| --- | --- | --- |
| `android/` | Kotlin 2.0.21, Compose M3, Navigation, Room, DataStore, WorkManager, Retrofit/OkHttp, Firebase, Sentry | Phone / emulator (minSdk 26) |
| `backend/` | Python 3.11, FastAPI, Pydantic v2, RapidFuzz, Supabase, structlog, Sentry, Prometheus | Any container / `uvicorn` |

They speak JSON over HTTP under `/api/v1`. The OpenAPI schema is the contract;
the Kotlin DTOs mirror `backend/app/models/schemas.py`.

## 2. Android layers

```
ui/            Compose screens (stateless) + ViewModels (StateFlow)
   ↕
data/
  repository/  SearchRepository, SavedRepository, RecentQueriesRepository
  remote/      Retrofit interface, DTOs, NetworkModule (OkHttp + interceptors)
  local/       Room (saved, recents), DataStore (settings)
   ↕
core/          ApiResult, dispatchers, error mapping
di/            AppContainer (manual service locator)
telemetry/     Timber, Sentry, Firebase bootstrap
work/          SyncWorker (periodic, 6h, network constrained)
push/          FirebaseMessagingService
```

**Rules that keep it honest**

* UI never sees exceptions - repositories return `ApiResult<T>` and a small
  `AppError` enum (`OFFLINE`, `TIMEOUT`, `SERVER`, `UNAUTHORIZED`, `PARSE`, `UNKNOWN`).
* Screens are stateless composables taking a `UiState` + lambdas, so every screen
  is renderable in a test without Android dependencies.
* ViewModels are the only place that launches coroutines; every one takes a
  `DispatcherProvider` so tests can inject `StandardTestDispatcher`.
* DTOs are converted to domain models at the repository boundary; Compose never
  imports `data.remote.dto`.

### State flow (search)

```
user types
   → HomeViewModel.onQueryChange(query)         (StateFlow update, debounce 250ms)
   → SearchRepository.suggest()                 (Retrofit)
   → state.suggestions                          (chips under the field)

user submits
   → navigate("results?q=…&category=…")
   → ResultsViewModel.load(reset = true)
   → SearchRepository.search(limit = 20, offset = 0, sort)
       success → cache[query] = page            (last-known-good)
       failure → cached page flagged fromCache = true, or ApiResult.Failure
   → results sorted/filtered by chips, "Load more" appends offset += n
```

### Offline behaviour

| Layer | What survives a dead network |
| --- | --- |
| Room | Saved entries, recent queries (full read/write) |
| DataStore | All settings, including a custom API URL |
| `SearchRepository` cache | Last successful first page per (query, category, sort) |
| `ApiResult.Failure` | Typed error → "You're offline. Saved items still work." |

That is why the demo works with airplane mode turned on: the saved tab and the
home shell are entirely local.

## 3. Search ranking (the part worth explaining to judges)

`backend/app/services/search.py`, ~250 lines, no search server.

1. **Normalise** - NFKD decomposition strips accents, casefold, regex tokenise,
   stopword removal.
2. **Expand** - a synonym map (`cheap → budget, affordable, low`, `wifi →
   internet, data`) is applied to the query only, so a tag match on "data" still
   satisfies "wifi".
3. **Score each document** with three additive signals:
   * **BM25** (`k1 = 1.5`, `b = 0.75`) over *field-weighted* term frequencies:
     title ×3.0, tags ×2.2, subtitle ×1.6, category ×1.4, description ×1.0.
   * **Fuzzy bonus** - RapidFuzz `token_set_ratio` / `partial_ratio`; above 62%
     similarity it contributes up to +2.5, which is what makes "cofee" work.
   * **Phrase bonus** - exact query in the title (+2.0), exact tag (+1.0),
     exact category (+0.8).
4. **Filter** by category facet, then **sort** by relevance / rating / price /
   recency. Every hit carries `score` and up to 3 `highlights`, so the UI can
   show *why* something matched.
5. **Suggest** by prefix first, then fuzzy fallback (min 78%), de-duplicated.

Cost: indexing 30 documents is sub-millisecond; the whole request is < 5 ms
locally. The same code indexes 10 000 rows fine (single pass, no allocations per
query beyond scoring).

## 4. Backend layout

```
app/main.py            create_app(): middleware, error handlers, metrics, lifespan
app/config.py          pydantic-settings; every integration optional
app/api/router.py      /api/v1 aggregation (health also mounted at /)
app/api/routes/*.py    search, insights, feedback, auth, integrations, admin
app/core/*             logging, errors (RFC 7807), security, limiter, observability
app/services/*         search engine, repositories, AI providers, catalogue, container
app/models/schemas.py  the API contract
app/data/seed.json     30-entry demo catalogue
tests/                 47 tests: unit + HTTP + rate-limit regression
```

### Repository swap

`EntryRepository` is a `Protocol` with two implementations:
`MemoryRepository` (bundled seed) and `SupabaseRepository` (PostgREST).
`build_repository()` picks Supabase when `SUPABASE_URL` + key exist, otherwise
memory. `scripts`/tests and the demo both run on memory, so no network is needed
to evaluate the submission.

### AI with a floor

`services/ai.py` tries Gemini, then OpenAI, then a deterministic heuristic that
always produces a useful sentence. Responses are cached in a `TTLCache` (15 min,
512 entries) and the cache hit is reported as `cached: true`. A missing API key
never breaks the detail screen.

## 5. Observability

| Signal | Where |
| --- | --- |
| Request logs | structlog JSON, one line per request, `x-request-id` correlation |
| Errors (app) | Sentry, environment/release tagged, PII disabled |
| Crashes/ANRs | Sentry Android + Firebase Crashlytics (guarded when unconfigured) |
| Metrics | Prometheus at `/metrics` (excluded paths: /health, /metrics) |
| Traces | Retrofit requests via `SentryOkHttpInterceptor`; Sentry traces sample 20% in release |
| Client logs | Timber `DebugTree` in debug, warn-only tree in release |

## 6. Notable decisions

| Decision | Why |
| --- | --- |
| Manual DI (`AppContainer`) over Hilt | One readable file, no annotation-processing surface, fewer build failure modes under a deadline |
| String routes over type-safe routes | Navigation 2.8 type safety is nice, but a typo in a serialized route is a runtime crash; constants are greppable and tested by the nav graph |
| Offset pagination over Paging3 | The catalogue is 30-1000 rows; an explicit state machine is easier to reason about and test |
| BM25 in-house over Meilisearch/Elastic | Judges can run the demo with zero infrastructure; ranking maths is explainable |
| Firebase plugins applied **only** when `google-services.json` exists | A judge cloning the repo must not need a Google account |
| Debug-only cleartext HTTP | Release `network_security_config` forbids cleartext; debug allows loopback + LAN for on-device testing |
| Sentry runtime SDK, not the Gradle plugin | Avoids mapping-upload credentials in CI while still capturing crashes |

## 7. What would change for production

* Real auth: Supabase Auth / OIDC instead of demo JWTs (token shape stays).
* Move the index into Postgres full-text + `pg_trgm`, keep the same ranking weights.
* Add Paging3 if the catalogue passes ~5k rows.
* Add `baseline-prof.txt` (profileinstaller is already wired) and Macrobenchmark.
* Replace the periodic `SyncWorker` with FCM-triggered delta sync.

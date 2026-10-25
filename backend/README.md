# SEARCHHH API

FastAPI service that powers the Android client. Every integration is optional -
with no `.env` at all it runs in offline/demo mode (bundled catalogue, heuristic
insights, in-memory rate limiting, no Sentry).

```bash
pip install -r requirements-dev.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

* Swagger UI: <http://localhost:8000/docs>
* ReDoc: <http://localhost:8000/redoc>
* OpenAPI schema: <http://localhost:8000/openapi.json>

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/health`, `/ready` | Liveness (with repository check) / readiness (index built) |
| GET | `/api/v1/search` | Hybrid search: `q`, `category`, `limit`, `offset`, `sort` |
| POST | `/api/v1/search` | Same, JSON body (used by the app) |
| GET | `/api/v1/search/suggest` | Autocomplete by prefix, fuzzy fallback |
| GET | `/api/v1/search/categories` | Facet counts |
| GET | `/api/v1/search/{id}` | One entry (404 problem-details otherwise) |
| POST | `/api/v1/insights` | Gemini → OpenAI → heuristic, TTL-cached |
| POST | `/api/v1/feedback` | Supabase or in-memory sink |
| POST | `/api/v1/auth/token` | Demo JWT (HS256, 12 h) |
| GET | `/api/v1/auth/me` | Echo the caller identity |
| GET | `/api/v1/integrations` | Self-describing registry: what is configured vs fallback |
| GET/POST | `/api/v1/admin/{stats,refresh,warm,ping}` | Operations (guarded by `API_KEY`/JWT) |
| GET | `/metrics` | Prometheus |

```bash
curl -s "localhost:8000/api/v1/search?q=cofee&limit=2" | jq
curl -s -X POST localhost:8000/api/v1/insights \
  -H 'content-type: application/json' \
  -d '{"entry_id":"svc-019"}' | jq
```

## Configuration

Copy `.env.example` to `.env`. Highlights:

| Variable | Default | Meaning |
| --- | --- | --- |
| `ENV` | `local` | `local` / `ci` / `staging` / `production` |
| `JWT_SECRET` | insecure dev value | **set this anywhere real** |
| `API_KEY` | empty | when set, protects `/api/v1/admin/*` |
| `SUPABASE_URL` + key | empty | switches the repository from memory to PostgREST |
| `GEMINI_API_KEY` / `OPENAI_API_KEY` | empty | switch insights from heuristic to LLM |
| `SENTRY_DSN` | empty | enables error + performance reporting |
| `REDIS_URL` | empty | shared rate-limit storage instead of `memory://` |
| `RATE_LIMIT_SEARCH` | `60/minute` | per-IP search budget |

## Layout

```
app/
  main.py              application factory, middleware, lifespan
  config.py            pydantic-settings (all optional integrations)
  api/routes/*.py      search, insights, feedback, auth, integrations, admin, health
  core/                logging, errors, security, limiter, observability, middleware
  services/
    search.py          BM25 + RapidFuzz hybrid engine
    repository.py      Supabase / memory implementations of EntryRepository
    ai.py              provider chain with a deterministic fallback
    catalog.py         seed loader
    container.py       service container + index lifecycle
  models/schemas.py    the API contract (mirrored by the Kotlin DTOs)
  data/seed.json       30-entry demo catalogue
tests/                 47 tests (unit, HTTP, rate-limit regression)
```

## Tests

```bash
python -m pytest tests -q                       # 47 passed
python -m pytest tests -q --cov=app             # 85% coverage
python -m ruff check . && python -m ruff format --check .
```

The suite uses the in-memory repository and `TestClient`, so it is hermetic:
no network, no credentials, no database, ~2 seconds.

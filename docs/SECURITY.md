# Security & privacy

## Secrets

| Secret | Where it lives | Committed? |
| --- | --- | --- |
| `JWT_SECRET` | `backend/.env` | no (`.env*` ignored, `.env.example` is placeholders only) |
| `SUPABASE_SERVICE_ROLE_KEY` | `backend/.env` / CI secret | no |
| `SENTRY_DSN` | `backend/.env`, `-Psearchhh.sentryDsn` | no |
| `google-services.json` | `android/app/` | no (only `.example`) |
| Release keystore | `android/keystore.properties` + `.jks` | no |
| GitHub secrets | Actions → `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` | n/a |

`.gitignore` blocks `*.jks`, `*.keystore`, `keystore.properties`,
`google-services.json`, `service-account*.json`, `*.pem`, `.env*`.
`pre-commit` adds `detect-private-key` on top.

## Authentication

* `POST /api/v1/auth/token` issues a short-lived HS256 JWT (`JWT_TTL_MINUTES`,
  default 12 h) for demo purposes. Production should delegate to Supabase Auth or
  an OIDC provider; the token shape consumed by the client stays the same.
* `GET /api/v1/auth/me` requires a valid bearer token.
* Write/admin routes accept `x-api-key` **or** a valid JWT when `API_KEY` is set.
  With `API_KEY` empty the API is open - acceptable for a local demo, documented
  here so nobody ships it that way.
* `DEV_AUTH_ENABLED=false` disables token issuing entirely.

## Transport

* Release builds: `network_security_config.xml` sets
  `cleartextTrafficPermitted="false"` for everything.
* Debug builds only: cleartext allowed to `10.0.2.2`, `localhost`, `127.0.0.1`
  and `192.168.0.0` - enough for a laptop-hosted API, no wider.
* OkHttp has explicit connect/read/write timeouts (10/20/20 s) and does not
  follow redirects to arbitrary hosts beyond OkHttp defaults.

## Rate limiting

`slowapi`, keyed by client IP, storage `memory://` by default or Redis when
`REDIS_URL` is set:

| Scope | Default |
| --- | --- |
| Global default | 120/minute |
| `GET /api/v1/search` | 60/minute |

Responses carry `x-ratelimit-*` and `retry-after` headers; a 429 returns a
problem-details body, not a stack trace.

## Error handling

All failures return RFC 7807-style JSON:

```json
{
  "type": "https://searchhh.local/errors/not_found",
  "title": "not_found",
  "status": 404,
  "detail": "No entry with id 'x'",
  "instance": "/api/v1/search/x",
  "request_id": "77ddab2819f5457a",
  "details": {}
}
```

Internal exceptions are logged with a request id; the client only sees
"Something went wrong on our side."

## Privacy (POPIA alignment)

* Sentry: `send_default_pii = False`, no request bodies, no cookies.
* Crashlytics/Analytics: collection is toggled by the user setting
  `telemetry_enabled`; the default is on but easily switched off in Settings.
* No advertising SDKs, no device identifiers beyond Firebase's installation id
  (only when Firebase is configured).
* Feedback is free-text; the UI never asks for an ID number, phone number or
  address. The optional `contact` field is not surfaced in the app.
* Data at rest on device: Room (saved entries) + DataStore (settings); tokens
  would use `EncryptedSharedPreferences`.

## Hardening checklist already applied

- [x] `x-content-type-options: nosniff`
- [x] `x-frame-options: DENY`
- [x] `referrer-policy: no-referrer`
- [x] `permissions-policy` restricting camera/geolocation
- [x] CORS configurable, defaults to `*` for the demo (tighten via `CORS_ORIGINS`)
- [x] Ruff `S` (bandit) rules + `bandit` in CI tooling
- [x] `pip-audit` available as `make audit`
- [x] No `eval`, no dynamic SQL, all queries parameterised via PostgREST/ORM
- [x] Input validation on every endpoint (Pydantic constraints + length caps)

## Reporting

This is a hackathon prototype with no production users. If you find something,
open an issue on the repository instead of disclosing it publicly.

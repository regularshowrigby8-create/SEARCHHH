# Searchhh

An Android opportunity-discovery framework built **on existing open-source tools**.
Development build, not a production-certified crawler.

**0.2 implementation under validation:** included phone backend, name-only onboarding,
random per-installation identity/credentials, official MCP SSE tools, outbound relay,
and 128 catalog adapters. [Architecture and honest availability limits](docs/searchhh/INCLUDED-BACKEND.md).
The older APK linked below does **not** contain this upgrade.

## Architecture

- Android: imported **EinkBro v16.7.0** browser, Kotlin, Jetpack Compose and Android System WebView; Searchhh screens use Retrofit, Room and WorkManager.
- Included backend (0.2): Kotlin/Room/coroutines, Ktor + official MCP SDK, JSch remote bridge, public search connectors. No user deployment.
- Optional external stack: Python **FastAPI**, **SearXNG** (128 configured adapters), **Scrapy**, **PostgreSQL**, **Redis + RQ**.
- Future, not implemented: AI swarm/BYOK mode, Go crawler, OpenSearch.

Searchhh is not an AI agent in this release. Opportunity classification uses explicit
form/cohort/certification signals. It discovers public pages; it does not bypass
logins, CAPTCHAs, rate limits or private networks, and never submits forms.

## Android test APK

**[Download Searchhh 0.1.0 test APK](https://github.com/regularshowrigby8-create/SEARCHHH/releases/download/v0.1.0-test-6/searchhh-0.1.0-test.apk)** — 23.8 MB.

[Release and checksum](https://github.com/regularshowrigby8-create/SEARCHHH/releases/tag/v0.1.0-test-6)
· [Successful validation run](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36499566556)
· [Exact source](https://github.com/regularshowrigby8-create/SEARCHHH/tree/f9fc2e45022cbf1d57c80810738caf4b4166b84b)

The pipeline passed framework tests, real backend integration, Android unit tests,
lint, universal APK assembly, and an Android 35 install/launch/crash smoke check,
then published the APK. This is an installable **debug-signed development build**,
not a signed production release. AI mode is not implemented.

Future builds use the `Searchhh checks and APK` workflow on branch
`arena/01a0ea36-searchhh`. APK artifacts are gated on earlier checks; release
publication is gated on the emulator check. Repository tag-creation permissions
may require a maintainer to prepare the exact commit's tag using authenticated
GitHub CLI before the workflow can publish a release. Never move a published tag.

Minimum Android: 7.0 (API 24). Application ID: `app.searchhh.browser`.
Keep Android System WebView updated. The APK does not bundle a full Chromium engine.

### What works without hosting?

**0.2 source:** enter your name and the included backend initializes itself. Search
providers and the experimental remote relay remain third-party network dependencies;
no backend address or manually generated token is requested.

**Released 0.1 APK only:**

The reused WebView browser and local saved-results database are on-device.
**Opportunity swarms require your own running backend.** There is no hidden shared
host or claim of free permanent hosting. In Settings, enter your HTTPS backend URL
and its access token. This token protects your private server; it is not a search
provider API key. Source-provider keys are not required for default mode.

## Optional external backend setup (not required by 0.2 Android)

Install Docker Engine + Compose v2 on a server, then:

```sh
cd backend
cp .env.example .env
# Fill three independent random values using:
python3 -c 'import secrets; print(secrets.token_hex(32))'
# Set POSTGRES_PASSWORD, SEARCHHH_ACCESS_TOKEN and SEARXNG_SECRET in .env.
docker compose --env-file .env up -d --build
```

API listens only on `127.0.0.1:8000` on the server. Put an HTTPS reverse proxy
(e.g. Caddy) in front of it. Do not expose PostgreSQL, Redis or SearXNG directly.
The Android app requires HTTPS for its backend. `/health` checks database/queue;
`/docs` provides FastAPI's interactive API documentation. `/v1/*` requires
`Authorization: Bearer <your-server-token>`.

This is a **single-owner deployment**, not a public multi-user SaaS. Use strong
secrets, a firewall, request-size/rate limits at the reverse proxy and resource
limits. Pin images and audit dependencies before production.

## Included Android workflow (0.2)

1. Enter your name; ID and secure credential are generated independently.
2. Select Opportunity Finder or Link Mode and choose from 128 catalog adapters.
3. Start a public search; optionally enable bounded, robots-aware page discovery.
4. Eight-source batches repeat with 75-second cooldowns (150 on errors).
5. Stop prevents further commits; save/open/export results locally.
6. Settings shows backend and remote-bridge state. Share agent access only with a
   trusted MCP SSE client; no AI model is bundled.

## Optional external stack workflow

1. Configure the backend and test the connection in Android Settings.
2. Choose Opportunity Finder or Link Mode; select from 128 sources.
3. Enter a topic. Optionally enable Scrapy one-hop discovery.
4. Start the swarm. SearXNG handles parallel source requests; RQ schedules another
   pass after a 60-second cooldown (120 seconds when errors are reported).
5. Stop prevents further result commits. An in-flight source request may finish;
   an active crawl subprocess checks for Stop every half-second.
6. Save locally with Room, open in the reused browser, or export JSON.

Results retain provenance. Known source dates sort first; unknown dates are never
replaced with discovery time. A detected application form is **not** confirmation
that an application is open, funded, eligible or safe. Check the original page.

Dedup uses w3lib canonicalization plus tracking-parameter removal, retaining form
IDs and meaningful query values. `.bot`, `.onion`, private-network URLs and common
bot/challenge pages are excluded. Scrapy obeys robots and throttles per domain.
No form contents are fetched by the opportunity spider.

Safety bounds are explicit: one active swarm per server, 5,000 stored results per
session, eight candidate pages per pass, 2 MB crawl responses and bounded timeouts.
Reaching result capacity stops with `capacity` status. There is no unlimited-access
promise. Source indexes overlap and some providers can be unavailable.

## Tests

```sh
python3 -m venv .venv
.venv/bin/pip install -r backend/requirements.txt
PYTHONPATH=backend .venv/bin/pytest backend/tests -q
npm install --prefix js-tests --ignore-scripts
npm test --prefix js-tests -- --runInBand
python3 scripts/check_locale_strings.py
# JDK 17 + Android SDK/NDK:
./gradlew testDebugUnitTest lintDebug assembleDebug -PuniversalApk
```

See [source/license ledger](docs/searchhh/SOURCES.md) and
[criticism and test record](docs/searchhh/VALIDATION.md). Existing upstream browser
features retain their upstream notices. New framework code is licensed under
GPL-3.0-or-later with this repository.

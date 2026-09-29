> **Searchhh 0.3 test APK:** [Download](https://github.com/regularshowrigby8-create/SEARCHHH/releases/download/v0.3.0-test-13/searchhh-0.3.0-test.apk)
> · [Release scope and validation](docs/searchhh/RELEASE-0.3.md)
> · [Passed CI](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36513573863)
>
> Global portal crawling and optional external AI review are now included. Provider
> keys/consent are required for AI; the optional public relay was unavailable in the
> final run. The 0.2 release evidence below is retained as historical documentation.

# Searchhh

An Android opportunity-discovery framework built **on existing open-source tools**.
Development build, not a production-certified crawler.

**0.2 test release:** included phone backend, name-only onboarding, random
per-installation identity/credentials, official MCP SSE tools, a tested outbound
relay, and 128 catalog adapters. No server deployment by the Android user.
[Architecture and availability limits](docs/searchhh/INCLUDED-BACKEND.md).

## Architecture

- Android: imported **EinkBro v16.7.0** browser, Kotlin, Jetpack Compose and Android System WebView; Room persistence and in-process Searchhh API calls; legacy Retrofit/WorkManager integration is retained.
- Included backend (0.2): Kotlin/Room/coroutines, Ktor + official MCP SDK, JSch remote bridge, public search connectors. No user deployment.
- Optional external stack: Python **FastAPI**, **SearXNG** (128 configured adapters), **Scrapy**, **PostgreSQL**, **Redis + RQ**.
- Future, not implemented: AI swarm/BYOK mode, Go crawler, OpenSearch.

Searchhh is not an AI agent in this release. Opportunity classification uses explicit
form/cohort/certification signals. It discovers public pages; it does not bypass
logins, CAPTCHAs, rate limits or private networks, and never submits forms.

## Android test APK

**[Download Searchhh 0.2.0 test APK](https://github.com/regularshowrigby8-create/SEARCHHH/releases/download/v0.2.0-test-11/searchhh-0.2.0-test.apk)** — 28.3 MB.

[Release and checksum](https://github.com/regularshowrigby8-create/SEARCHHH/releases/tag/v0.2.0-test-11)
· [Successful validation run](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36505625533)
· [Exact source](https://github.com/regularshowrigby8-create/SEARCHHH/tree/22d6835343e933c6d0d82ecc6be5a661b441a88d)

The pipeline passed framework tests, real 128-adapter configuration integration,
Android unit tests, lint, APK assembly and Android 35 device checks. Device tests
covered identity isolation, Room migration, MCP initialize/list/call/authentication,
credential rotation and Stop. **Public MCP SSE negotiation and a tool call through
the real SSH/HTTPS relay also succeeded.**

The live search probe returned 40 results from GitHub/Hacker News. The selected
public SearXNG node returned HTTP 429; this is reported, not bypassed. The catalog
is **not** a promise of 128 simultaneously working upstreams.

This is a **debug-signed development build**, not a production release. Export saved
results before uninstalling an older test build if Android rejects an update due
to a different debug signing certificate. No AI model is bundled.

Future builds use the `Searchhh checks and APK` workflow on branch
`arena/01a0ea36-searchhh`. APK artifacts are gated on earlier checks; release
publication is gated on the emulator check. Repository tag-creation permissions
may require a maintainer to prepare the exact commit's tag using authenticated
GitHub CLI before the workflow can publish a release. Never move a published tag.

Minimum Android: 7.0 (API 24). Application ID: `app.searchhh.browser`.
Keep Android System WebView updated. The APK does not bundle a full Chromium engine.

### What works without hosting?

Enter your name and the included backend initializes itself. Search providers and
the remote relay remain third-party network dependencies; no backend address,
hosting account or manually generated token is requested. The relay terminates
HTTPS and can see forwarded traffic; disable it in Settings if unwanted. URLs can
change after reconnection. Ordinary in-app backend calls do not use the tunnel.

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
Clients should use HTTPS for this optional external API. `/health` checks database/queue;
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

Use its documented `/v1/*` API from your own client. Current Android Settings
exposes the included backend, not external-server setup. The external worker uses
SearXNG/Scrapy and repeats after 60 seconds (120 when errors are reported).

Results retain provenance. Known source dates sort first; unknown dates are never
replaced with discovery time. A detected application form is **not** confirmation
that an application is open, funded, eligible or safe. Check the original page.

Dedup canonicalizes URLs and removes tracking parameters while retaining form
IDs and meaningful query values (OkHttp on Android; w3lib in the external stack). `.bot`, `.onion`, private-network URLs and common
bot/challenge pages are excluded. The Android crawler uses Jsoup and crawler-commons
robots rules; the external crawler uses Scrapy. Forms are not fetched or submitted.

Safety bounds are explicit: one active swarm per installation, 1,000 results per
phone session (5,000 in the optional external stack), bounded page discovery,
response sizes and timeouts. Reaching capacity stops with `capacity` status.
There is no unlimited-access promise. Source indexes overlap and providers can
be unavailable. Broad physical-device, long-duration and live accuracy testing
remain outstanding.

## Tests

```sh
python3 -m venv .venv
.venv/bin/pip install -r backend/requirements.txt
PYTHONPATH=backend .venv/bin/pytest backend/tests -q
npm install --prefix js-tests --ignore-scripts
npm test --prefix js-tests -- --runInBand
python3 scripts/check_locale_strings.py
# JDK 17 + Android SDK/NDK:
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest -PuniversalApk
```

See [source/license ledger](docs/searchhh/SOURCES.md) and
[criticism and test record](docs/searchhh/VALIDATION.md). Existing upstream browser
features retain their upstream notices. New framework code is licensed under
GPL-3.0-or-later with this repository.

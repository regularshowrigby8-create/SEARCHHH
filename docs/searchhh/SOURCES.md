# Source and license ledger

## Imported application

- **EinkBro**: https://github.com/plateaukao/einkbro
- Release: `v16.7.0`; commit: `9904018cdbbbd5049c62fa521f3faf59fad8383d`.
- License: GPL-3.0-or-later. Original `LICENSE.md`, contributors, source headers and documentation are retained.
- Imported with GitHub CLI's repository tarball endpoint, not reconstructed from snippets.
- The original README is preserved at `docs/searchhh/UPSTREAM-EINKBRO-README.md`.
- Retained: WebView implementation, tabs, reader mode, downloads, ad filtering, browser settings, Compose components and existing Room stores.
- Changed: application ID/name/launcher, disabled backup, dependency declarations, CI, JavaScript **test harness** (native token signature, simulated hit testing/visibility, observer cleanup). Production translation JavaScript is unchanged.
- New framework code is isolated in `app/.../searchhh/` and `backend/searchhh/`; it is not attributed to upstream.
- Searchhh is an independent derivative, not an official EinkBro release. The upstream icon is retained in this initial test build.

## Reused tools, not copied/reimplemented

| Tool | Source | License | Role |
|---|---|---|---|
| SearXNG | https://github.com/searxng/searxng | AGPL-3.0-or-later | Metasearch and provider adapters |
| Scrapy | https://github.com/scrapy/scrapy | BSD-3-Clause | Crawl scheduling, robots, throttling, extraction |
| FastAPI | https://github.com/fastapi/fastapi | MIT | HTTP API, validation and API documentation |
| RQ | https://github.com/rq/rq | BSD-2-Clause | Redis-backed work and delayed passes |
| SQLAlchemy | https://github.com/sqlalchemy/sqlalchemy | MIT | PostgreSQL persistence |
| PostgreSQL | https://github.com/postgres/postgres | PostgreSQL License | Job/result storage |
| Redis 7 | https://github.com/redis/redis | Version-dependent; inspect the deployed image's license | Queue and coordination |
| Retrofit / OkHttp | https://github.com/square/retrofit / https://github.com/square/okhttp | Apache-2.0 | Android API transport |
| AndroidX Compose / Room / WorkManager / Security | https://github.com/androidx/androidx | Apache-2.0 | UI, local saves, status sync, encrypted preferences |
| w3lib | https://github.com/scrapy/w3lib | BSD-3-Clause | URL canonicalization |

The 33-source allowlist was checked against SearXNG settings commit
`12f8b6515ca77c3c3bc1498584950ef5daca1433`. Inactive upstream adapters and
credential-requiring alternatives were not selected. Catalog presence is not a
claim that all providers are currently reachable.

SearXNG is a separate, unmodified service. Compose currently uses its official
`latest` image for initial integration; pin the tested digest before production.
Keep its notices and provide the corresponding source to service users as required
by its license. Do not relabel it as an original Searchhh search engine.

No unlicensed repository code was imported. Two superficially matching
FastAPI/Scrapy/PostgreSQL/Redis projects were rejected because no license was found.

## Searchhh 0.2 included-backend additions

Ktor (Apache-2.0), official MCP Kotlin SDK 0.4.0 (MIT at that tag), JSch mwiede fork
(BSD-style), Jsoup (MIT), and crawler-commons (Apache-2.0) are consumed as dependencies,
not copied and relabeled. Review their packaged notices before production distribution.
See [included backend design](INCLUDED-BACKEND.md) for architecture, API boundaries,
public service dependencies and explicit validation limits. SearxDroid's "self-contained"
description was checked: it is a public-instance client, not a bundled SearXNG daemon.
No code was imported from repositories with unclear licensing. Chaquopy was researched
but not adopted: the current SearXNG native-wheel requirements were not demonstrated
portable across all supported Android ABIs. The Kotlin path avoids pretending that
PostgreSQL/Redis/Linux containers run inside an ordinary Android APK.

The expanded **128-entry** catalog uses SearXNG settings commit
`12f8b6515ca77c3c3bc1498584950ef5daca1433`. The real-container CI check asserts every
catalog ID is in the running SearXNG `/config` response, not only in local JSON.
Public-instance selection is independent of that CI server and checks the selected
node at runtime. A catalog entry is not a live-provider guarantee.

Relay event framing (`event: tcpip-forward`, `address`) was checked against the
existing Elixir client https://github.com/erlef/localhost-run. No Elixir code is
bundled. SSH banners/help URLs are never treated as tunnel endpoints. Before sharing
a tunnel, the Android bridge checks public unauthenticated rejection and an
authenticated health response matching this installation. This is a connectivity
check, not a claim of end-to-end TLS: the relay still terminates HTTPS.

Full MIT (MCP), BSD-style (JSch), and Apache-2.0 (crawler-commons) license texts are
bundled in `app/src/main/assets/searchhh-licenses/`, alongside the repository's
existing upstream licensing. Pins remain explicit in Gradle; this debug build is
not presented as a completed third-party vulnerability audit.

## 0.4 crawler catalogue and extraction integrations

All 100 submitted codebase references plus 20 additions are preserved in
`app/src/main/assets/searchhh-codebases.json`; source input and reproducible audit
are in `tools/crawlers/`. See [CRAWLER-AUDIT.md](CRAWLER-AUDIT.md). This registry is
metadata, not an import of 120 software distributions. GitHub SPDX metadata may
be missing or inconclusive and does not establish license compatibility. Original
names/URLs remain separate from corrected source references; missing repos are
not fabricated and retired tools are not represented as live adapters.

Actual new **optional Python backend** dependencies:

| Component | Version | Source | License | Use |
|---|---|---|---|---|
| Extruct | 0.18.0 | https://github.com/scrapinghub/extruct | BSD-3-Clause | Local JSON-LD extraction from already-fetched HTML |
| Trafilatura | 2.2.0 | https://github.com/adbar/trafilatura | Apache-2.0 | Local article/evidence text extraction |

Installed through pinned package dependencies, not copied source snippets.
Parsel is already used by Scrapy's response selectors; it is not counted as a
second independent crawler. These Python tools are not bundled into Android.
The phone reuses its existing Jsoup, Gson, OkHttp and crawler-commons libraries
with small bounded integration glue for structured/sitemap discovery. No source
from secret scanners, brute-force tools or server crawler frameworks is bundled
as executable Android code. No unsupported universal ignore profile is activated.

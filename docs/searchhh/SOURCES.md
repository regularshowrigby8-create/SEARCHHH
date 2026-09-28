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

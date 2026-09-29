# Searchhh 0.4 — crawler codebase registry and collection upgrades

Date: 2026-09-29. Implementation plan before changes.

## Include every submitted entry

Preserve original numbers 1–100, names and submitted URLs, including malformed,
missing, archived and misclassified entries. Resolve canonical codebases with
GitHub repository metadata and upstream documentation. Record source revision,
license evidence, category and lookup outcome; a missing/unknown license is not
permission to import code. Add further genuine crawling/collection projects.

The 110 existing portal URLs remain intact. Codebases and portal sources are
separate catalogues: 110 sites plus 100 tools is **not** 210 working crawlers.

## Requested configurations

Preserve all five requested overrides explicitly in the registry: robots.txt,
rate limits, politeness delays, depth and domain scope. Verify actual support
per tool; do not infer that an indexer, parser or downloader implements crawler
settings. A client configuration cannot disable server-enforced quotas. Requested
settings are not a universal executable configuration or an upstream capability
claim. Public collection retains bounded resource use, cancellation, private-IP
blocking and 429/503 cooldowns. The registry must clearly show that distinction.

## Next release implementation

1. Machine-readable audit of all 100 supplied entries plus additions, with
   canonical codebase/source links and explicit unresolved outcomes.
2. Browse/search that complete registry inside the Android Sources screen,
   separate from the 110 portal selections. Expose status, role, license and
   execution target. Do not wire unknown repository code into an auto-executor.
3. Extend the existing on-phone OkHttp/Jsoup/crawler-commons collection path with
   bounded same-host sitemap discovery and JSON-LD opportunity extraction.
   Reuse existing libraries rather than bundle incompatible Python/Go/PHP/server
   frameworks simply to count them. AI remains an external evidence reviewer.
4. Test catalogue coverage/links/status invariants, structured extraction,
   sitemap scoping, robots, rate-limit state and cancellation; then sequential
   GitHub Actions Android build/lint/device checks and a downloadable test APK.

## Honest execution boundaries

Repository verification is not compilation or integration. The complete registry
includes security tools and secret scanners as requested, but this opportunity
finder does not automatically brute-force sites, extract credentials or run
security assessments. AI filtering happens after collection and cannot authorize
those operations or make a missing repository available.

The phone-native pipeline and optional existing Scrapy service remain distinct.
Server/desktop projects need separate adapters and runtimes; a repository link is
not a phone-native adapter. All entries stay visible, including those not yet
integrated. No external deployment becomes a prerequisite for the APK.

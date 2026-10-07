# SEARCHHH project audit

Date: 2026-10-07
Branch: `arena/3674d801-searchhh`
Source inspected: `9c205f33f5bba71157df130bb3f7782abf534481` plus the current working-tree implementation

## Product surface

SEARCHHH is an Android/Kotlin opportunity-discovery feature built into EinkBro.

- `SearchhhActivity` owns the Compose shell and Discover, Sources, Saved, and Settings routes.
- `LocalBackend` owns the in-process search session, Room persistence, cancellation, result retention and optional AI review.
- `PortalCrawler`, `PublicSearch`, `PortalParser`, `StructuredDiscovery`, and `LocalPolicy` enforce bounded public crawling, robots rules, same-host discovery, date handling, duplicate identity, and filtering.
- `SearchhhResultsWebViewActivity` is now the Searchhh-owned result/source WebView. It does not reuse EinkBro `BrowserActivity`, and it is integrated with the Searchhh session, Room saved results, retry, back/reload actions, provenance, result IDs and errors.
- The Compose Discover route no longer renders crawler-result cards; it presents the session summary and opens the complete result document in the Searchhh WebView. The Saved route also opens its Room-backed results in the same WebView document, so native Compose remains a route shell rather than a second crawler-result renderer.

## Crawler capability boundary

The catalogue in `app/src/main/assets/searchhh-codebases.json` remains reference metadata. It is not treated as a set of installed runtimes.

Four declared, reviewed Android-native profiles are executable through the app and MCP start-search tool:

| Profile | Catalogue entry | Android implementation |
|---|---:|---|
| `phone-scheduler` | 1 / Scrapy | bounded `PortalCrawler` scheduling, OkHttp fetches, robots, cooldowns, same-host limits and cancellation |
| `phone-article` | 65 / Trafilatura | Jsoup main/article evidence extraction |
| `phone-structured` | 66 / Extruct | bounded JSON-LD extraction through `StructuredDiscovery` |
| `phone-selector` | 67 / Parsel | scoped CSS-selector/listing extraction through `PortalParser` |

`StartRequest.adapter` is validated by `LocalBackend`; unknown profiles fail closed. Selected profiles are also applied to detail/sitemap extraction, not just the first page. Scrapy, Extruct, Trafilatura and Parsel native runtimes remain available only where the optional Python backend is deployed. No arbitrary repository code is embedded or launched.

## WebView and transport review

- Result HTML is escaped/static and includes result ID, score, kind, source provenance, publication/discovery dates, evidence links, review quotes, deadline/eligibility evidence, saved state, errors and retry.
- App-private `searchhh://save`, `searchhh://unsave`, and `searchhh://retry` actions are allow-listed; save actions require a 64-character lowercase result hash and no JavaScript bridge is installed.
- External navigation accepts only canonical public HTTP(S) URLs. HTTP additionally requires the existing `BrowserConfig.K_ALLOW_HTTP` preference. Mixed content is blocked and TLS errors are cancelled.
- External pages may use JavaScript for normal rendering, but they cannot call a Searchhh app bridge. File/content access is disabled.
- The new activity is non-exported in the manifest.

## Quality and verification state

The lexical inventory has 622 interaction candidates and 9 reviewed contracts. `quality/interaction-inventory.json` and `docs/INTERACTION_INVENTORY.md` were regenerated after the WebView/profile/UI edits. `tools/quality/interactions.py --check`, `git diff --check`, and the Python quality suite passed in this workspace.

The unfinished scan and release gates remain open. The project is not release-approved.

## Known blockers and risks

- This workspace has no `java`, `javac`, `JAVA_HOME`, Android SDK, or emulator. Gradle cannot start locally, so Kotlin compilation, Android JVM tests, lint, Detekt, ktlint, release R8, connected tests, screenshots, accessibility inspection, WebView device behavior, signing and release smoke are not claimed.
- The new Kotlin/WebView/profile files therefore still require hosted or provisioned Android compilation before release consideration.
- Existing hosted PR checks include failed strict Android/Detekt/device lanes and an inaccessible CodeQL C++ autobuild log. Those outcomes remain evidence, not passes.
- Public portal rate limits, robots changes, WebView renderer behavior, relay availability and optional AI-provider quotas remain runtime risks.

## Non-goals

No release signing, backend deployment, certificate override, cleartext relaxation, catalogue inflation, fabricated device evidence, or release-complete claim is permitted until the required Android, device, security and release checks pass on the exact candidate commit.

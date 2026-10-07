# Searchhh 0.4.0 test release — construction and validation

Status: **published and tested** — all five sequential GitHub Actions gates passed.

**[Download Searchhh 0.4.0 test APK](https://github.com/regularshowrigby8-create/SEARCHHH/releases/download/v0.4.0-test-15/searchhh-0.4.0-test.apk)**

- [Release](https://github.com/regularshowrigby8-create/SEARCHHH/releases/tag/v0.4.0-test-15)
- [Successful CI run 36518594817](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36518594817)
- APK source: `f643e19c56081e2b792f6204bd44902392c16f82`.
- APK size: **28,380,930 bytes**.
- GitHub-reported SHA256: `c3898c9c4eb2054ff1d86265dce1225bc07a33a3c2e1f441247b2c2f62b17ffb`.
- SHA256SUMS is also attached to the release. The digest above is from GitHub's
  asset metadata, not a locally downloaded/rehashed binary.

## Implemented

- The original **110 portal sources are preserved byte-for-byte** (SHA256
  `958985b1b7625ecf9f535c1cd25d07b058033474755f7cf5560ac79c21a728b0`).
- A separately labelled, searchable/exportable **120-entry codebase registry**:
  all 100 supplied entries, including broken/archived ones, plus 20 additions.
  Codebase links, roles, revisions, license metadata and integration status are
  shown in Sources → Codebases. These are not 120 installed Android crawlers.
- Added authenticated read-only `list_codebases` MCP tool. The eight real MCP
  tools do not expose an arbitrary code executor or treat codebase IDs as portals.
- On-phone JSON-LD discovery for supported programme/course/event/job types;
  original source evidence retained, unverified status preserved, no remote
  context resolution or JavaScript execution. HTML and Atom/RSS continue working;
  relative Atom links now resolve against their publisher.
- If an HTML listing has no matching entries, bounded same-host sitemap discovery
  checks one declared/default sitemap, at most one index child, and one discovered
  detail page per source/pass. It does not recursively exhaust every sitemap.
- Detail-page text/application extraction; same-host follow-up; server cooldowns
  apply to robots responses too and survive resetting a search on the same process.
- Optional Python backend now actually reuses **Extruct 0.18.0** and
  **Trafilatura 2.2.0** with existing Scrapy/Parsel. No Python runtime or external
  deployment is required by the Android APK.

## Explicitly not completed

- Runtime adapters/installation for every catalogue project. Two exact submitted
  identities (#50, #78) remain unresolved; #92 has a probable spelling correction; #83 is a packaging repository.
- The five requested ignore options are retained as audit requirements and
  field-level capability findings, **not enabled as a universal runtime profile**.
  The public crawler still observes robots, cooldowns and resource/network bounds.
- No automatic security scans, brute-force enumeration, secret harvesting, form
  submission, authentication/CAPTCHA bypass or paid scraping service fallback.
- No automatic execution of arbitrary repository code by AI. AI remains external
  evidence review with the previous OpenRouter/Z.AI implementation, not crawling.
- No full multi-model consensus/automatic provider signup or Material 3 overhaul.
- Redirect/JS/login/PDF-only portals can still need manual browser use. Sitemaps
  are bounded, same-host and XML-only; compressed-file/foreign-host sitemap trees
  are not exhaustively supported. No claim of global exhaustive real-time search.
- Continuous collection keeps the existing rolling 1,000-result view. Save/export
  important results; Android can stop background processes.

## Validation

Local checks completed:

- Python: **43 tests passed**, including all-entry preservation, exact URL retention,
  separate source/tool counts, unresolved entries, truthful integration status,
  requested-profile preservation and Extruct/Trafilatura integration fixtures.
- Reused browser JavaScript: **5 suites / 13 tests passed**.
- Locale consistency: no stale keys (existing untranslated strings remain).
- Whitespace/diff checks passed.
- Added **12 Android crawler unit tests** covering structured data, malformed/deep
  JSON, source provenance, sitemap scoping, relative feeds, application extraction,
  robots, provider cooldowns, independent hosts and cancellation. The complete
  Android unit/lint/build gate passed in CI, not locally (no local Java/Android SDK).
- Android device test expanded for the 120-entry registry, 110-source separation,
  MCP discovery and rejecting catalogue IDs as executable portal selections. A
  separate Compose device test navigated Sources → Codebases, filtered to #92,
  and verified its catalogue-only label and source-code link. Both passed on API 35.

Sequential CI results: framework tests → real optional backend stack integration
(PostgreSQL/Redis/RQ/SearXNG) → Android unit tests/lint/APK assembly → Android 35
instrumentation/launch → APK publication: **all passed**.

Live portal diagnostic (a two-portal sample, not all 110 sources): **5 results**;
provenance counts `Opportunity Desk=3`, `Detail page extraction=2`,
`Application link extraction=2`, `Opportunities Corners=2`; `errors=[]`.
Structured/sitemap extraction has fixture coverage; this live sample does not
certify every structured type or sitemap endpoint.

Final optional public relay diagnostic: **UNAVAILABLE: SocketTimeoutException**.
Public remote connectivity is **not verified for test-15**. Local authenticated
MCP passed, and the phone crawler does not depend on the relay. Earlier test-14
had a successful public relay diagnostic; that is not substituted for the final
run's outcome. No live provider AI inference or automatic signup was tested.
This is a debug-signed development release. If signing differs from an older test
build, export saved results before uninstalling. No AI weights are bundled.

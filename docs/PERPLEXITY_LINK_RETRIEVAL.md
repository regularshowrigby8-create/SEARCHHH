# Perplexity plan retrieval and repository comparison

Source retrieved: `https://www.perplexity.ai/search/ed8ba7d0-9db1-4bbb-8b35-e386669ce43a#13`

Retrieval status: **complete**. The shared page returned 32 fetch chunks (`0` through
`31`). It contains multiple follow-up answers, not one single implementation
specification. This file records the complete topic inventory and compares each
proposal with this repository. The page's code examples are treated as proposals,
not as authority to replace the existing app.

## Retrieved topic inventory

1. Public discovery source plan: 100 general, metasearch, developer, social,
   academic, archive and opportunity sources.
2. Integration tiers: core sources first, opportunity sources second, commercial
   and specialist sources later.
3. Common result adapter schema: title, URL, snippet, source, author, dates,
   content type and official/unofficial status.
4. Public-web boundary: no private systems, restricted content, dark web,
   CAPTCHA/login/paywall bypass or unrestricted crawling.
5. Kotlin/Compose/Material 3/WebView starter architecture.
6. Theme, typography, shape, color, dynamic-color and AMOLED suggestions.
7. Native Compose animation catalogue and optional Lottie/Rive/Shimmer tools.
8. UI/menu/drawer/navigation catalogue and adaptive phone/tablet navigation.
9. Central route registry and central menu registry.
10. AI-agent task sequence: audit, foundation, theme, navigation, drawer, home,
    results, filters, WebView, animation, accessibility, testing, performance,
    CI and final verification.
11. Stable test tags and behavior-based Compose tests.
12. Interaction inventory and dead-button detection rules.
13. Unfinished-code Gradle/factory checks.
14. WebView requirements: HTTPS first, scheme validation, safe bridges, SSL
    handling, back/forward/reload/stop/share/save/external open.
15. UI-change audit: prove that the requested production UI visibly changed.
16. Screenshot/visual regression suggestions: Paparazzi/Roborazzi/Showkase.
17. Performance suggestions: Macrobenchmark, Baseline Profiles, memory and
    recomposition review.
18. Continuous CI, master verification task and emulator smoke tests.
19. Adversarial criticism agent and completion contract.
20. Factory expansion: safe autofix policy, unfinished detector, interaction
    audit, UI-change audit, agent alerts, repair loop, master gate, CodeQL,
    dependency review, Reviewdog and dashboard health.
21. Non-terminal finding states: OPEN, ASSIGNED, AUTOFIX_ATTEMPTED,
    RECHECK_REQUIRED, FIXED_PENDING_REVIEW, VERIFIED, BLOCKED,
    WONT_FIX_APPROVED and REOPENED.
22. Searchhh product plan: Kotlin Android UI, Python/FastAPI backend, permitted
    adapters, URL normalization, duplicate filtering, ranking, deadlines,
    source credibility and continuous user-stoppable sessions.
23. Crawler requirements: robots.txt, domain delays, depth/page/request limits,
    clear User-Agent, content limits, cancellation, no form submission and no
    access-control bypass.
24. Search categories: opportunities, certifications, forms, developer,
    regional, remote/in-person, free/paid, education, deadline and safety.
25. Backend progression: Kotlin-only first where possible, Python backend,
    PostgreSQL/Redis/OpenSearch later, Go only for demonstrated scale needs.
26. 100 niche/alternative search-engine names, with API/terms/availability
    caveats and an initial 15-source recommendation.

## Comparison with current implementation

| Area | Current repository | Status |
|---|---|---|
| Public discovery | 110 portal entries in `searchhh-portals.json`; Kotlin crawler, robots/rate/depth policies and result normalization exist | **PARTIAL / real runtime exists, not 110 independent API adapters** |
| Source catalog | Source and codebase catalogues are visible in Sources UI | **IMPLEMENTED as catalogue; catalogue is not execution proof** |
| Backend | In-app Kotlin backend, Room, coroutines, WorkManager/foreground service and optional FastAPI backend exist | **PARTIAL / external provider adapters remain bounded** |
| Continuous search | Start/stop, rounds, duplicate count, filtering, retention and cancellation exist | **IMPLEMENTED with bounded retention and user stop** |
| Public-web boundary | Robots, rate limits, depth/domain restrictions and no form submission are represented/tested | **PARTIAL; provider terms and per-source support remain review work** |
| URL/result model | Canonicalization, source identity, dates, evidence, review and saved-result data exist | **IMPLEMENTED for current pipeline; not every proposed field exists** |
| WebView | Existing BrowserActivity/AndroidX WebKit, safe schemes, TLS cancellation, HTTP approval and mixed-content denial | **PARTIAL; device regression remains open** |
| Theme | Central `SearchhhDesignSystem`, light/dark tokens and system dark-mode selection added | **IMPLEMENTED checkpoint; still Compose Material 2-compatible, not Material 3 migration** |
| Animations | Existing app motion only; no broad animation library added | **OPEN intentionally; behavior/accessibility first** |
| Navigation | Four real routes and centralized `SearchhhMenuRegistry` | **IMPLEMENTED for existing screens; proposed future routes not invented** |
| Drawer/adaptive navigation | Existing bottom navigation; no new drawer or NavigationSuiteScaffold | **OPEN** |
| Stable tags | `SearchhhTestTags` exists and interactions checker exists | **IMPLEMENTED / coverage incomplete** |
| UI tests | Existing tests and hosted behavior tests; full Compose interaction closure incomplete | **PARTIAL** |
| Unfinished code | Existing quality scanner reports 557 files/178 findings | **DETECTED, NOT FIXED** |
| Dead controls | Interaction inventory and contract checks exist | **CANDIDATES DETECTED; not all behavior closed** |
| UI-change verification | Browser dashboard checks and source interaction evidence exist | **PARTIAL; native screenshots/device proof open** |
| Visual regression | Hosted browser screenshots; no approved native screenshot baseline | **OPEN** |
| Performance | Static checks and bounded output; no complete device benchmark | **OPEN** |
| Factory retry | `auto_issue.py` provisions selected ecosystems, retries once and stops bounded | **IMPLEMENTED** |
| Factory remediation | `remediation_plan.py` assigns owner/action and blocked prerequisites | **IMPLEMENTED; no security auto-fix** |
| Product security | `app_security_policy.py` checks WebView/HTTP/TLS/file/debug/credential policy | **IMPLEMENTED; two cleartext findings remain** |
| Agent alerts | Remediation packets exist; dedicated alert/dashboard health layer is not complete | **PARTIAL** |
| Safe autofix | Safety policy blocks arbitrary mutation; no approved formatting autofix workflow yet | **OPEN / intentionally conservative** |
| CodeQL/dependency review | 65/66-tool factory has local and hosted security tools; CodeQL/Reviewdog not enabled | **OPEN** |
| Completion contract | `AGENTS.md`, quality policy and issue workflow enforce handoff rules | **IMPLEMENTED as repository policy; technical enforcement has limits** |
| Finding lifecycle | Factory records PASS/FAIL/REPORTED/BLOCKED and issue packets | **PARTIAL; full OPEN→RECHECK_REQUIRED lifecycle should be added** |

## Source/repository comparison

The link names official Android/AndroidX, Material, WebKit, Compose, Lottie,
Rive, Coil, Paparazzi, Roborazzi, Showkase, LeakCanary, Macrobenchmark,
Accessibility Test Framework, CodeQL, Reviewdog, SearXNG, Common Crawl, GitHub,
GitLab, OpenAlex, CORE, BASE, Reddit, YouTube, Bluesky, Mastodon and many other
services. The current repository deliberately does not add every named project:
`docs/factory/FLEET.md` and `tools/factory/repositories.json` contain current
factory provenance, while this document records proposals that still need an
actual adapter, license/terms review, credentials or a safe runtime contract.

A listed source is not a working adapter. A listed UI library is not a shipped
APK dependency. A listed crawler is not permission to crawl its target.

## Implementation decisions from the link

- Keep Kotlin/Compose/AndroidX WebKit for the APK.
- Keep Python/FastAPI as an optional backend path; do not introduce Go without a
  demonstrated scale bottleneck.
- Keep public-only crawling, robots/rate/depth/domain limits and Stop behavior.
- Use current four real routes; do not add placeholder routes from the proposal.
- Use native Compose behavior first; defer Lottie/Rive/Shimmer until a real
  visual requirement and approved dependency exist.
- Keep security, credentials, WebView bridges, cleartext and release signing out
  of unattended autofix.
- Use the factory to select, provision, run, normalize, assign and recheck.

## Next bounded implementation order

1. Run hosted compile/device verification for the new theme/menu checkpoint.
2. Add a real filter state and behavior test, not a filter-only screen.
3. Add the factory's unfinished-code and interaction candidate report as explicit
   registry jobs, reconciling with existing scanners rather than duplicating them.
4. Add a UI-change audit and agent-alert packet with non-terminal statuses.
5. Evaluate Material 3 migration only after dependency/build impact is audited.
6. Add adaptive navigation only when a real tablet/expanded-window destination
   contract exists.
7. Add native visual/performance evidence.

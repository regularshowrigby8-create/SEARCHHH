# Quality continuation: compiler remediation

> Historical compiler checkpoint only. Current assignments and verified state are in [NEXT_AGENT_WORK_ORDER.md](NEXT_AGENT_WORK_ORDER.md), [REMEDIATION_BACKLOG.json](REMEDIATION_BACKLOG.json) and the latest [implementation report](../QUALITY_IMPLEMENTATION_REPORT.md). Do not resume the old source SHAs as current work.

## Audit / baseline before this increment

Restored branch history to remote `6102e35` using a mixed reset after fetching; working files were preserved, including the three unpushed files from the prior handoff. The old local commit object was not available in the restored checkout, so that correction will be recommitted on the same session branch.

Repository-scoped GitHub APIs now work; `/user` returns integration-scope 403 (not the former 401). Run 36524028312 is confirmed **failure**. Its host lane reports 24 passing library tests, zero failures/skips, no new app tests, no APK. Device lane reports 28 passing tests in total (library unit/device results), no new app tests or APK. This is not app verification.

Local baseline: `python3 -m unittest discover -s tools/quality/tests -q` passes 26 tests. `./gradlew --continue searchhhVerification -PuniversalApk` exits 1 before configuration: Java is absent in this restored environment. No local adb detected.

Inspected `BrowserActivity`, `BrowserController`/`TabController`, `AlbumController`, browser container/pause implementations, existing quality policy and CI diagnostic emitter. Hosted compiler diagnostics identify:
- `BrowserActivity.kt:616`: `showAlbum` parameter differs from the interface name.
- `BrowserActivity.kt:618`: `removeAlbum` Boolean parameter differs from the interface name.
- `BrowserActivity.kt:1032`: deprecated `TRIM_MEMORY_MODERATE`.

Android's ComponentCallbacks2 reference says MODERATE is deprecated at API 35 and not delivered since API 34; BACKGROUND remains the recommended opportunity to free rebuildable resources. Reference consulted 2026-09-29: https://developer.android.com/reference/android/content/ComponentCallbacks2#TRIM_MEMORY_BACKGROUND . Do not hide this warning by replacing the old constant with an unexplained integer.

## Planned small changes

1. Preserve/push the native-generated-output scanner correction with its existing tests.
2. Match interface parameter names, preserving positional delegation; no named call sites to the old BrowserActivity parameter names were found.
3. Extract the existing background-tab pause/preload-release behavior into a small tested helper and trigger it at the supported BACKGROUND level. Do not pause the active tab. Test below-threshold, background/higher and missing-active-tab behavior.
4. Emit bounded multi-line diagnostic chunks, with truncation explicitly reported, to inspect compiler warnings without unavailable ZIP downloads. Preserve failed outcomes and warnings-as-errors.
5. Execute the checker tests, inventory check, diff check and hosted Android verification; record results without claiming the unexecuted app tests pass.

No new UI destination, runtime crawler, visual redesign or external dependency is being claimed by this increment.

## Second batch audit (before changes)

Run 36570002530 confirms the first three warnings are gone; compilation now exposes 23 remaining warnings (captured in `compiler-warnings-1965719.txt`). Inspected every reported call site and its relevant existing preference/crawler tests.

Planned corrections: use existing WindowCompat rather than deprecated Window calls; reuse JSON serializers; use language tags/Locale.ENGLISH rather than deprecated Locale constructors; preserve encrypted preferences and their existing narrowly scoped deprecation adapter (remove deprecated imports, not encryption); select crawler-commons' collection overload; rename a Notification.Builder extension shadowed by a platform member; remove an exhaustive-when's unreachable fallback. Preserve pre-34 cache trim semantics in a precisely scoped compatibility helper, with decision tests and real Android cache tests.

Checked crawler-commons **1.4 tag source** using GitHub API (`src/main/java/crawlercommons/robots/SimpleRobotRulesParser.java`, lines 589–649). The deprecated String overload uses `exactUserAgentMatching=false`, while the Collection overload uses the configurable default. Explicitly preserve the prior matching mode in a common adapter rather than silently changing robots behavior. Add fixtures for case-insensitive/prefix agent matching, allow/disallow, delay and sitemap extraction.

Locale updates must keep stored legacy language-only values readable and preserve script/region when saving new values. New preference tests will assert both. Crypto instrumentation will check round-trip and absence of plaintext credentials in an isolated store. No global warning suppression, weakened assertion, baseline or disabled test is authorized.

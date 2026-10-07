# SEARCHHH

SEARCHHH is a privacy-forward opportunity discovery workspace inside EinkBro. It searches configured public opportunity portals from the device, follows bounded same-host links when enabled, removes duplicates, keeps a durable Room session, and lets you save results for offline access. It also exposes an authenticated local MCP endpoint for a separately supplied AI client; no AI model or provider key is bundled in the APK.

> **Release status:** the source tree is active, but release approval is still blocked. The full Android/JDK/SDK/emulator quality chain has to pass on the exact candidate commit before a signed APK can be called a release. Debug APKs and hosted diagnostics are not release substitutes.

## What is implemented

- Compose Searchhh launcher with Discover, Sources, Saved, and Settings routes.
- Local Kotlin backend with Room persistence, rolling result retention, duplicate tracking, cancellation, and interrupted-session recovery.
- Portal catalogue and bounded HTML/RSS/JSON-LD/sitemap discovery with robots and politeness checks.
- Saved opportunities stored on-device and exportable JSON results/codebase audit.
- Optional encrypted AI-provider configuration and an authenticated MCP/SSE bridge.
- Searchhh-owned results WebView: crawler and saved output are rendered as one escaped document with result IDs, provenance, evidence, review/deadline excerpts, errors, retry, and save/remove actions. Result/source links no longer launch EinkBro's `BrowserActivity` path.
- Browser safety defaults: the Searchhh WebView has no JavaScript bridge, accepts only canonical public HTTP(S) result navigation, keeps HTTP opt-in, denies mixed content, and cancels invalid TLS.
- Four declared Android-native crawler capability profiles are selectable from Sources: bounded scheduler (`phone-scheduler` / Scrapy catalogue entry 1), article evidence (`phone-article` / Trafilatura 65), structured JSON-LD (`phone-structured` / Extruct 66), and CSS-selector extraction (`phone-selector` / Parsel 67). Other catalogue entries remain reference metadata; arbitrary repository code is never launched.
- Stable semantic test tags for routes, search controls, source filters, result details/open/save actions, and the results WebView CTA.

## Repository layout

- `app/` — Android app, Compose UI, local backend, browser, persistence, and tests.
- `backend/` — optional Python service and integration tests; the phone path does not require deployment.
- `ad-filter/`, `adblock-client/` — browser ad-blocking modules.
- `quality/`, `tools/quality/`, `docs/quality/` — interaction contracts, verification gates, evidence, and remediation queue.
- `app/src/main/assets/searchhh-portals.json` — configured public portal seeds.
- `app/src/main/assets/searchhh-codebases.json` — audited reference catalogue; it is not a claim that every entry is an installed runtime.

## Build and verification

The canonical environment is JDK 17, Android SDK 36, and an Android 35 emulator. From the repository root:

```sh
python3 -m unittest discover -s tools/quality/tests -v
python3 tools/quality/interactions.py --check
python3 tools/quality/unfinished.py
./gradlew --continue searchhhVerification -PuniversalApk
```

The last command is the full gate and includes host, release, backend, and device/evidence work. A connected emulator is required; `searchhhJvmVerification` is only a host subset. The current Arena workspace may not have Java or the Android SDK installed, in which case the exact Gradle command must be recorded as blocked rather than treated as a pass.

## Privacy and transport notes

Search queries are sent to the configured public portal hosts. The optional remote MCP relay is a third-party HTTPS terminator and can see forwarded traffic; it is disabled in Settings when not wanted. Credentials are generated per installation and stored through Android encrypted preferences. Forms are discovered and linked but never submitted by the crawler. TLS errors fail closed, and the app does not add a blanket cleartext exception to make a failing request succeed.

## Current quality work

The project is tracked in Linear as **SEARCHHH**. The current ordered work is recorded in `docs/quality/NEXT_AGENT_WORK_ORDER.md` and `docs/quality/issues/`:

1. reconcile the recovered diagnostic and interaction inventory;
2. execute the HTTP approval, mixed-content, unsupported-scheme, and invalid-TLS device regressions;
3. close action-to-state evidence for search, stop, filters, results, details, saves, routes, theme, accessibility, offline, and expiry;
4. run the complete Android verification and release chain.

Do not claim a feature is complete from a catalogue entry, a declared test tag, a host-only check, or an unexecuted screenshot requirement.

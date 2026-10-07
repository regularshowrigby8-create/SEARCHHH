# SEARCHHH platform workstreams

Date: 2026-10-07
Branch: `arena/3674d801-searchhh`
Candidate SHA: `e74ffb2ce62cbdb45055e7903dc047ecc04299f0`
Latest Android verification: [run 37556398995](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/37556398995)
Status: **PARTIAL / BLOCKED**. This operating model distributes work; it does not turn a tracker or document into a build runner.

## Platform strengths and boundaries

| Platform | Use it for | Do not use it for | Current output |
|---|---|---|---|
| GitHub / Actions | source of truth, pull requests, Android SDK/JDK runners, Gradle, emulator, CodeQL, artifacts and exact commit evidence | treating a green sub-job as release approval; hiding failed lanes | PR #2, branch `arena/3674d801-searchhh`, hosted Android build/test evidence |
| Linear | accountable work packages, dependencies, acceptance criteria, status, owners and run links | storing source code or replacing Gradle/device execution | SEARCHHH project, SAV-5–SAV-11 workstreams and dependency links |
| Notion | human-readable runbook, platform matrix, decision log, evidence interpretation and handoff | authoritative build status or executable tests | SEARCHHH Execution Plan with current WebView/adapter checkpoint and this operating model |
| Local repository tools | edit/debug source, fast Python checks, inventory generation, `git diff --check` and reproducible scripts | claiming Android/device results when JDK/SDK is absent | Python quality suite, syntax checks and source changes |
| Gmail | notification/approval inbox for GitHub/Linear/Notion events; optional triage labels | sending credentials, using emailed secrets, or acting as a build runner | CI notification visibility; credentials remain out of chat and source |

## Work allocation

### SAV-10 — Searchhh WebView and Android state surface

Owner: Android source lane. Scope: `SearchhhResultsWebViewActivity`, escaped HTML, allow-listed actions, Room save/remove, live/saved session state, public-URL/HTTP/TLS policy, Compose entry points and Android tests.

Acceptance: debug and release Kotlin compilation, WebView policy tests, result identity/provenance/save/retry behavior, no EinkBro `BrowserActivity` path, no privileged JavaScript bridge, and device evidence for navigation/security states.

### SAV-11 — Declared crawler adapters and optional backend

Owner: crawler/runtime lane. Scope: `CrawlerAdapter`, `LocalBackend`, `PortalCrawler`, MCP schema, Python API/worker/spider, adapter provenance and bounded extraction tests.

Acceptance: the four declared profiles execute through their permitted runtime boundary; unknown profiles fail closed; robots, public URL, cancellation, rate limits and form non-submission remain enforced; catalogue-only entries are not presented as installed runtimes.

### SAV-9 — Quality, device evidence and release convergence

Owner: verification/release lane. Scope: coordinate SAV-6/SAV-7/SAV-8/SAV-5; preserve current failures; reconcile exact-SHA lint/Detekt/ktlint/backend/device/security/release/signing evidence.

Acceptance: every required lane passes on one exact candidate commit. A debug APK, skipped job, missing artifact or tracker update cannot approve release. SAV-10 and SAV-11 block the action-to-state evidence lane; SAV-6, SAV-7 and SAV-8 block release convergence.

## Debugging loop

1. **Local fast loop:** edit one bounded workstream; run Python quality tests, `compileall`, inventory check and `git diff --check`.
2. **Linear coordination:** update the workstream description/comment with changed paths, acceptance evidence and blockers; create a dependency relation before starting a dependent lane.
3. **GitHub execution:** push the exact branch; run the Android/quality workflows. If KtLint auto-commits, fetch and rebase before adding the next fix. Inspect annotations/artifacts by run ID, not `latest`.
4. **Notion evidence:** record what passed, failed, blocked or was skipped, with the SHA and run URL. Preserve failure evidence; never rewrite a failed lane as success.
5. **Gmail triage:** use CI notifications to locate a run, then return to GitHub for authoritative logs/artifacts. Never copy tokens or verification codes into the workstream.

## Current platform blockers

- Local workspace has no Java/JAVA_HOME, so Gradle cannot run locally.
- Hosted run `37556398995` on `e74ffb2ce62cbdb45055e7903dc047ecc04299f0` executed 395 tests with 0 failures and produced debug/device APK evidence, but the overall device lane still failed the unchanged strict chain; repository-wide lint (465 app errors plus module errors), Detekt (2,601 weighted issues), formatting, unfinished-code/policy and release gates remain red or unresolved. Debug output is not release approval.
- Linear and Notion are coordination/evidence systems, not alternate Android build executors. The only valid build execution remains a provisioned JDK/Android runner (GitHub Actions or an equivalent explicitly provisioned CI runner).

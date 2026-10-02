# Sequential issue closure

User instruction: resolve issues sequentially before producing the next build.

- **Verification is not delivery.** Private test APKs are still compiled/installed inside CI to run actual Android tests. Ordinary release verification runs unit tests, lint and R8 but no longer calls `assembleRelease` or uploads debug APK downloads. The signed release assembly job stays downstream of the full quality/backend/device chain, and publication stays downstream of the signed-release device test.
- Do not mark an issue closed just because its code changed or its warning disappeared. Record the relevant executed regression result and inspect for new failures in the changed code.
- Do not start fixing the next issue before closing the active one. A blocked verification leaves the current issue open.

## Issue 1: toolbar border DrawAllocation (closed below)

Baseline: source `5896283`, run 36596759905; two lint findings in `ThemedEdgeBorderView`.

Changes awaiting execution:
- RectF allocation replaced by the equivalent scalar `Path.arcTo` call.
- Boxed scallop-center list replaced by the same arithmetic in an indexed loop.
- DashPathEffect/interval array reused for the current display density; regenerated only on first use or density change.

Closure evidence required:
1. Full app lint report no longer contains DrawAllocation for this view (other issue types still fail normally).
2. Android test compares 144 software-rendered pixel cases with the pre-fix stamp/dashed/classic algorithms: 3 densities, 4 widths, both edge directions, normal/inverted colors, and successive style/size changes on a reused view.
3. Android cache test proves stable object identity across repeated same-density requests and recreation/reuse when density changes.
4. Representative reference/current border PNGs and completion marker collected from the device. They are rendering-regression artifacts, **not full-screen before/after APK screenshots**.

No new release APK, dependency, warning suppression or source waiver is authorized by this fix. No measured startup/frame-time improvement or whole-draw allocation-free claim is made; other color/theme helper allocations remain outside this issue.

Next issue stays queued until these checks are observed passing.

### First hosted attempt — b7ca3e8, run 36599950711

App lint decreased from 470 to 468 errors; the two priority-9 DrawAllocation findings disappeared. 351 host tests passed, Detekt remains at 2790 issues. **Issue NOT closed:** Android instrumentation compilation rejected the unavailable `androidx.test.annotation.UiThreadTest` import; the new pixel/cache tests did not execute. Replaced the annotation with existing `InstrumentationRegistry.runOnMainSync` plus a FutureTask whose `get()` propagates failures to JUnit (no added dependency). Keep all assertions intact.

Raw hosted logs/artifact downloads return Azure EOF in this environment. Extend compact diagnostics to include the active border source files and transport the tiny synthetic PNGs only when the current rendering test passes. This is diagnostic transport, not a replacement for executed tests. Also remove unnecessary host-only `assembleDebug`; actual device-test assembly remains inside the emulator lane.

### Android regression passes — e66c8f1, run 36601704025

371 combined test results passed, including **both** new Android methods: 144 pixel comparisons and cache identity/density invalidation. No failures/errors/skips. The app lint reduction remains real; no next issue started. Image export is still missing, so the closure checklist is not complete.

AGP's post-test installation cleanup makes app-private extraction unreliable after Gradle returns. Keep installations on the **disposable verification emulator only** with `-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true` until the existing EXIT capture/launch checks finish. No production configuration or APK debug setting changes. The mock harness now asserts that this flag is actually passed. AndroidX uses the same property in its own build: https://android.googlesource.com/platform/frameworks/support/+/f42a711fe12a08550d75e8ca3bb6d6834db7a7dd/gradle.properties . Actual image export must still be observed before claiming closure.

The focused formatting diagnostics identified nine new test formatting findings and five scalar-argument wrapping findings. Correct those only; older class/other-style formatting debt remains enabled and separately visible. The reference fixture covers changed STAMP/DASHED plus the CLASSIC transition, rather than copying all unrelated rendering branches; coverage is explicitly 144 cases, not all ten styles or full screens.

Immediate checker correction: the first mock-argument assertion exposed a fixture omission (the fake Gradle executable had not written its argument file); five harness cases failed at `4bdc0da`. Correct the fake executable to record arguments, preserving every assertion and real exit-code check. All 40 checker tests then pass. The failed intermediate commit is not release evidence.

### Follow-up — 4358317, run 36603250641

Both border Android tests pass again (371 combined passing results) and focused formatting output has no findings in the three new Kotlin files or new scalar arc arguments. Existing renderer-formatting debt remains, without suppression. The retention flag exposes the completion marker, but diagnostic transport rejects `reference-stamp.png` as missing/empty/over its small 2048-byte cap; the old message did not distinguish those causes. Therefore image closure is still withheld.

Strengthen the Android test: explicitly assert the effective density and require all four nonempty images before writing the completion marker. Improve transport to report exact byte sizes on rejection and give each image its own bounded annotation (16 KiB per file, maintaining the total annotation-count budget). This changes diagnostic transport, not a quality threshold or test assertion. Re-run before closure.


### Verification retrieval blocked — latest source 92484f6

Run 36605281071 started for the stronger density/image checks. GitHub then returned HTTP 401 Bad credentials for the run and check-annotation requests. Its final result is unknown; a watch-process exit code is not accepted as PASS. Stop at this issue. Reconnect GitHub in Arena, retrieve the actual result and image evidence, and resolve any remaining failures before closing or advancing. Latest local checker suite: 41 passed. No release APK offered. Final report changes are documentation/evidence only and remain saved locally while the connection needs attention.


### CLOSED — border allocation issue, verified source 92484f6

GitHub access restored. Retrieved run 36605281071: both stronger Android tests pass (371 combined results; 327 release unit results); release lint DrawAllocation count is zero. All four PNGs and completion marker retrieved. PNG signatures/chunk CRCs and 257×5 dimensions validated; each reference/current pair is byte-identical. Stamp PNGs are 2391 bytes, explaining the previous 2048-byte diagnostic cap; dashed PNGs are 163 bytes. Actual saved files are in `border-92484f6/`; structured evidence is `verification-92484f6.json`. These are software border renders only. Global quality still FAILS; no APK delivery authorized. Only after this closure does work advance to SAF stream ownership.

## Issue 2: SupernoteStorage Recycle / stream ownership (closed below)

Pre-change audit and plan recorded in PROJECT_AUDIT.md. No release build until remaining required gates pass.


### SAF first hosted attempt — a247575, run 36609069275

Host lane passes 360 results, including all nine new stream-ownership cases. Recycle is absent from the high-priority app lint groups. The change introduced five multiline-expression formatting findings in the test and a ReturnCount finding in the writer. Keep this issue open: re-express nullable tree/file/stream resolution as scoped nullable operations with one return, preserve all failure/closure assertions, format the new code, and re-run. Older picker-method formatting/generic-catch debt remains visible and untouched. Add an explicit full-report Recycle count to compact diagnostics; this is reporting, not suppression.


### CLOSED — SAF ownership, verified source d390b4f

Android verification run 36610394275: 360 host and 336 release unit results pass, including all nine ownership regressions in both variants. Debug and release XML summaries explicitly report Recycle = 0. No formatting findings remain in the new test, and no ReturnCount remains in the changed writer; older picker-method findings remain enabled. Detekt total is 2788. App lint total is 466 errors / 14 hints.

Its standalone device lane failed with shell exit 1 and zero reported tests; raw logs returned Azure EOF, so its exact cause is not diagnosed and it is NOT treated as passing. Manual rerun API returned scoped HTTP 403 (actions:write required). The independent delivery workflow 36610394636 ran the same full verifier at the **identical source SHA** and reported **380 passing combined results**, no failures/errors/skips, including all nine SAF tests and both border regressions. Four exported border images still match the saved verified images exactly. This supplies the executed regression evidence without changing permissions or relaxing any gate.

Evidence: verification-d390b4f.json. No claim of real OEM SAF-provider/picker coverage or atomic replacement. Overall gates remain FAILING; release build/smoke/publication jobs are skipped. Next queued issue: audit BookmarkRenderer's legacy ExifInterface usage; no implementation for that issue has started.


## Active issue 3 — platform ExifInterface migration

Pre-change audit: PROJECT_AUDIT.md; baseline source c63e25a has eight app ExifInterface findings. Migrate both BookmarkRenderer and EinkImageProcessor to explicit AndroidX ExifInterface 1.4.2. Preserve rotation-only backgrounds and all eight processed-image orientations; do not silently fix mirror handling or unrelated image/network behaviors in this migration.

Four new Android regression methods exercise the actual two pipelines: sixteen JPEG orientation comparisons against independent pixel-coordinate references; no-EXIF JPEG behavior; PNG transparency/lossless output; invalid/missing source preservation and deep-mode pass-through conditions. Representative expected/current images use the existing private-device evidence mechanism. Compact annotations prioritize the active EXIF set; both border and EXIF sets remain in full artifacts/JSON. Three new checker regressions cover EXIF capture/transport without masking master failure. Local checker suite: 44 passes; unfinished scan 542 files/178 findings/zero checker errors; inventory 617/9. Actual Android execution, ExifInterface=0 and image export are pending. No release assembly authorized.


### First EXIF verification — 8c44b27, run 36626884065

Both debug/release reports explicitly have ExifInterface=0; app lint decreases from 466 to 458 errors (14 hints remain). Release unit results: 336 passed; host 360; full device verifier 384 combined passing results, including all four new image-pipeline methods. Sixteen tagged-JPEG orientation comparisons plus no-metadata JPEG, PNG alpha/lossless and rejection/pass-through behavior execute on Android 35. Four exported 128×160 PNGs validate with correct CRCs and byte-identical reference/current pairs; saved in exif-8c44b27. These are independent pixel-reference vs actual processed-image outputs, not full-screen screenshots.

One new test-only ComplexCondition finding keeps the issue open. Split the two-pipeline artifact-selection expression into explicit when branches with an invalid-pipeline error. All assertions and comparisons remain unchanged. Re-run before closure; do not advance to the next issue.


### Issue 3 CLOSED — final source 6abf29b

Final standalone run **36628443911**, device check **109611293978**, reports **384 combined passing results**, including all four EXIF regressions and the 16 orientation comparisons. Release check **109611294088** reports **336 passing** results. Standalone host check **109611294295** fails with zero tests/no lint; the available annotation truncates a `java.util.zip.ZipException` and does not identify the archive. Independent same-SHA delivery run **36628444279**, host check **109611299996**, reports **360 passing** results. Its device check **109611299564** fails with shell exit 1 and zero reported tests. Neither unsuccessful attempt is counted as passing. Counts overlap.

Debug/release **ExifInterface = 0**; app lint **458 errors / 14 hints**. Detekt **2788**, back to pre-migration count, with no EXIF-file findings in retrieved focused formatting/Detekt diagnostics. The introduced test ComplexCondition is removed; assertions are unchanged. Both workflows remain FAIL overall, and signed release stages are skipped.

Saved `verification-6abf29b.json` and `exif-6abf29b/` (four 128×160 PNGs plus marker). Validated all PNG chunk CRCs, current/reference byte equality, and equality to the first-run exports. Viewed both current images. References reconstruct old orientation semantics, not old-APK/full-screen screenshots; no performance claim. Local checker suite remains **44 passed**, index **617/9** unchanged. Background mirror omission, memory/atomic-save policy and API 24/OEM-format coverage remain open.

**Next bounded task:** audit background mirrored-EXIF tags 2/4/5/7 before implementing a correction. No follow-up implementation or release assembly has started.


## User-requested next-agent work order — 2026-09-29

Added `NEXT_AGENT_WORK_ORDER.md`, `REMEDIATION_BACKLOG.json` and `ISSUE_TEMPLATE.md`, linked from AGENTS/README/CLAUDE and the old continuation. This is planning only; no new production issue is claimed fixed. The ordered plan covers complete occurrence reconciliation, known and omitted lint groups, Detekt/format/compiler/unfinished debt, state/navigation/WebView, actual crawler/backend and external-AI integration, all control/visual/performance evidence, and gated production release.

**Immediate next action changes to S00 (inventory prerequisite)** because the retained app report omits 28 groups and a new full artifact download again failed with storage EOF. Recover full reports and map every occurrence before calling the 400+ backlog exhaustive. The next **production correction** remains S01, mirrored-background EXIF; no implementation started. WIP is one root-cause ticket, with executed final-source closure before the next. Full project status remains PARTIAL/release BLOCKED.


### S00 active — recovery infrastructure, not a production fix

Normal artifact download and alternate client fail. Added a standalone read-only recovery workflow for original source 6abf29b/run36628444279; existing quality/release gates unchanged. Full static XML/JSON/log payloads use bounded gzip/base64 pages and source/run/artifact/file checksums. Missing/duplicate/truncated/mixed/oversized data and unsafe paths are rejected. 55 local checker tests pass; unfinished remains 178. Actual hosted recovery still required before closing transport or S00. No S01 work started.

### S00 transport CLOSED; raw inventory recovered, semantic reconciliation still OPEN

Read-only recovery run **36635551405** passes all ten jobs at tool source0778e68. All five bundles validate source6abf29b plus page/payload/file checksums. Preserve five failed recovery attempts (4096-character truncation, 50-annotation job cap, original formatter plugin-resolution failure, and formatting-size overflow) in S00-transport.md. None was counted as a complete recovery.

Saved `recovered-6abf29b/{README.md,manifest.json,occurrences.json}`. Complete app lint34groups/458errors/14hints; Detekt2788 cross-checked against SARIF; **13875** formatting occurrences reconciled against eleven report footers; unfinished178. Raw rows17890 include overlapping reports/tools; no inflated unique-bug count. Added strict parser with seven tests; **66 total checker tests pass**, repeat export byte-identical; scanner remains178findings at546files.

**Next:** `issues/S00-ownership.md` — stable semantic tickets, collision-aware aliases, all occurrence/scenario ownership and security-priority review. Do not start mirrored EXIF or other production work yet. S00 remains PARTIAL; release remains BLOCKED.

## FACTORY-001 bounded infrastructure CLOSED — production debt remains open

User-requested development factory added58 distinct engine integrations. Hosted
run36662473189 at25f9401 executes50:20PASS/9REPORTED/21FAIL, zero blocked,
unchanged source fingerprints. Eight native/APK-only entries are not counted
as executed.88 local checker tests pass; actual backend43/JS13/property3 pass.
Provisioning, fixed argv, bounds, source guards, fresh report validation and
compact receipts are implemented. Findings remain failures, not baselines.

See factory/VERIFICATION.md (under docs) and its checked50-record export. Earlier
TLS, missing-prerequisite, argument-parsing and encoding setup failures were
retained and corrected. No Android/runtime dependency or canonical gate changed.
Triage produces3,509 provisional packets preserving17,890 raw observations.

Next: S00 semantic ownership with security review, including2 redacted Gitleaks
candidates. Not S01 or a build. Unfinished178, missing visual/performance/device
closure and all existing native/backend/signing requirements remain unresolved.

## FACTORY-002: implementation verified, effort acceptance open

User requested more automatic checks and a visible tool/repository/ownership map.
Delivered64 registered engines,56 actual hosted executions at1171a4c/run36665725670
(24PASS/9REPORTED/23FAIL), six new engine integrations, real Chromium behavior,
a11y findings, change-based local execution and eight report adapters.105 checker
tests pass.479 local aliases become156 provisional packets with no data closure.
No measured70–80% savings yet; use at least10 comparable real paired work items.

Next: S00 security ownership review using the factory; assess npm advisories and
record the first paired routine-effort observation. Not another infrastructure
catalogue, speculative bulk patch, S01 correction or premature APK. Canonical
gates remain unchanged and release remains blocked.

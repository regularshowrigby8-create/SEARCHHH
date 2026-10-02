# Baseline verification

Date: 2026-09-29. Working tree was clean at `dbd9366c6ab57cdd17d503fa70555a94ccb5a1d6` when these commands ran, before quality implementation.

All commands ran in `/home/user/SEARCHHH`. These are actual attempts, not inferred results. Missing Java is an environment limitation; it does not establish whether baseline source compiles or fails. Connected tests were attempted as well, despite no adb/device being available.

### `./gradlew assembleDebug`

Exit: **1**. Result: **BLOCKED (environment, before project configuration)**.

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
```

### `./gradlew test`

Exit: **1**. Result: **BLOCKED (environment, before project configuration)**.

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
```

### `./gradlew lint`

Exit: **1**. Result: **BLOCKED (environment, before project configuration)**.

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
```

### `./gradlew connectedDebugAndroidTest`

Exit: **1**. Result: **BLOCKED (environment, before project configuration)**.

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
```

### `adb devices`

Exit: **127**. `adb: command not found`. No local install, launch, logcat or screenshot evidence.

## Prior remote evidence (not a substitute)

Existing five-job workflow run 36518594817 passed on f643e19 (the baseline production tree; subsequent dbd9366 was documentation-only). It tested a narrower legacy rule set, with a lint baseline and no strict format/Detekt/unfinished/interaction gate. Its optional public relay timed out. None of that proves the new quality requirements pass.

## Enforcement baseline

`gh api repos/regularshowrigby8-create/SEARCHHH/branches/main/protection` returned HTTP 403, exit 1: `Resource not accessible by integration`. Administrator activation is blocked. Do not request credentials in chat; administrator must configure protection using their authorized GitHub connection.


## Release delivery correction — 2026-09-29

Local `./gradlew --continue searchhhVerification -PuniversalApk` again exited 1: Java/JAVA_HOME absent; no local SDK/device execution claimed. Hosted source `0f92445`, run 36583626253, executed `:app:verifyReleaseRuntimeDependencies :app:testReleaseUnitTest :app:lintRelease :app:assembleRelease` with `--continue`: 327 release app unit tests passed; release runtime guard passed; R8 and release lint failed. The new guard's first attempt at `1620f1d` failed on Android artifact variant ambiguity and was corrected to inspect component identities rather than artifacts. These are real failed build attempts, not successful release evidence. Signing/production APK device smoke remains blocked behind the quality gate and unconfirmed secure signing configuration.

### R8 continuation baseline

Source `b709d86`, run 36585651804: release unit tests 327 passed; `:app:minifyReleaseWithR8` fails for CheckReturnValue, Immutable, RestrictedApi, ManagementFactory and RuntimeMXBean; `:app:lintRelease` fails with 470 errors. Local master exits 1 (Java unavailable), Python checker tests 33 passed. Exact warnings copied into the R8 continuation evidence file before the compatibility changes.

## Sequential border remediation — 2026-09-29

Before edits at `5896283`: `./gradlew --continue searchhhVerification -PuniversalApk` exits 1 because neither Java nor JAVA_HOME is available locally. This is an environment block, not a successful Android check. 35 local Python checker tests passed. Hosted baseline 36596759905 confirms two DrawAllocation findings in the actual toolbar separator. Work is limited to that issue; all other failing quality gates remain enabled and block delivery.

First attempt `b7ca3e8`, run 36599950711: lint 468 errors (two fewer), 351 host tests pass, but Android test compilation fails on unavailable UiThreadTest annotation. It is not rendering verification. Follow-up `e66c8f1` replaces it with existing main-thread instrumentation and explicit FutureTask failure propagation. Local checker suite now passes 40 tests. No dependency added and no release assembly requested.

## SAF stream-ownership baseline — after border closure

Resumed at source 92484f6 with GitHub access restored. Retrieved run 36605281071 and decoded/validated the four real border PNGs: both pairs byte-identical, valid CRCs, 257×5. Both stronger border Android tests pass; release DrawAllocation count is zero. Only then began SAF ownership remediation. Local checker baseline 41 passes, inventory 617/9; local `./gradlew --continue searchhhVerification -PuniversalApk` exits 1 because Java/JAVA_HOME is absent. Existing caller already closes its Pair-returned stream; the next change makes ownership scoped and directly testable rather than claiming an observed leak.


## SAF ownership verification — d390b4f

Run 36610394275: 360 host and 336 release unit results pass; both app lint reports explicitly have Recycle=0, with 466 app lint errors/14 hints remaining. New test formatting and changed-writer ReturnCount findings resolved; Detekt remains failing with 2788 issues. Its standalone device lane reported zero tests and shell exit 1; raw log download EOF prevented diagnosis, and manual rerun returned scoped actions:write HTTP 403. Independent delivery run 36610394636 at the same source executed the full device verifier and passed 380 combined results (including all nine SAF tests and both border tests). Counts overlap and are not additive. No production release jobs ran. Full evidence in quality/verification-d390b4f.json; no rule or permission was weakened.

## EXIF migration baseline — c63e25a

`python3 -m unittest discover -s tools/quality/tests -q`: 41 pass. `python3 tools/quality/interactions.py --check`: 617/9, current but incomplete. `./gradlew --continue searchhhVerification -PuniversalApk`: exit 1, no Java/JAVA_HOME. Retrieved run 36612274354 host check 109556416947 confirms 8 ExifInterface findings. Relevant source/routes/dependencies and behavior differences audited before changes in PROJECT_AUDIT.md.

## Work-order preparation baseline — 2026-09-29

Source/document checkpoint: `77fbeef55d5ea9c28fa0c0d2c097d2b2081eaba4`; runtime verification remains `6abf29b`.

- `python3 -m unittest discover -s tools/quality/tests -q`: exit 0, 44 tests pass.
- `python3 tools/quality/interactions.py --check`: exit 0, 617 candidates / 9 contracts; not complete behavior coverage.
- `python3 tools/quality/unfinished.py`: exit 1, 542 files / 178 findings / zero checker errors.
- `./gradlew --continue searchhhVerification -PuniversalApk`: exit 1 before configuration, Java/JAVA_HOME absent. No new Android execution claimed.
- `gh run download 36628444279 -n android-build-test-lint-36628444279 -D /tmp/searchhh-handoff-host`: exit 1, artifact storage EOF. Artifact listing succeeds; this is not a GitHub authentication failure. Do not retain signed artifact URLs in tracked documents.

Plan: add linked work order, verified-seed backlog and issue template; preserve strict policies and existing CI/release behavior. Validate links/JSON and rerun local documentation-relevant checks before pushing.

## S00 transport pre-edit baseline — 2026-09-29

At 030984f: checker suite 44 PASS; index 617 candidates/9 contracts PASS; unfinished 542 files/178 findings/zero checker errors FAIL. Local master exits 1 before configuration (Java/JAVA_HOME absent). `gh run download 36628444279 -n android-build-test-lint-36628444279 -D /tmp/s00-host` fails storage EOF; alternate client URLError. Existing GitHub repository/run APIs work. No production edit and no report recovery yet. Planned negative-tested, read-only annotation transport; no release authorization.

## S00 actual recovery and raw export — 2026-09-29

Recovery source **0778e68**, run **36635551405**, all ten jobs PASS. Decoder reconstructs all five groups and validates full page/source/run/file/payload hashes. Original report source is **6abf29b**, not the tooling SHA. Original report runs still FAIL overall. Formatting is from executed standalone36628443911; delivery36628444279 formatting failed at Gradle plugin resolution and is not counted as a report.

Strict raw export: app debug/release each458errors/14hints (34groups), library debug53/52; Detekt2788 XML entries independently match SARIF; eleven ktlint text reports contain **13875** occurrences with exact per-rule footer reconciliation; unfinished178. Total raw diagnostic rows17890 preserves duplicate-report/tool overlaps and is not unique bugs. Snapshots:617interaction candidates/9contracts/42scenarios (38declared blockers). Repeat export is byte-identical; manifest records SHA256s.

Current local checks after transport/export tools: **66 checker tests PASS**, index617/9 PASS, unfinished546files/178findings/0checkererrors FAIL. No new scanner findings. Local Android master remains blocked by absent Java. S00 semantic ticket ownership/alias reconciliation remains incomplete; raw recovery does not authorize S01 or release.

## Factory pre-edit baseline — 2026-09-30

At3949290:66 standard-library checker tests PASS; interaction index617/9 PASS; unfinished546files/178findings/0checkererrors FAIL. Local full master exits1 before configuration: Java/JAVA_HOME absent. Node22.22.3, Python3.11.2, git/gh/ripgrep available; uv/Go/Java/Docker/ShellCheck/shfmt absent from PATH. GitHub history/index alignment after an ancestry check preserved all files and yielded clean status before edits. New infrastructure does not certify existing production debt.

## Factory local execution checkpoint — 2026-09-30

84 checker tests PASS (66 existing plus18 real factory regressions). The first
50-job local batch at `build/reports/factory/20260930T023600Z-fc8034` recorded
14PASS/6REPORTED/23FAIL/7BLOCKED:43 subprocess executions, **not43 clean checks**.
Subsequent binding corrections verified mypy/Pylint/Pyright/docstrings/schema,
property tests, formatter and spelling. Strict backend tests now pass with the
Starlette-required `httpx2` development test client; no backend requirement was
changed. JavaScript:5 suites/13tests PASS; property tests:3 PASS.

Original actionlint wrapper provisioning failed upstream download. Replaced it
with a digest-pinned official binary, not another tool. All24 remaining Python
environments and pinned npm dependencies installed. Four initial official
binary downloads failed TLS EOF locally; hosted provisioning remains pending.
Local43-tool availability is not hosted execution evidence.

Interaction index617/9 PASS. Unfinished553files/178findings/0checkererrors FAIL,
unchanged debt after correcting a new process-reaping finding. Full Android
master remains Java/JAVA_HOME-blocked before configuration. Native/release gates
and production files are unchanged. Next: hosted50-tool execution and final
compact receipts/handoff; no APK or release approval.

## Factory final executed checkpoint — 2026-09-30

At25f9401, run36662473189 executes50/50 selected engines:20PASS,9REPORTED,
21FAIL,0BLOCKED. All source fingerprints are unchanged; page/ID/source and
selected-tool reconciliation passed. All provision/checker-test lanes succeeded;
failing diagnostics keep the overall workflow red. Full receipts and earlier
failed attempts are retained in docs/factory/VERIFICATION.md. No native/APK
factory execution is inferred for the eight excluded prerequisite-only jobs.

Local88 checker tests PASS; interactions617/9 PASS; unfinished553files/178findings/
0errors FAIL. Master exits1 before configuration: missing Java/JAVA_HOME. Prior
strict backend43, JS13 and property3 tests passed and were re-exercised by hosted
factory steps. Cold workspace restoration removed ignored tool caches; installed
availability must be re-established rather than inferred from this evidence.

GitHub reconnection restored access. Verified every published file against
remote1d49385 (zero missing, exactly four known local modifications) before
restoring only local Git history/index and publishing25f9401 without force.
Production paths and canonical release workflows remain unchanged from3949290.

Handoff-only checks after cache restoration:88 tests PASS; interactions617/9
PASS; unfinished553/178/0 FAIL; docs formatter PASS. A schema-check retry first
reported BLOCKED because its isolated environment was absent; explicit locked
provisioning of only check-jsonschema restored it and the actual scan passed.
Raw artifact download still returns storage EOF; bounded hosted receipts were
retrieved and reconciled successfully through the Checks API.

## FACTORY-002 pre-edit baseline — 2026-09-30

At6f94604:88 checker tests PASS; interactions617/9 PASS; unfinished553/178/0
FAIL; Android master exits1 before configuration because Java/JAVA_HOME is
absent. After restoration, verified every published file against the remote
(no differences/missing files) and restored only local Git history/index. No
source was overwritten. Six-engine expansion is user-authorized WIP1; S00 open.

## FACTORY-002 executed verification —2026-09-30

Published3f061ae run36665053481 and repeated1171a4c run36665725670 both execute
56/64 engines:24PASS/9REPORTED/23FAIL,0BLOCKED, unchanged source fingerprints.
All six new engines execute. Playwright/OpenAPI/pipdeptree/Taplo PASS; axe FAIL
with11 records, npm audit FAIL (local3 vulnerabilities:1moderate/2high).
105 checker tests PASS; jsdom and actual Chromium control behavior PASS. Real
local normalization preserves479 aliases in156 provisional packets,0parser
errors; all six failed jobs remain failed. Receipts reconcile page/ID/source
coverage. No productivity percentage or semantic closure inferred.

Unfinished557/178/0 FAIL; interaction617/9 current but incomplete. Master remains
Java-blocked locally. Browser download TLS and artifact storage EOF block local
image inspection; hosted screenshots were produced, not manually reviewed here.
App/runtime files and canonical workflows unchanged versus6f94604. Measurement
returns NOT_MEASURED/exit2 with the empty ledger. Routed docs checks really ran
and preserved2FAIL/1BLOCKED rather than disguising missing local prerequisites.

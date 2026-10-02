# Next-agent work order: sequential quality repair and construction

**Issued:** 2026-09-29. **Overall status: PARTIAL; release BLOCKED.**
**User mandate:** resolve issues sequentially before the next Searchhh release; deliver no debug APK or test-report bundle in place of the release.

This is an execution instruction, not a claim that the backlog is fixed. Read it with [the policy](../QUALITY_RULES.md), [the implementation report](../QUALITY_IMPLEMENTATION_REPORT.md), [the queue](REMEDIATION_BACKLOG.json) and [the issue template](ISSUE_TEMPLATE.md). Policies are not waived by this plan. The queue tracks ordered work packages; it is **not** an invented list of 458 individual errors. Historical continuation notes are evidence, not current assignments.

## Current factory expansion and measurement acceptance

FACTORY-002 infrastructure is verified at1171a4c/run36665725670:64 registered,
56 executed (24PASS/9REPORTED/23FAIL). All six additions actually ran. Open
[the complete factory map](../factory/FACTORY.html),
[all64 repositories/responsibilities](../factory/FLEET.md) and
[executed results/gaps](../factory/EXPANSION_RESULTS.md).

The70–80% routine-effort target remains OPEN and unmeasured. S00 now has one
active production ticket: [HTTP/WebView security](issues/S00-http-webview-security.md).
It adds default-off HTTP approval and blocks mixed content, but targeted Android
execution is blocked by missing Java/JAVA_HOME. Verify that ticket before any
second production repair. Also inspect the new npm advisories during S00 review,
record the first comparable paired effort item, and do not infer savings from
fewer packets. Canonical gates, WIP1 and no-premature-release requirements remain.

## Factory available — resume S00, not a production build

[FACTORY-001](issues/FACTORY-001.md) closes the bounded infrastructure request:
58 declared engines, 50 executed in run36662473189, with 20PASS/9REPORTED/21FAIL.
Read [the evidence](../factory/VERIFICATION.md) and
[the guide](../factory/README.md). Reports, dependencies and test cases are not
extra tool engines. Native/APK-only factory jobs remain prerequisite-dependent.

First inspect the two redacted Gitleaks candidates and other security results;
map them to reviewed semantic tickets without exposing credentials. Then use
`python3 -m tools.factory.factory triage` to generate3,509 provisional file/rule
packets preserving17,890 recovered observations. Every raw occurrence still
needs accountable ownership and duplicate aliases. No auto-fix or mass closure.
On restored hosts, doctor/provision again: ignored build caches may be absent.

## Current S00 checkpoint — recovery completed

Read [the recovered baseline](recovered-6abf29b/README.md) and [its manifest](recovered-6abf29b/manifest.json) first. Recovery run **36635551405** succeeded; all five report groups reconstruct at original source `6abf29b`. All 34 app lint groups are available; the historical 28-group omission described below is resolved. Actual formatting debt is **13,875 occurrences**. The compact export contains **17,890 raw diagnostic records**, with overlaps deliberately preserved—not that many unique bugs.

**Immediate next action is S00 semantic ownership/deduplication**, not another blind download retry: populate stable root-cause tickets and cross-report aliases from the verified export, and review newly visible security findings before confirming production priority. Follow [S00-ownership](issues/S00-ownership.md). The remaining sections retain the original plan/baseline context; S00 is not closed, and no production correction has started.

## 1. Start here — exact next assignment

1. Work in `regularshowrigby8-create/SEARCHHH`, on **`arena/01a0ea36-searchhh`**. Do not switch/create branches or force-push. Inspect `git status`, `git log -5`, and the remote tip before editing; preserve other people's changes. An unexpected initial-commit checkout with thousands of untracked existing files is a metadata discrepancy to investigate, not permission to add/rewrite the whole repository.
2. Read `AGENTS.md`, `docs/QUALITY_RULES.md`, the latest sections of `PROJECT_AUDIT.md`, `BASELINE_VERIFICATION.md`, `QUALITY_IMPLEMENTATION_REPORT.md`, and `SEQUENTIAL_REMEDIATION.md`. Read `docs/APK_DOWNLOADS.md` before any packaging work.
3. Claim **S00 — reconcile the complete current diagnostic inventory** in the queue. No production correction is active at handoff. The first subsequent code task is **S01 — background mirrored EXIF**; its audit was queued, not implemented.
4. Execute and record the local baseline below. Recover full current hosted reports or execute the same checks in a working JDK/SDK environment. Populate occurrence records before claiming the entire 400+ backlog is enumerated. Do not infer omitted rules from an old lint baseline.
5. Finish S00, then take one concrete root-cause ticket from S01. Audit → regression → minimal fix → verification → evidence → closure. Only then start the next ticket. If blocked, record the exact failed command/symbol/evidence dependency and stop that sequence; do not mark it done to work around the blocker.

### Established checkpoint — do not regress or redo closed fixes

Historical evidence source **`6abf29b57982b93fe929674c70230bb17f6940cf`**; reconciled repository checkpoint is the current branch tip and must be verified from Git history before reuse. New documentation commits are not new runtime test evidence.

| Evidence | Observed result | Interpretation |
| --- | --- | --- |
| Standalone run `36628443911`, device check `109611293978` | 384 combined results passed | Includes JVM results; not 384 unique device tests |
| Same run, release check `109611294088` | 336 unit results passed | Release lane still fails lint; not an approved APK |
| Same-SHA delivery `36628444279`, host `109611299996` | 360 results passed | Independent host evidence; totals overlap other lanes |
| Standalone host `109611294295` | Zero tests; truncated ZipException | Failed attempt, not a pass; exact archive unknown |
| Delivery device `109611299564` | Zero tests; shell exit 1 | Failed attempt, not a pass |
| Both workflows | FAILURE | Signed release stages skipped |

Closed bounded fixes: **border allocation** (144 software pixel comparisons/cache tests), **SAF stream ownership** (nine regressions), **AndroidX EXIF migration** (four Android methods, 16 orientation comparisons). `DrawAllocation`, `Recycle`, and `ExifInterface` counters are zero. See `verification-{92484f6,d390b4f,6abf29b}.json`; retain these regression suites. Border/EXIF artifacts are small deterministic renders, not whole-app before/after screenshots or hardware performance results.

## 2. What “all 400+ issues” means

The verified app report has **458 errors and 14 hints**. Other independent debt: **2788 Detekt weighted findings**, formatting/compiler checks, **178 unfinished-source review candidates**, and **617 interaction candidates with only 9 contracts**. Do **not** add module/variant counts or subtract contracts from controls to invent a count of unique bugs. Findings can share a root cause; one root fix can legitimately resolve multiple occurrences, but closure must account for each occurrence.

The app summary currently exposes `MissingTranslation` 92, `Typos` 3, `UseKtx` 83, `InlinedApi` 4, `NewApi` 3, and `TrimLambda` 5 hints. That is **185 identified error occurrences; 273 error occurrences and 9 hints are not individually described by the retained summary**. The summary omits **28 rule groups**, potentially including non-error severities. Library summaries also report dependency/catalogue/target-API findings. Counts are from source `6abf29b`, not a promise about the next run.

### S00 inventory procedure — no silent remainder

1. Obtain **complete** reports from the same source SHA: all app/library debug and release lint XML, Detekt XML/SARIF, ktlint reports, compiler logs, unfinished JSON, interaction inventory/contracts and required scenarios. Record tool versions, source SHA, workflow/run/job, command, exit, report path and SHA-256. Preserve logs outside tracked source; commit compact normalized evidence only. Never commit secrets, signed artifact URLs, Gradle caches, downloaded archives or APKs.
2. Read every lint `<issue>` including secondary locations; count each issue element once per raw report. Normalize runner prefixes, but retain module/variant/report provenance. Keep raw per-report totals. Assign a stable ID from tool + rule + repository path + enclosing symbol/resource key + normalized message. Lines are navigation hints, not identity. Resolve collisions explicitly, not by dropping duplicate-looking entries.
3. Create `docs/quality/issues/<stable-id>.md` using the template. For repetitive findings, one reviewed root-cause ticket may list multiple occurrence IDs and exact paths/keys. Keep an index `docs/quality/issues/index.json` with each occurrence, its owning ticket and status. Duplicate debug/release/dependency reports link to one root cause with all report occurrences preserved. Do not confuse a secondary location with another root cause.
4. Reconcile raw totals and severities to complete reports (**458 errors / 14 hints at the checkpoint**). If the source/tool version differs, save a new baseline and explain each delta; never force counts to the old number. Give **every** lint rule, including the omitted 28 groups, a ticket and phase. Add all Detekt/format/compiler/unfinished findings and each unmet required scenario to the same index with separate tool/category fields.
5. Rank real crash, data-loss, secret exposure and unsafe navigation ahead of mechanical work. Within a phase: severity → dependency prerequisites → smallest root cause → stable ID. If new evidence requires a priority change, record why and update the queue before proceeding. Do not start several fixes in parallel.
6. S00 closes only when totals reconcile, every occurrence is owned, unimplemented features have explicit tickets, and duplicates/secondary locations are explained. **Partial annotations are a useful seed, not exhaustive inventory.** No invented locations, counts or “all clear” inferred from annotation silence.

**Report-access fallback:** use `gh run download` first. This environment last returned storage EOF, despite working GitHub API access. Try a working local/hosted SDK environment or the existing uploaded reports through an accessible route. If necessary, make diagnostic transport its own audited, tested infrastructure ticket: paginate a compact complete index with source/report hashes, page index/total and reconstructed-total checks; test missing/truncated pages and preserve failing exits. Do not raise thresholds, suppress findings or add successful fallbacks. If full evidence still cannot be recovered, S00 remains BLOCKED; request the missing report/access, not credentials. Authentication failures require reconnecting GitHub in Arena. Manual rerun has previously returned `actions:write` 403; do not endlessly retry or weaken permissions.

## 3. Mandatory work-in-progress limit and closure protocol

**WIP = one root-cause implementation ticket.** A phase can have many sequential tickets; “fix all lint” is not an acceptable single ticket. Helpers may inspect or review, but no concurrent production patches for different issues. All contributors read the same policy; repository text cannot force agreement or protect against an administrator bypass.

Statuses: `queued → auditing → reproducing → implementing → verifying → closed`. `blocked` and `reopened` remain unresolved. Record the owner/role and next action. Never move straight from implementing to closed.

For **every ticket**:

1. **Audit first.** Inspect rule explanation, exact source/callers, reachable UI, min/target API behavior, dependencies, cancellation/error/durable-write paths and existing tests. Save audit and command baseline before edits. Declare allowed files, intended behavior and non-goals.
2. **Reproduce.** Save the current diagnostic or failing behavior. Write a regression on the real boundary; when practicable run it red before fixing. For mechanical changes, retain reproducible rule failure and unchanged behavior tests instead of fabricating a behavioral defect. If local execution is unavailable, label it blocked and use hosted execution; code review alone is not a pass.
3. **Fix minimally.** Preserve architecture, names/IDs, routes, stored data and licensing. Reuse existing implementations first. Add a dependency only after checking the catalogue and upstream compatibility. No broad rewrite/autoformat/upgrade mixed into a semantic correction.
4. **Verify locally and on Android as applicable.** Run the changed regression, impacted module's tests/lint/format/Detekt, then the full master and hosted release lanes. For controls, perform the action and assert observable state/navigation/persistence; a displayed label or unused tag is insufficient. Do not delete assertions, skip tests, swallow exceptions, add a debt baseline or hide warnings.
5. **Compare identities, not just totals.** All owned findings must disappear for the right reason and no introduced finding may remain. Preserve prior regressions. An unchanged total can hide one new and one removed finding. Known unrelated failures stay listed and assigned; they prevent overall PASS, but do not prevent evidence-backed closure of a genuinely bounded fix.
6. **Close only on executed final-source evidence.** Save source SHA, exact run/job/commands, fresh non-skipped test outcomes, full relevant report comparison, visuals/hashes if needed and limitations. Code changed after a failing run must be reverified. Same-SHA independent runs may establish bounded behavior, but retain failed attempts; final release requires all mandatory jobs succeeding, not a mosaic of red workflows. A docs-only follow-up can reference its tested code SHA without pretending it was re-executed.
7. Update the issue index, queue, `SEQUENTIAL_REMEDIATION.md`, audit/baseline as needed, `QUALITY_IMPLEMENTATION_REPORT.md` and release status. Commit with the policy trailer and push the session branch. **Do not issue a release APK between tickets.** Then select the next ready ticket.

## 4. Ordered repair playbook

The queue contains dependency IDs. Use full reports from S00 to attach every occurrence; the order below does not imply omitted diagnostics can be ignored. Safety-critical evidence can justify an explicitly documented reordering.

| Phase | Concrete work and guardrails | Required closure evidence |
| --- | --- | --- |
| **S01 Image correctness** | Audit `BookmarkRenderer.saveStartPageBackground` and the actual picker caller for mirrored EXIF tags 2/4/5/7. Define correct TIFF coordinate transforms; distinguish rotate/flip/transpose order. Leave web tone/dither/codecs, sampling, atomic-save policy and normal tags unchanged unless separately audited. Do not just copy a matrix without coordinate proof. | Add red regression for each mirrored background case, then all eight tags in both actual pipelines. Keep missing-EXIF, PNG alpha/losslessness, rejection/preservation tests. Existing tests deliberately assert old background mirror omission: update only those expectations to independently derived correct coordinates, retaining old behavior as historical evidence. Save new current-vs-correct-reference images and document the intentional visual delta. |
| **S02 API and safety** | Resolve `NewApi` (3 known) and `InlinedApi` (4 known) and any higher-risk findings uncovered in S00. Start at `values-night/styles.xml` and `BrowserActivity`; inspect all reported variants/locations. Isolate newer resource attributes into correctly qualified resources with complete old-device fallback; guard runtime APIs with supported compat APIs or real SDK checks. Do not raise minSDK 24 to silence lint. Audit actual resource/theme inheritance rather than blindly moving a style. | API-appropriate tests on minimum-supported and threshold devices where behavior differs, plus API 35; light/dark resource resolution and theme behavior. Crash/data-loss/security fixes need failing-before/passing-after tests. API-35-only results cannot certify API-24 behavior. |
| **S03 Existing dead actions** | Trace system-TTS pause/resume through `TtsViewModel` and `WebSpeechHandler`, active engine and UI. Implement a truthful chunk/offset/utterance-state contract if system TTS cannot natively pause; handle stop, completion, engine switch and lifecycle. Do not set a “paused” flag while audio continues or restart already completed text silently. Audit other live no-op controls individually. | Action → real playback/state consequence with deterministic engine fixture and actual device smoke; repeated pause/resume/stop/races/recreation. Label an intentionally unsupported action visibly and test disabled state, but do not disable requested features merely to close a ticket. |
| **S04 Resources and translations** | Resolve all 92 known `MissingTranslation` and 3 `Typos` occurrences, then other resource rules. One resource family/locale per ticket. Preserve keys, format argument index/type/count, plurals, escapes and accessibility meaning. Translate actual user-facing text with review; do not copy English everywhere or mark UI strings non-translatable to quiet lint. A genuine brand/protocol token needs a documented semantic reason. | `scripts/check_locale_strings.py`, affected debug/release lint, resource compilation, placeholder/plural/locale-switch tests and visible layout checks (long text, supported scripts/RTL). No lost keys or fallback regression. |
| **S05 Kotlin/API idioms** | Resolve 83 known app `UseKtx` occurrences, library overlaps and 5 `TrimLambda` hints one file/call pattern at a time. Check existing KTX dependency first. Preserve transaction commit/apply timing, cursor/stream ownership, lifecycle/cancellation and exception semantics. Prefer supported exact overloads; do not mechanically replace calls with different behavior. | Targeted semantic tests for affected boundaries, unchanged persistence/order/error behavior, exact rule absence across all owning reports. Cover all remaining style/idiom lint groups from S00, not only these examples. |
| **S06 Build/dependency compatibility** | Resolve `OldTargetApi`, `GradleDependency`, `NewerVersionAvailable`, `AndroidGradlePluginVersion`, `UseTomlInstead` and other actual dependency findings. Centralize duplicate versions without changing resolution first; then one dependency or inseparable compatibility set per ticket. Check upstream changelog, licenses, ABI/minSDK, Kotlin/KSP/Compose/AGP/Gradle/JDK compatibility. Security advisories can move a ticket earlier. Do not bulk-upgrade to “latest” or add libraries for count. | Resolution graph before/after, compile + JVM + Android regressions, release runtime guard and R8. For toolchain sets, document why versions must move together. Keep minSDK/app identity/signing behavior. Independently review exact existing Ktor/annotation compatibility exceptions, not global suppression. |
| **S07 Detekt** | Work through **every actual rule/finding** in the full 2788-finding report. Prioritize swallowed/broad exceptions and resource/lifecycle errors, then complexity/long methods/nesting, naming/magic numbers and remaining rules. Extract responsibility-specific helpers preserving ordering/threading/visibility; name constants by domain, not meaningless numbered aliases. Catch only recoverable known exceptions; preserve coroutine cancellation and primary/suppressed causes. Changing maxIssues or excluding files is forbidden. | Before/after per-rule/per-symbol comparison and behavior tests. For pure extraction, unchanged output/order/error/cancellation assertions. Review all files changed by extraction. `maxIssues: 0` remains enforced. Preserve legitimate exact exceptions with owner, rationale, review/removal condition and regression. |
| **S08 Formatting/compiler completeness** | Remove remaining ktlint findings in small file-scoped mechanical tickets after behavior is stable. Do not run whole-repo autoformat during a semantic fix. Manually review formatter effects on strings, generated JS and DSLs. Resolve compiler warnings as real compatibility/typing problems, never disable warnings-as-errors. Handle any leftover lint groups explicitly assigned here; unknown groups must already have a rule-specific plan from S00. | Clean affected reports and compilation; full tests/master still run. Final rule census must show no unmapped diagnostic or new warning. Retain upstream notices. |
| **S09 Unfinished/source completeness** | Review each of the 178 scanner candidates by call path, not just token. Implement reachable unfinished behavior; remove genuinely unreachable code only with caller/reference proof; preserve required features. Broad catches, empty returns, empty overrides, JS placeholders and backend gaps need behavior tests. Do not delete TODO text while leaving a stub. | Every fingerprint disposition and executed regression. Legitimate framework no-ops require exact documented exceptions with owner/expiry/tests, not an allowlist for debt. A candidate owned by S10–S14 is transferred to a named ticket and remains unresolved; S09 triage is not a claim of zero unfinished findings. |

## 5. Ordered functional construction — not catalogues or mock completion

Keep existing Kotlin/Compose/WebView/Room architecture. Audit what exists before building; prefer maintained existing code and minimal integration glue. Each row is multiple sequential vertical slices, each with a real user action → state holder → repository/service → durable/observable result, error path and cancellation test. Link every slice to current control IDs and required scenarios. No decorative buttons, invented destinations or empty handlers.

| Phase | Construction sequence | Acceptance tests |
| --- | --- | --- |
| **S10 State, identity and navigation** | Central route/ID handling → state/repository event flow → search Start → Stop/cancellation → filters → stable result details → persistent saves/history → drawer/back/unknown route → loading/empty/error/retry/offline/expiry/duplicates → restoration and theme persistence. Inspect `SearchhhActivity`, `SearchhhData`, `SearchhhRoute`, existing Room/WorkManager/backend ownership before changing boundaries. | Real tagged actions followed by changed query/job state; Stop cancels owned workers and prevents late-result resurrection; filters change results; save survives restart; IDs survive navigation and duplicate merging; retry preserves intent without duplicate work; offline recovery and expired results deterministic; drawer closes and Back resolves correctly. |
| **S11 WebView** | HTTPS default/explicit HTTP approval → reject unsupported schemes → retain SSL cancellation → origin-limited bridge or remove unnecessary privilege → navigation/back/forward/reload/close → safe reuse/restoration/download/error behavior. Reuse AndroidX WebKit feature detection, not a Chromium build. Keep debugging gated only by `BuildConfig.DEBUG`, never a user preference in release. | Real WebView fixtures for redirects, cross-origin frames, malformed/unsupported URLs, untrusted certificates and bridge calls; navigate/reload/close and assert effects. Test privileged URL/origin validation including redirect destinations. No permissive SSL handler, universal file access or blanket cleartext bypass. |
| **S12 Runtime sources/crawlers/backend** | Audit supplied 100 codebases plus additions and existing 110 portal sources; preserve missing/archived/security entries with truthful status. Classify connector/extractor/runtime before counting. Integrate one supported existing runtime at a time into real job lifecycle/results; then continuous orchestration until Stop, dedupe/expiry, authenticated MCP/relay, secure name-only initialization and no end-user server deployment prerequisite. Build optional existing backend services only where architecture needs them. | Each claimed runtime actually executes a controlled public/fixture crawl through app/backend path and produces persisted results; start/stop/retry/offline/error/cleanup tests. Distinct installations with same display name get unique secure identity. Global opportunity-specific forms/portals/public announcements, not South-Africa-only or generic engine-count inflation. Real PostgreSQL/Redis/queue/backend stack tests before claiming integration. Five requested crawler override capabilities require per-engine supported/unsupported status and authorized scope; never silently claim universal overrides or bypass access controls. |
| **S13 External AI hive/key lifecycle** | Provider adapters → encrypted server-side key ownership/rotation/revocation → authorized external inference → evidence-based link sorting/review → bounded multi-provider orchestration/fallback → capability/model discovery. Reconcile existing local `AiVault`/reviewer paths with the server-key requirement; do not silently leave keys in APK or migrate secrets without authorization. AI reviews crawler results; it is not the crawler and performs no local inference. | No keys in APK/logs/reports; persistence encryption and tenant isolation tests; rate limits/quota/outages/malformed results/cancellation; provenance and unsupported-provider handling. Published free-tier/open-source options may be used; do not promise permanently unlimited free inference. Signup/key retrieval only through supported authorized flows with explicit consent; verification/CAPTCHA/terms decisions block unsupported automation, never get silently bypassed. Model catalogue size is not executed adapter count. |
| **S14 Complete UX/evidence/performance** | Finish reachable Material3/theme semantics while preserving functionality → review every visible control and dynamic callback → accessibility labels/focus/touch sizes/disabled explanations → real light/dark screens and state screenshots → measured startup/scroll/recomposition/WebView/image/memory review. Update inventory/contracts only after source exists. | Action/state tests for every in-scope control and every key in `quality/required-scenarios.json`; manual review of overlapping/static inventory entries. Before/after deterministic screenshots for home, drawer, loading/results/empty/error/filter/saved/browser/settings. Existing border/EXIF snippets do not cover these screens. Record device, build, scenario, warmup, sample count, metric and variability for benchmarks; agree/document budgets rather than fabricate speedups. Deterministic-preview-only evidence is explicitly limited and does not close a missing full-screen gate. |

**Do not claim construction complete when only UI, catalogue, SDK wrapper or fake-service tests exist.** Contract tests are necessary but actual runtime/device/backend execution is also required. Preserve missing/unsupported features honestly; unresolved requested functionality stays blocked, not silently descoped. Any product/scope decision needed from the user is a named blocker, not an excuse to implement no-op UI.

## 6. Commands and evidence retrieval

Run from repository root. Capture each command's exit separately; one later successful command must not mask an earlier failure. Shell pipelines use `set -o pipefail`. Reports under `build/` are ephemeral and not committed wholesale.

```sh
# Baseline and each correction
python3 -m unittest discover -s tools/quality/tests -v
python3 tools/quality/interactions.py --check
python3 tools/quality/unfinished.py
bash -n tools/quality/device_verification.sh
git diff --check

# On JDK 17 / SDK 36, with Android 35 emulator for full master
./gradlew --continue ktlintCheck detekt -PuniversalApk
./gradlew --continue :app:compileDebugKotlin :app:testDebugUnitTest \
  :ad-filter:testDebugUnitTest :adblock-client:testDebugUnitTest \
  :app:lintDebug :ad-filter:lintDebug :adblock-client:lintDebug -PuniversalApk
./gradlew --continue :app:verifyReleaseRuntimeDependencies \
  :app:testReleaseUnitTest :app:lintRelease :app:minifyReleaseWithR8 -PuniversalApk
./gradlew --continue searchhhVerification -PuniversalApk
# In the disposable CI emulator, device_verification.sh runs the master,
# retains private test installs for image capture and preserves failing exits.

# Existing non-Android gates (install dependencies as specified in CI)
PYTHONPATH=backend pytest backend/tests -q
npm test --prefix js-tests -- --runInBand
python3 scripts/check_locale_strings.py
# Real backend stack: reproduce backend-integration job's isolated setup,
# stack_smoke.py and cleanup. Never reuse production credentials.
```

`searchhhJvmVerification` is not the full gate. The master internally assembles private debug/instrumentation APKs; this is allowed test work, **not user delivery**. Ordinary host/release diagnostic lanes intentionally avoid unnecessary APK assembly. Keep the emulator runner's retention/export mechanism and failed-exit propagation. If action syntax is not recognized by `evidence.py`, extend its checker with negative regressions; never weaken assertion/freshness requirements.

```sh
# After pushing, find the run for the exact code SHA; never blindly use latest.
gh run list --branch arena/01a0ea36-searchhh --limit 10
gh run view RUN_ID --json headSha,status,conclusion,jobs
gh run watch RUN_ID --exit-status --interval 30
gh api repos/regularshowrigby8-create/SEARCHHH/actions/runs/RUN_ID/artifacts
gh run download RUN_ID -n ARTIFACT_NAME -D /tmp/searchhh-reports-RUN_ID
gh api 'repos/regularshowrigby8-create/SEARCHHH/check-runs/CHECK_ID/annotations?per_page=100' --paginate
```

`RUN_ID`, `CHECK_ID`, `ARTIFACT_NAME` are substitutions, not literal invocations. Store clean provenance, not temporary authenticated storage URLs. An empty JUnit summary, absent XML, timeout, cancellation or skipped test is **not** success. Full XML is authoritative; bounded annotations explicitly omit groups. Test output timestamps/source marker must correspond to the executed code. Keep both unsuccessful and successful attempts.

## 7. S15 convergence — all required work, not a reduced gate

Before S16, all occurrence tickets are closed with evidence; no queued/blocked/reopened finding or required feature remains unaccounted for. Re-enumerate full reports from the release-candidate code, not just the old seed. Require zero unaccepted lint findings (including configured warning/hint handling), compiler warnings, ktlint/Detekt findings, and unfinished/evidence failures under the **unchanged strict policy**. Necessary exact compatibility exceptions must still be legitimate, tested and in-date. Scanner candidates are not all bugs, but each legitimate exception must meet policy rather than become a debt baseline.

Run the full master, strict release-runtime/R8 checks, framework/locale/JS tests, actual backend integration and device checks at the candidate source. Verify every scenario key/control disposition and required screen/performance artifact. Ensure previously gated backend/framework jobs actually ran now—“skipped because quality failed” was never a pass. Repeat until all mandatory jobs succeed. Flaky archive/emulator failures remain infrastructure defects until investigated; retry success may provide new evidence but does not erase failures.

## 8. S16 production release — last, and only after S15

Preserve **Searchhh 0.4.1 / code 5 / `app.searchhh.browser` / minSDK 24** unless explicitly authorized otherwise. Deliver ordinary non-debuggable R8/resource-shrunk release, never `releaseDebuggable`, a renamed debug APK, an unsigned APK or a diagnostic bundle.

Use the existing `.github/workflows/buid-app-workflow.yaml` chain: `quality → framework → backend-integration → android → device-smoke → release-build → release-smoke → publish-release-apk`. Do not bypass a failed predecessor. Inspect the current workflow, don't assume this prose supersedes code. Production signing must use the stable independent backed-up key, not Android debug signing or a disposable key. Setup belongs in GitHub secrets/variables, **never chat or Git**; see `APK_DOWNLOADS.md` for exact names. Signing availability/admin protections were previously unconfirmed (403).

Require actual APK SDK signature and decoded manifest checks, pinned signer SHA-256, runtime dependency guard, non-debuggable/non-test-only/non-profileable constraints, and original checksum preserved through actual release installation. Launch that exact APK, assert process/UI, inspect crash buffer and verify `run-as` fails; a debug emulator install is not release smoke. Publish only **`Searchhh-0.4.1.apk` and `SHA256SUMS`** after all jobs succeed. Verify the downloadable asset checksum and link, then update release status. No signer material, test APK, report or screenshot is a release asset.

## 9. Commit, push and handoff contract

Each verified bounded fix and each honest blocked handoff updates its ticket/index and current report. Keep source and evidence changes reviewable; no mass “fix all” commit. Use:

```sh
git add <reviewed-explicit-paths>
git commit -m "Describe one bounded correction" \
  -m "Searchhh-Quality-Policy: accepted-v1"
git push origin arena/01a0ea36-searchhh
```

No force push, no switch to main, no automatic PR merge, no hidden resets over working changes. If GitHub auth fails, request reconnect in Arena; never request passwords/tokens/2FA. Report exact local and remote commit identity after push.

End every handoff using the policy's required headings, with overall PARTIAL/BLOCKED while any required gate is red. Include: current ticket/status, exact next action, allowed files/non-goals, code SHA vs docs SHA, run/check IDs and unfinished watches, executed pass/fail/blocked evidence, introduced vs inherited findings, visual/performance limitations, and whether a release actually exists. Make the next agent able to resume without rediscovering state or relying on chat memory.

# Quality implementation report

Date: 2026-09-30. **Status: PARTIAL — required quality gates still fail.**

## Latest FACTORY-002 result — 2026-09-30

### STATUS: PARTIAL

The expanded infrastructure is implemented and executed. The requested **70–80%
active routine-effort reduction remains NOT MEASURED**. Product/release gates
remain unresolved; no APK approved.

### SUMMARY

64 distinct registry engines, including six additions. At source `1171a4c`,
[run 36665725670](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36665725670)
executes 56: **24 PASS / 9 REPORTED / 23 FAIL / zero BLOCKED**. Every source
fingerprint is unchanged. All six additions execute, not merely install.
The eight native/APK-only jobs remain outside this batch.

[The searchable factory map](factory/FACTORY.html),
[all repositories and responsibilities](factory/FLEET.md),
[execution results/gaps](factory/EXPANSION_RESULTS.md) and
[repeat receipts](factory/verification-1171a4c.json) are saved. The map explicitly
uses the first expanded source `3f061ae`; it is not live release certification.

### FILES CREATED

Automation/router, report adapters, factory-map renderer/template, browser
checks, OpenAPI adapter, dashboard behavior tests, repository metadata, two
Python inputs/locks, the 64-engine map/reference, measurement ledger/method,
FACTORY-002 ticket and immutable execution/normalization evidence.

### FILES MODIFIED

Factory registry/schema, installer/runner, npm pins/lock, type-check targets,
factory workflow, checker tests and work instructions/audit/handoff. No app,
backend runtime, JS-test product source or canonical Android/delivery gate
changed from `6f94604`.

### DEPENDENCIES ADDED

Development only: Playwright 1.63.0, axe-core 4.13.0, Taplo CLI 0.7.0, npm
12.1.0, pipdeptree 4.2.5 and OpenAPI Spec Validator 0.9.0, plus locked support
packages. Playwright-managed Chromium is an explicit development prerequisite,
not an APK dependency or a Chromium source build. No runtime library upgrade.

### COMMANDS RUN

Locked provisioning, actual new/scoped factory checks, automatic hosted batches,
`automation plan/check/report/overview/measure`, dashboard DOM tests, 105 checker
tests, interaction and unfinished scans, local Android master attempt, receipt
reconciliation and branch-only commits/pushes. Exact argv and repositories are
in the registry and complete map; no help/version probe counted as integration.

### PASSED

105 checker tests; dashboard DOM behavior; actual hosted Chromium search,
no-match, status-filter/reset and retry-state/reload checks; OpenAPI grammar;
installed dependency conflicts; TOML validation; factory typing/lint/schema
checks. All hosted provisioning and checker-test steps succeeded.

### FAILED

23 hosted diagnostics remain red. Axe emits 11 accessibility rule/page records
across three HTML assets, not 11 confirmed unique root causes or affected nodes.
Npm audit fails; the local report shows three development dependency advisories
(one moderate, two high). Review relevance before any update. Other existing
security/style/license/complexity checks still fail. No suppression or successful
fallback was added. The workflow conclusion remains FAILURE.

### DEAD BUTTONS FOUND

No new app-wide control inventory closure. The actual HTML retry button changes
state under Chromium, but native `einkbro://retry` dispatch is not verified.
The factory map's search/filter/reset/focus/count effects are exercised.

### UNFINISHED CODE FOUND

557 files / 178 findings / zero checker errors; FAIL. Interaction inventory
617 candidates / 9 contracts is current but incomplete. Six real failing scans
normalized 479 aliases into 156 provisional packets, without parser errors;
this is not semantic deduplication or a labor-savings percentage. Eight formats
are supported; unsupported reports retain explicit opaque review work.

### UI CHANGES VERIFIED

New factory dashboard serves HTTP 200 and has DOM and real Chromium behavior
checks. CI captured factory and before/after retry images. Local Chromium fetch
and artifact download are blocked by TLS/storage EOF, so images were not
manually reviewed here. No new native/full-app visual or physical performance
approval is claimed. The existing Android UI source was unchanged.

### WEBVIEW CHECKS

Owned HTML assets only; external requests blocked, no fake native bridge.
Chromium is not Android WebView. Native origin/bridge/navigation/device safety
and the original WebView policies remain separate required work.

### REMAINING RISKS

70–80% savings need at least ten real comparable paired work items against the
already automated FACTORY-001 baseline, including failed/blocked checks. The
ledger is empty and returns NOT_MEASURED/exit 2. Root-cause diagnosis, patch
correctness, security/license disposition, native fixtures, visual references
and signing decisions remain agent/reviewer work. Nine further tools are
explicitly planned, not implemented or counted. Pins are not a full upstream
security audit; third-party tools are not OS-sandboxed. Local master remains
Java-blocked. No canonical gate, consent or maintainer approval is bypassed.

### NEXT TASK

Run a device regression for [S00 HTTP/WebView security](quality/issues/S00-http-webview-security.md)
in a JDK/SDK/emulator environment; host unit lanes passed but device behavior is
still open. Then inspect the three npm audit advisories and record
the first comparable paired routine-effort work item. Do not start S01, unrelated
remediation or a production build prematurely.

## Latest result — executable factory, 2026-09-30

### STATUS: PARTIAL

The bounded FACTORY-001 infrastructure ticket is closed. App quality and release
remain blocked; no production issue or gate is waived by this closure.

### SUMMARY

58 distinct upstream engines have concrete commands, scoped profiles and
provisioning/prerequisites. At source25f9401,
[run36662473189](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36662473189)
executes50: **20PASS / 9REPORTED / 21FAIL / zero BLOCKED**. All50 retain unchanged
source fingerprints. Workflow conclusion is FAILURE, not a passing app gate.
The other8 native/APK integrations are not counted as newly executed factory jobs.

[Evidence](factory/VERIFICATION.md), [receipts](factory/verification-25f9401.json),
[guide](factory/README.md) and [58 integrations](factory/TOOLS.md) are durable.
The final documentation checkpoint does not create new native execution evidence.

### FILES CREATED

- `tools/factory/`: runner, safety checks, installer, receipt exporter, typed
  command registry/schema, configs,24 Python inputs/hash locks, npm integrity
  lock, pinned bootstrap/upstream metadata and property/mutation tests.
- `.github/workflows/factory.yml`: scoped hosted provisioning and real checks,
  fixed action SHAs, bounded receipt annotations and redacted security artifact.
- `tools/quality/tests/test_factory.py`:22 additional real regressions.
- `docs/factory/`: plan, guide,58-engine reference, verification and receipts.
- `docs/quality/issues/FACTORY-001.md`: bounded scope, evidence and limitations.

### FILES MODIFIED

`.gitignore`, AGENTS, audit/baseline/implementation records, sequential log,
next-agent work order and backlog. No app/library/backend/JS production file,
runtime dependency or canonical Android/delivery workflow changed from3949290.

### DEPENDENCIES ADDED

Development only:24 isolated Python environments,18 pinned npm packages
(including support dependencies),5 digest-pinned official binaries and a
verified uv bootstrap. These are not additional app/runtime dependencies or
extra engine counts. HTTPX2 supplies Starlette's supported development test
client; REUSE's charset-normalizer extra supports hosts without libmagic.
Hosted core setup installs the missing ripgrep system prerequisite. Full pins,
transitive hashes and provenance are in the manifests and source audit.

### COMMANDS RUN

- `python3 -m unittest discover -s tools/quality/tests -q`
- `python3 tools/factory/install.py --ecosystem python|node|binary`
  (separate invocations; explicit `--lock` only when creating/revising locks)
- `python3 -m tools.factory.factory run --tools ... --workers 3`
  and scoped profiles; exact per-engine argv is retained in the registry.
- `python3 -m tools.factory.factory triage`
- `python3 tools/quality/interactions.py --check`
- `python3 tools/quality/unfinished.py`
- `./gradlew --continue searchhhVerification -PuniversalApk`
- Branch-only commits/pushes and GitHub run/check annotation retrieval.

### PASSED

88 local checker tests; hosted checker-test steps;43 strict backend tests;
5 Jest suites/13 tests;3 property tests. Factory Pylint/Mypy/Pyright/docstrings,
registry schema, scoped formatter and additional checks pass. Refer to the
20-PASS list in the receipts rather than treating the whole workflow as green.

### FAILED

21 diagnostics fail, including security, license, dependency, style, complexity,
HTML/CSS and duplicate-code checks. Reports include12 Bandit findings,6 Deptry
observations,203 clone pairs and2 redacted Gitleaks candidates. These are not
additive confirmed bugs or confirmed live secrets. Detect-secrets findings
remain FAIL despite its zero exit. Source unfinished gate remains FAIL.

Earlier real setup failures were retained and fixed: OSV archive contract,
missing hosted ripgrep, Bandit argument placement, REUSE encoding support,
Knip dependency resolution and the strict test-client warning. The successful
later executions, not earlier availability probes, establish integration.

### DEAD BUTTONS FOUND

No new live-control investigation was performed.617 interaction candidates and
9 contracts do not prove608 dead buttons; behavior completeness remains open.

### UNFINISHED CODE FOUND

553 files scanned;178 findings; zero checker errors. Count unchanged, no debt
allowlist added.3,509 generated file/rule packets preserve17,890 recovered raw
observations, with semantic ownership and duplicate aliases still unresolved.

### UI CHANGES VERIFIED

None: no UI source changed. No new whole-screen visual, accessibility or
performance evidence. Existing required UI/device gates remain unverified here.

### WEBVIEW CHECKS

The declared Kotlin SSL syntax scan executes and passes its narrow rule.
No WebView runtime, navigation, bridge, cleartext or device-safety closure is
claimed. Existing WebView debugging and production policies remain unchanged.

### REMAINING RISKS

- Global native/backend/device/signing gates still govern release. Local master
  cannot configure without Java/JAVA_HOME; no signed APK is approved.
- Native/APK-only factory commands need real prerequisites and execution.
- Mutation report exposes surviving/uncovered cases; no adequacy claim.
- Source hashes detect changes after execution; third-party tools are not
  OS-sandboxed. Integrity pins are not exhaustive dependency security review.
- Findings require source-level disposition. Read-only automation does not
  auto-repair source, establish root causes or replace human review.
- Caches may disappear on restoration. Re-run doctor and explicit provisioning;
  do not reuse installation claims as proof of current availability.
- Administrator-required protection/signing status remains unconfirmed.

### NEXT TASK

S00 semantic ownership and security review. Inspect the redacted candidates,
claim one bounded ticket, reconcile all raw aliases/scenarios, reproduce,
minimally repair, execute relevant checks and record closure before moving on.
Do not start S01 or a release merely because the factory infrastructure works.

## Latest S00 result — report recovery CLOSED; semantic inventory still OPEN

### STATUS: PARTIAL

Recovered complete existing quality reports without changing application code or weakening a quality/release gate. **S00 itself remains open**: raw occurrences still need stable semantic tickets, duplicate aliases and accountable ownership. No APK produced or approved.

### SUMMARY

Read-only [recovery run 36635551405](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36635551405), tooling source **0778e68**, passes all ten jobs. All five groups reconstruct with matching page counts, whole-payload/file hashes and original source/run identities. Report source is **6abf29b**; host/release/Detekt/source come from delivery36628444279, formatting from standalone36628443911 at the identical SHA. Both original workflows still FAIL. This is recovered historical execution evidence, not a newly certified build.

The original delivery formatting job failed during configuration because the Detekt plugin could not resolve; it never ran ktlint. Retained that failure and all unsuccessful transport attempts. Actual limits discovered and tested: 4096 characters per message and 50 annotations per job. Final transport uses 3000-character data pages, eight per step, at most40per job; larger bundles are split into deterministic shards, never truncated.

### FILES CREATED

- `.github/workflows/quality-report-recovery.yml`: read-only, pinned-artifact recovery; no build/sign/publish step.
- `tools/quality/report_transport.py`, `report_inventory.py` and their two test files.
- `docs/quality/issues/S00-transport.md` (closed) and `S00-ownership.md` (next task).
- `docs/quality/recovered-6abf29b/{README.md,manifest.json,occurrences.json}`: readable full-rule totals, provenance/hashes and compact raw findings. The ~5.6MB normalized dataset is intentionally retained for the requested complete backlog; raw archives/log dumps are not tracked.

### FILES MODIFIED

`AGENTS.md`, audit/baseline/implementation documents, sequential log, work order and backlog. Existing Android quality/delivery workflows, dependencies and production files are unchanged. Source/build diff between6abf29b and recovered tooling checkpoint0778e68 is empty for app/library/build configuration paths.

### DEPENDENCIES ADDED

None; Python standard library only.

### COMMANDS RUN

Local checker suite, inventory check, unfinished scan, blocked Android master attempt, both artifact download clients, `gh run view/watch`, check annotation retrieval, checksummed decode, strict occurrence export, repeat-export byte comparison and `git diff --check`. Exact IDs and source/report/dataset/exporter SHA256s are in the manifest; failed attempts are in the transport ticket.

### PASSED

- All ten final recovery jobs; all five complete payloads decode successfully. **59 tests** ran in the hosted recovery jobs.
- **66 local checker tests** after adding seven strict inventory-parser tests.
- App debug/release each **458 errors / 14 hints**, **all 34 rule groups** recovered. Library debug53/52 retains dependency overlap.
- **2788 Detekt occurrences**, independently equal in XML and SARIF.
- **13875 ktlint occurrences** across eleven complete text reports; every parsed per-rule count matches its original report footer. Previously this total was unknown.
- **178 unfinished candidates** recovered; original snapshot617controls/9contracts/42scenarios (38declared blockers).
- **17890 raw diagnostic records** exported with unique immutable-report occurrence addresses; repeat export is byte-identical. This is **not** a unique-bug total or stable root-cause ticket index.
- Current interaction index remains617/9; shell/whitespace checks pass.

### FAILED

All original mandatory quality failures remain. Current unfinished scan: **546files / 178findings / zero checker errors**, exit1; no newly introduced scanner findings. Local full master cannot start without Java/JAVA_HOME. Five unsuccessful recovery runs remain documented; none is counted as a complete recovery. Latest ordinary full verifier status is not inferred from the successful read-only recovery workflow.

### DEAD BUTTONS FOUND

No new behavioral execution. Existing TTS pause/resume no-op remains pending. Interaction candidates are not automatically dead buttons.

### UNFINISHED CODE FOUND

All178original candidates retained with source/fingerprints/locations; no marker-only deletion or suppression. Runtime crawler/AI/UI construction remains incomplete.

### UI CHANGES VERIFIED

None; no runtime/UI behavior changed in this increment. Prior image regression limitations remain unchanged.

### WEBVIEW CHECKS

Full reports expose three JavaScript-enablement findings and global cleartext configuration. These now have exact source locations for risk review; do not claim them fixed or blindly disable browser features. Prior SSL-cancellation evidence remains valid for its source, not a new comprehensive hardening pass.

### REMAINING RISKS

S00 still requires semantic identity/ownership/alias reconciliation. Security-priority review must consider `InsecureBaseConfiguration`, modern backup/transfer rules, JavaScript origins/bridges and application-context ownership before confirming production order. Report variants/tool findings overlap. The manifest distinguishes configured app release/library debug scope and limited compiler-prefix extraction from unexecuted checks. Signing/admin readiness and wider interaction/runtime/visual/performance gaps remain unresolved.

### NEXT TASK

**S00-ownership:** reconcile every recovered occurrence/scenario into stable, collision-aware semantic tickets with explicit ownership and cross-report aliases; review security priorities. Read `quality/issues/S00-ownership.md`. Do not start S01 or publish a release before the required preceding work closes.

## Latest handoff — repository work instructions (documentation only)

### STATUS: PARTIAL

User requested a GitHub-pushed work order for the next agent to resolve all 400+ issues sequentially. Created a concrete S00–S16 plan with full-report inventory reconciliation, rule-specific correction guidance, one-active-ticket protocol, required regressions, runtime/UI construction order and final production-release gate. **No additional production correction or Android verification is claimed in this documentation increment.**

### SUMMARY

The baseline is 458 app lint errors, not 458 total project issues. Saved app summaries omit 28 groups (273 error occurrences and 9 hints are not individually described). Full-artifact retrieval was attempted again and failed with storage EOF; the backlog explicitly remains an incomplete seed. Next agent must recover/reconcile full reports before claiming exhaustive triage. First code correction afterward remains mirrored-background EXIF.

### FILES CREATED

`quality/NEXT_AGENT_WORK_ORDER.md`, `quality/REMEDIATION_BACKLOG.json`, `quality/ISSUE_TEMPLATE.md` (all under `docs/`).

### FILES MODIFIED

Root `AGENTS.md`, `README.md`, `CLAUDE.md`; project audit, baseline, this implementation report, sequential remediation log and historical continuation pointer. No build, production, test, signing or CI behavior changed.

### DEPENDENCIES ADDED

None.

### COMMANDS RUN

Git status/history/remote comparison and fetch; GitHub artifact listing/download; Python checker tests, interaction check, unfinished scan, local full master attempt, JSON/link/queue validation and `git diff --check`. Same-branch Git metadata was aligned with published `77fbeef` without overwriting files after ancestry verification; status was clean before the documentation edits.

### PASSED

44 checker tests; interaction index consistency at 617 candidates / 9 contracts. Documentation JSON, phase dependency ordering, local links and whitespace are validated before commit. These are documentation/tool checks, not new app test evidence.

### FAILED

Unfinished scan: 542 files / 178 review candidates / zero checker errors, exit 1. Local master cannot start because Java/JAVA_HOME is absent. Full hosted artifact download fails with storage EOF. All previously recorded failing required project gates remain open.

### DEAD BUTTONS FOUND

No new runtime audit claim. Existing TTS pause/resume no-op is assigned to S03 for actual behavior correction/testing.

### UNFINISHED CODE FOUND

No findings removed in this increment. The work order assigns real fixes and later construction ownership; catalogue/placeholder presence never closes a ticket.

### UI CHANGES VERIFIED

None; documentation only. Previous bounded image evidence remains limited to the tested render/save paths.

### WEBVIEW CHECKS

No new execution; prior SSL cancellation evidence retained. S11 requires actual browser navigation/scheme/origin/bridge/state tests.

### REMAINING RISKS

Full occurrence inventory is not available yet; sign-off cannot rely on top-N summaries. Known quality/runtime/interaction/security/performance gaps and signing/admin-access uncertainty remain. Instructions do not technically force all readers/forks/admins to comply. No release APK authorized.

### NEXT TASK

**S00: obtain complete same-source diagnostic reports and reconcile every occurrence into owned tickets.** Then S01 is the first production correction. Follow `quality/NEXT_AGENT_WORK_ORDER.md`; earlier next-task paragraphs below are historical.

## Latest verified issue — EXIF migration CLOSED, source `6abf29b`

### STATUS: PARTIAL

The third bounded issue is closed after actual final-source verification. Required project-wide quality gates still fail; **no signed release APK or approved download has been produced**. Earlier sections below are historical checkpoints, not the current totals.

### SUMMARY

Migrated both actual image readers (`BookmarkRenderer.saveStartPageBackground` and `EinkImageProcessor.processBytes`) to AndroidX ExifInterface 1.4.2 without changing orientation, encoding, exception or caller behavior. Four Android tests cover 16 JPEG orientation comparisons across the two pipelines, absent metadata, transparent/lossless PNG, and rejected-input preservation/pass-through. The first run passed functional checks but introduced a test-only ComplexCondition; explicit pipeline branches removed it without weakening assertions, and the final source was re-executed before closure.

### FILES CREATED

- `app/src/androidTest/java/info/plateaukao/einkbro/unit/ExifImageFixtures.kt` and `ExifImagePipelineTest.kt`.
- `docs/quality/verification-6abf29b.json`: source/run/check identities, test summaries, lint counters, failed attempts and image hashes.
- `docs/quality/exif-8c44b27/` and `exif-6abf29b/`: four actual Android PNG exports and completion marker per verified run.

### FILES MODIFIED

Production: `unit/BookmarkRenderer.kt`, `unit/EinkImageProcessor.kt`, `app/build.gradle.kts`, `gradle/libs.versions.toml`. Evidence transport/checkers: `tools/quality/ci_summary.py`, `device_verification.sh`, `tests/test_contracts.py`, `tests/test_device_runner.py`. Documentation: project audit, baseline verification, sequential remediation, this report and APK download status. No new UI control or route; interaction inventory remains unchanged.

### DEPENDENCIES ADDED

Only **`androidx.exifinterface:exifinterface:1.4.2`**, pinned explicitly. Actual debug/release compilation and API 35 execution succeeded; API 24 runtime is not separately certified.

### COMMANDS RUN

- Local: `python3 -m unittest discover -s tools/quality/tests -v`; `python3 tools/quality/interactions.py --check`; `python3 tools/quality/unfinished.py`; `bash -n tools/quality/device_verification.sh`; `git diff --check`.
- Local master `./gradlew --continue searchhhVerification -PuniversalApk` remains blocked by missing Java/JAVA_HOME, as recorded before implementation.
- Hosted full device master and strict host/release/format/Detekt lanes: [36628443911](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36628443911) and independent same-SHA delivery verifier [36628444279](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36628444279). Retrieved executed evidence using `gh run view` and `gh api .../check-runs/{id}/annotations`; waited for completion with `gh run watch`.

### PASSED

- Final-source standalone device verifier: **384 combined passing results**, including all four EXIF methods, previous SAF/border tests and SSL cancellation; zero failures/errors/skips in reported test results. This lane includes JVM results, not 384 unique device tests.
- Same-source delivery host: **360 passing** results; standalone and delivery release lanes: **336 passing** each. Counts overlap and must not be added.
- Debug/release XML explicitly report **ExifInterface = 0**, down from 8. App lint errors decrease **466 → 458**. DrawAllocation and Recycle remain zero.
- Detekt returns **2789 → 2788**, its pre-migration count. No EXIF-test findings appear in retrieved focused format/Detekt diagnostics; neither global gate passes.
- Both current/reference PNG pairs are byte-identical, have valid chunk CRCs and measure **128×160**. All five final-run files also equal their first-run counterparts. Both current images were viewed: correct background 90° and web TRANSVERSE quadrant placement.
- **44 local checker tests pass**; interaction index consistency passes at **617 candidates / 9 contracts**; shell syntax and whitespace checks pass.

### FAILED

Both overall workflows **FAIL**. Required lint (**458 app errors / 14 hints**, library debt also remains), formatting, Detekt (**2788**), unfinished-source and interaction-evidence gates are still red.

The standalone host check `109611294295` reported **zero tests and no lint reports**. Its available failure annotation ends at `Caused by: java.util.zip.ZipException: Ar`; the affected archive and complete root cause are unavailable, so this is not claimed to be a diagnosed transient failure or a pass. Independent same-SHA host check `109611299996` supplies the passing test evidence above. Delivery device check `109611299564` also failed with shell exit 1 and zero reported tests; standalone device check `109611293978` independently supplies the final-source Android evidence. Both failed attempts are retained explicitly.

### DEAD BUTTONS FOUND

No new controls were added or certified. The previously identified system-TTS pause/resume no-op remains unresolved; the interaction inventory is not proof of working controls.

### UNFINISHED CODE FOUND

Current scan: **542 files / 178 review candidates / zero checker errors**, failing as designed. Broader crawler/AI/runtime/interaction requirements remain incomplete. No debt suppression or test bypass was added.

### UI CHANGES VERIFIED

Actual image-processing/save paths were tested, not a new screen. `BrowserActivity`'s background picker still calls the same writer and retains its existing toast/reload path; deep-mode image interception still calls the same processor. Export references independently reconstruct previous orientation semantics; **they are not before-APK or full-screen screenshots**. Tone reference reuses existing processing code, isolating orientation/codec behavior rather than independently certifying tone correctness. Full visual/accessibility/performance requirements remain open.

### WEBVIEW CHECKS

Image-processing entry point and all eight web-image orientation behaviors remain intact. Existing invalid-certificate cancellation regression passes in the final-source verifier. This does not establish completion of broader navigation, bridge, scheme or state-restoration hardening.

### REMAINING RISKS

Background mirrored EXIF tags remain intentionally ignored, matching previous behavior. No memory/downsampling/atomic-save policy changes, API 24/OEM-photo/all-format certification or performance measurements. The archive-reading host failure needs complete diagnostics if it recurs. Signing availability and administrator protections remain unconfirmed. Delivery `release-build`, `release-smoke` and `publish-release-apk` are skipped after failed quality; private emulator APKs are not release assets.

### NEXT TASK

Audit background handling of mirrored EXIF tags (2, 4, 5, 7) as the next bounded correctness issue, before changing its existing behavior. No implementation for that follow-up has started; do not produce a release before all required gates pass.

## Previous verified code — source `d390b4f`

Two bounded issues have now been closed **sequentially**, with no release APK produced:

1. **Border allocations:** source `92484f6`, run 36605281071: zero DrawAllocation findings; both Android tests pass, including asserted effective densities, 144 exact pixel comparisons, and cache reuse/invalidation. Four 257×5 PNGs in `quality/border-92484f6/` have valid CRCs and byte-identical reference/current pairs. The stamp images are 2391 bytes, explaining the earlier transport cap. Evidence: `quality/verification-92484f6.json`.
2. **SAF stream ownership:** source `d390b4f`: SupernoteStorage now owns/closes its output stream around the write callback and returns only the Uri after write AND close complete. DownloadHelper reports failure on missing destinations or propagated exceptions. All **nine new regressions pass** (bytes/order/MIME, unavailable destinations, open/write/close failures, suppressed close failure). Debug and release XML summaries explicitly report **Recycle = 0**. Existing caller already used `.use`; this fixes an ownership-design/lint problem, not a measured preexisting leak. Evidence: `quality/verification-d390b4f.json`.

### Executed verification

- [Android verification 36610394275](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36610394275): **360 host / 336 release unit results pass**, no failures/errors/skips. Counts overlap and are not additive.
- Its standalone device lane failed with shell exit 1 and no reported tests. Exact cause unavailable: raw log download returned Azure EOF. It is **not** counted as passing.
- [Delivery workflow 36610394636](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36610394636), **identical source SHA**, independently ran the full device verifier: **380 combined results pass**, no failures/errors/skips, including the nine SAF tests and both border tests. Exported border images still match the saved images byte-for-byte. These include repeated JVM tests, not 380 unique device tests.
- New test formatting and the touched writer's ReturnCount finding are resolved. Older picker formatting/generic-catch findings remain visible. No suppression, skipped test or relaxed assertion.
- **41 local checker tests pass**; inventory **617 candidates / 9 contracts**, incomplete. Unfinished scan **540 files / 178 review candidates / zero checker errors**.
- Local master remains environment-blocked by absent Java/JAVA_HOME. Hosted execution supplies Android evidence. Manual job rerun is unavailable to the integration (HTTP 403, actions:write required); GitHub read/push access works. No permission changes were attempted.

### Still failing / not release-ready

App lint **466 errors / 14 hints**, library lint **53/52** (overlap), Detekt **2788**, formatting, unfinished-source and interaction-evidence gates. Real Supernote provider/picker/OEM behavior and atomic replacement are not certified: the ownership tests use actual tracking streams with mocked Android/SAF boundaries. Border artifacts are software-rendered strips, not full-screen before/after APK screenshots or hardware/e-ink performance measurements. No timing or whole-renderer allocation-free claim.

Known system-TTS pause/resume no-op and broader crawler/AI/interaction gaps remain. SSL-cancellation regression passed in the retrieved same-source device verifier; broader WebView hardening is incomplete.

### Files / dependencies / delivery

Created `SupernoteStorageTest.kt`, verified border PNGs/marker, and structured verification records. Modified the scoped writer, DownloadHelper caller, CI diagnostics and audit/baseline/remediation/release/report documents. **No dependency added.** No new control or route; existing SAF download paths still use the same caller and visible success/error feedback.

Ordinary host/release checks do not assemble APKs; private emulator-test APKs stay internal. In the retrieved delivery workflow, **release-build, release-smoke and publish-release-apk are skipped** after failed quality. Signing availability remains unconfirmed. There is no approved `Searchhh-0.4.1.apk` download. Final report/evidence updates do not change the verified runtime code.

**Historical next task at this checkpoint:** audit `BookmarkRenderer`'s legacy ExifInterface usage. That migration is now closed in the latest section above.

## Earlier verified status — source `62cbf8d`

[Run 36595094637](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36595094637) confirms **release optimization and unsigned APK assembly pass**. The former R8 missing-type failure is resolved. These are unsigned verification outputs, **not a signed Searchhh release download**. Original R8 failure evidence and the exact compatibility rationale are retained; no global warning suppression or disabled shrinker was added.

- Release app unit lane: **327 passed**; debug host lane: **351 passed**; device lane: **369 combined passed**, zero failures/errors/skips. Counts overlap and are not additive unique tests.
- Android 35 passed the actual Ktor detector fallback and authenticated HTTP/SSE/MCP tests. It also passed the actual theme-dialog test: applied public theme/color/window values, tagged OK click, then observable fragment dismissal. This does not establish settings-to-dialog navigation, pre-34 animation equivalence, before/after screenshots or performance improvements.
- The unsafe private window-field reflection and silent catch are removed. The actual Compose panel theme receives the public API-34 attribute; pre-34 devices use supported platform movement behavior.
- Still failing: **470 app lint errors / 14 hints**, library lint **53/52** (overlap), Detekt **2,791 weighted issues**, formatting, unfinished-code and interaction evidence. Local unfinished scan: **536 files / 178 review candidates / zero checker errors**. No scanner waivers.
- Local checker tests: **35 passed**; interaction index: **617 candidates / 9 contracts**, still incomplete. Two new rows describe one actual dialog control/callback and share one test; they are not two independent tests.
- Added only **compile-only** `com.google.errorprone:error_prone_annotations:2.18.0`; the release runtime guard rejects accidental packaging of this group. Encryption semantics are unchanged. The two exact Ktor desktop-type R8 rules have upstream source evidence, owner/review/removal conditions and executed Android regression evidence in COMPATIBILITY_EXCEPTIONS.md.
- Structured test/diagnostic/lint-priority evidence is saved in `quality/verification-62cbf8d.json`. A final readability cleanup replaces the Android test's literal SDK 34 with the equivalent named platform constant; follow-up CI is pending for that cleanup and this report.

**Next task:** eliminate per-draw RectF/DashPathEffect allocations in `ThemedEdgeBorderView` with a rendering regression test. Production signing and all remaining gates still block APK delivery; no debug or unsigned APK is offered as the release.


## Earlier release-delivery checkpoint — source `0f92445`

The user explicitly requires a normal signed Searchhh release. Debug-artifact recommendations have been withdrawn from the download page and README. Debug publication has been replaced with a gated signed-release build/validation/device-smoke/publish chain. No production signing key is generated or exposed; confirming Actions signing secrets requires permissions unavailable to this integration (403).

Latest completed verification: [36583626253](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36583626253), source `0f92445`. **327 app release-variant unit tests passed**, debug host lane 351 passed, device lane 367 combined passed; those counts overlap and must not be summed as unique tests. The corrected release runtime dependency guard and Kotlin compilation passed. Release assembly failed at `:app:minifyReleaseWithR8` on missing classes (including Error Prone annotations and Ktor's desktop management/debug detector references); `:app:lintRelease` failed with 472 errors. No release APK was produced. Existing format/Detekt/unfinished/evidence gates still fail. Bounded hosted evidence is in `quality/release-verification-0f92445.json`.

Follow-up cleanup removes the now-unreferenced developer-setting labels from 31 locale files and adds R8 missing-class capture to CI diagnostics (one new regression test). These follow-up changes await hosted verification; all locale XML parses locally, 33 checker tests pass, inventory remains 617/7 and diff check passes. The unfinished scanner still fails at 534 files / 179 candidates / zero checker errors. No warning suppression or debt waiver was added.

**Next task:** resolve `:app:minifyReleaseWithR8` missing-class failures using the expanded diagnostics and reviewed upstream Android compatibility, without hiding required runtime dependencies. Signing configuration and all remaining quality failures must also be resolved before a real `Searchhh-0.4.1.apk` download can be offered.


## Earlier verified state — source `483b771`

[Android verification 36573815185](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36573815185) completed **FAILURE overall**, while compilation, assembly and executed tests passed. Exact bounded annotations are saved in [verification-483b771.json](quality/verification-483b771.json).

- **351 host test results passed**, zero failures/errors/skips, across the three Android modules.
- **367 combined results passed** in the device lane, zero failures/errors/skips. This includes repeated JVM tests; it is not 367 additional device tests. Android 35 navigation/recreation, cache eviction/disk recovery and encrypted-store tests passed.
- All 17 focused regression methods in the annotations passed, including TLS cancel/no-bypass, route fallback/IDs, browser memory side effects, robots matching/delay/sitemaps, locale round-trip/legacy values and actual cache/encrypted storage behavior.
- App production and test compilation passed without relaxing warnings-as-errors. The final test warning was fixed by reusing the caption test's configured Json instance, with assertions unchanged.
- Five debug APK variants were assembled. They are in artifact `android-build-test-lint-36573815185`, **not a certified release**. No APK binary is committed to Git.
- Still failing: app lint **470 errors / 14 hints**, library lint **52 / 51 errors** (overlap possible), Detekt **2,790 weighted issues**, formatting, unfinished-code and interaction-evidence checks. Local unfinished review has **179 candidates**, not 179 proven bugs; no waivers. Local checker tests: **27 passed**.
- Four registered tab destinations, Back and recreation/query restoration now have passing Android behavior evidence. Full interaction coverage, before/after pixels, app-launch smoke after the master gate, performance, unsupported-URL handling and full bridge/cleartext safety are not established.
- GitHub repository access is restored; no reconnection is needed. Administrator-only branch protection remains unavailable. Most crawler codebases remain catalogue entries, not working integrations.

**Next task:** implement and behavior-test system-TTS Play/Pause in `TtsViewModel.pauseOrResume`, the confirmed no-op branch.

The original increment and earlier handoff below are historical records. Statements that tests were unexecuted, compilation blocked or GitHub disconnected describe those earlier runs, not the current verified source above.

Audit and baseline were saved before implementation. This is the first enforcement increment, not completion of all 18 phases or all crawler runtimes.

## Original enforcement increment (historical)

- Repository instructions/policy, commit and PR acknowledgment checks, CODEOWNERS, and administrator protection instructions. Enforcement limits and the actual 403 are documented.
- Tested unfinished-source scanner covering production/tests/scripts; exact expiring regression-backed exception format. No exceptions granted. Conservative candidates are not automatically proven defects.
- Generated interaction index (617 lexical candidates, including overlapping widgets/callbacks and previews); seven initial contracts (three linked to test methods, four start/stop entries incomplete); 42 required behavior/visual/performance scenarios remain independently gated.
- Central Searchhh test tags and four actual tab destinations; saved route IDs, safe unknown fallback, Back handling. Browser remains a separate legacy activity. No fake drawer/history/live route was registered.
- TLS handler now cancels invalid certificates before user feedback, with focused unit regression test. Existing HTTP/global-cleartext and native-bridge risks remain. The system-TTS notification Play/Pause path dispatches to the no-op branch (`TtsViewModel.kt:70,223–226`); UI/device confirmation is still pending.
- Two route unit tests, one TLS unit test and two Compose navigation/state-restoration tests added. They are **not executed**: hosted app compilation fails before these tests can run.
- Detekt 1.23.8, ktlint Gradle plugin 12.1.2 / engine 1.5.0; compiler warnings as errors; strict lint with former baseline/translation/release exclusions removed from configuration. Historical baseline file retained, not used.
- `failOnUnfinishedCode`, `verifyInteractionInventory`, `verifyInteractionEvidence`, `searchhhJvmVerification`, `searchhhVerification` and supporting provenance/checker-test tasks. Full gate requires device evidence; all three Android modules' unit and connected-debug tasks are included, with app UI execution ordered after library device tests. No-source library tasks are not counted as behavioral proof. No missing-task success fallback.
- New multi-lane Android workflow; fail-closed aggregate. Existing APK publication workflow now depends on the full quality workflow before any legacy release jobs.

## Executed locally so far

| Command | Exit | Result |
|---|---:|---|
| `./gradlew assembleDebug` | 1 | Baseline blocked: no Java |
| `./gradlew test` | 1 | Baseline blocked: no Java |
| `./gradlew lint` | 1 | Baseline blocked: no Java |
| `./gradlew connectedDebugAndroidTest` | 1 | Baseline blocked: no Java |
| `adb devices` | 127 | adb unavailable |
| `sudo apt-get update` / JDK install attempt | install 100 | Debian repository connection failed; package unavailable; Java still absent |
| `python3 -m unittest discover -s tools/quality/tests -v` | 0 | 26 checker tests passed (including failed-master/crash preservation with mock adb; these are not device tests) |
| `python3 tools/quality/interactions.py --check` | 0 | Inventory synchronized, not behavioral coverage |
| `python3 tools/quality/unfinished.py` | 1 | 180 findings across 525 source files; zero checker errors; no waivers |
| `python3 tools/quality/evidence.py` | 1 | Missing fresh Android execution and incomplete interaction/scenario contracts |
| `git diff --check` | 0 | No whitespace patch errors |

## Hosted verification and failure loop

- Initial run [36521594729](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36521594729), source `949223c`: failed SDK setup and overly strict acknowledgment footer parsing. Neither was passed off as application success.
- Fixed generated co-author footer handling with a regression test; replaced failing SDK setup action with explicit checks of the runner's already-installed SDK 36/platform tools.
- Run [36521842149](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36521842149), source `b8a57fa`: configuration executes; acknowledgment and inventory checks pass; strict source/format/Detekt/lint/compiler/device aggregate fails.
- Diagnostic rerun [36522577862](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36522577862), source `cdc5455`: same substantive failures. `:app:compileDebugKotlin` fails with `warnings found and -Werror specified`; app tests/assembly are blocked, not passing. Detekt reports **2,781 weighted issues**, not 2,781 proven runtime bugs. `:ad-filter:lintDebug` reports 52 errors; `:adblock-client:lintDebug` reports 51, including `adblock-client/build.gradle:12` / OldTargetApi. Counts overlap because dependency lint is enabled. App lint is blocked behind compilation.
- Publication run [36522578086](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36522578086) fails its required quality job; `publish-test-apk` is **skipped**, not green. No new APK has been certified or released. The prior 0.4 APK is unchanged and does not contain this SSL source fix.
- Added compact annotation diagnostics because downloading GitHub job logs returned EOF. Full reports remain workflow artifacts. GitHub limits per-step annotations, so execution outcomes are now emitted before bounded diagnostic samples.
- The final device harness captures crash logs **inside** the emulator runner, before teardown, preserves a failing Gradle exit, fails on crashes, and launches/dumps/screenshots only after the master gate succeeds. Three local mock-process tests verify these exit paths; they do not prove Android behavior. Hosted re-verification of this harness follows its push.

## Additional executed verification

| Command / check | Exit | Evidence |
|---|---:|---|
| `PYTHONPATH=backend .venv/bin/pytest backend/tests -q` | 0 | 43 passed; one existing Starlette/httpx deprecation warning |
| `npm ci --prefix js-tests --ignore-scripts` | 1 | No package-lock; corrected to the repository's install approach |
| `npm install --prefix js-tests --ignore-scripts --package-lock=false` then `npm test --prefix js-tests -- --runInBand` | 0 | 13 tests, five suites passed; npm audit reported zero vulnerabilities |
| `.venv/bin/pip check` | 0 | No broken requirements |
| `python3 scripts/check_locale_strings.py` | 0 | No stale keys; many missing translations remain (not translation completeness) |
| Workflow/Detekt YAML parse | 0 | Parsed after installing existing backend requirements, including PyYAML; initial system-Python attempt lacked PyYAML |
| Narrow tracked-source secret-pattern scan | 0 | No private-key/AWS-ID/GitHub-token/OpenAI-project-key candidates; not a complete secret/history audit |
| JDK bootstrap via temporary `jdk4py==17.0.9.2` environment | 0 | Local Java 17 executable available; no APK/runtime dependency added |
| `./gradlew help` and `./gradlew --continue searchhhVerification -PuniversalApk` with that JDK | 1 | Wrapper distribution download fails with SSLHandshakeException / peer EOF; no TLS validation bypass used |
| Ktlint CLI download attempt | curl 35 | Maven TLS connection failed; no formatter or binary added to Git |
| `bash -n tools/quality/device_verification.sh` | 0 | Shell syntax only, not device verification |

The original no-Java baseline remains accurate. Later bootstrap changed the local blocker from missing Java to network/TLS; adb/SDK remain absent locally. Hosted SDK/JDK checks succeeded, exposing actual source/static debt.

Current scanner breakdown: **73 empty-action, 65 silent-catch, 8 unfinished-marker, 13 constant-return-review, 21 empty-function-review** findings. Several are preview/interface/third-party cases requiring legitimate review. No blanket exceptions were added. Additional negative fixtures exposed an empty-string expression-return detection bug; it was fixed and all 26 checker tests pass. Empty functions and Python empty-collection returns are now flagged too; this increased the count from the earlier 151 to 180, rather than hiding cases. The broader scanner also reaches `ad-filter/scriptlets_src/`, beyond the initial module-src-only lexical audit.


## Findings and incomplete requirements

- `TtsViewModel.pauseOrResume` returns for system TTS without action; EPUB structured parsing and filter detector markers remain. Preview no-op handlers must not be described as nine dead live buttons.
- Scanner finds silent exceptions/empty actions/constant-return review candidates across Kotlin, JavaScript and Python. Some are legitimate contracts/preview callbacks; each needs contextual review and regression evidence, not mechanical deletion or blanket suppression.
- Start/stop/filter/save/details/error/offline/accessibility/browser behavior coverage is not complete. Most of the 617 candidates are unmapped. Existing tests are not automatically treated as coverage.
- No verified before/after screenshots, pixel regression baseline, deterministic Searchhh screen preview set or Macrobenchmark added. Home-light/drawer/theme and other required UI are absent/incomplete. Navigation/tag changes are source-level only until Android tests execute. No visual or performance improvement is claimed.
- Material 2 remains; Material 3 migration is not done. No new runtime dependency was added merely for a count.
- Global cleartext and existing native interfaces remain unaudited for full origin/frame safety. SSL cancellation test is added, not yet passed.
- Hosted checks confirm compiler warnings block `:app:compileDebugKotlin`, dependency-module lint fails, Detekt reports 2,781 weighted issues and formatting fails across modules. This includes work needed in touched files; not every failure is claimed to be preexisting. Strict failures are not permission to waive them.
- Branch protection is not enabled (403). Administrator action is required; no rules can compel public readers/forks.
- Most catalogued crawler codebases are still not runtime integrations; this task does not retroactively complete that rejected deliverable.

## Files

Created: `AGENTS.md`; policy/audit/baseline/inventory/report docs and `docs/quality/` evidence; `quality/` rules/contracts/inventory; `tools/quality/` checkers/tests; new workflow/PR template/CODEOWNERS; central tags/routes and Android tests.
Modified: root/app Gradle, dependency catalogue, legacy publication workflow, agent/contributor guides, SearchhhActivity, WebViewSslHandler and its caller. No app identity/package rename or broad UI rewrite.

## Historical handoff — GitHub connection blocked (subsequently resolved)

Latest successful push: **`6102e35b358278263c2dc1cfed8f63fda66c89c6`** on `arena/01a0ea36-searchhh`.
Final requested runs:
- [Android verification 36524028312](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36524028312)
- [Publication pipeline 36524028490](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36524028490)

Observed before connection loss: checker tests, acknowledgment and inventory steps passed; unfinished-source gate failed; Detekt again reported 2,781 weighted issues. **The final overall conclusions, library-device results and final artifact list are unconfirmed.** The watcher ended, but subsequent `gh run view` and check-annotation API calls returned **HTTP 401: Bad credentials**. Its exit code must not be treated as a successful quality result.

GitHub operations were stopped rather than requesting or storing credentials. The user must reconnect GitHub in Arena. This is separate from the earlier branch-protection 403: even after reconnection, administrator rights are still needed to enable required checks on main.

After connection loss, the source scanner's explicit generated-output exclusions were completed for native `.cxx` / `.externalNativeBuild` directories. Regression coverage verifies those outputs are omitted while similarly named directories *inside actual source* are still scanned. All **26 checker tests** pass locally. This small correction and this final report update are saved locally on the session branch; they have **not been pushed or verified by hosted CI** after the 401.

The application changes remain unverified; no certified APK or visual evidence is claimed. After reconnecting and confirming the final run, the first code-remediation target is the warnings blocking `:app:compileDebugKotlin`, without weakening warnings-as-errors. Static/interaction/visual/protection work still remains after that.

## Next task at the previous handoff (completed)

Reconnect GitHub in Arena and retrieve the verification results. Repository operations have since been restored; see the current state above.

## Continuation — access restored and first compiler corrections

Repository APIs work again (the `/user` endpoint remains outside the integration's scope). Run 36524028312 is now confirmed **failed**, not unknown: 24 library unit tests passed; the device lane reports 28 library unit/device results in total, no app tests and no APK. Audit and local baseline for the next increment are in `quality/CONTINUATION-2026-09-29.md`.

Preserving the saved scanner correction, this increment fixes interface-parameter mismatches in BrowserActivity and replaces its obsolete memory-trim threshold with supported BACKGROUND handling. A small memory-trim helper has three action/side-effect unit tests; these are **added, not yet executed** until hosted compilation succeeds. Compiler diagnostics are now emitted in readable bounded chunks rather than only three warnings. Local checker tests pass **27** cases; native generated outputs are still excluded precisely. No gate, warning, test or debt baseline was disabled.

### Compiler batch two

Run [36570002530](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36570002530) failed as expected on remaining debt; the first three compiler warnings are gone. Its 23 remaining compiler warnings were saved in `quality/compiler-warnings-1965719.txt` and addressed in the next batch. That batch migrates compatible APIs, avoids repeated JSON allocation, preserves full TTS locale tags, keeps robots matching behavior through a current API adapter, and retains encrypted credentials rather than switching to plaintext. One narrowly scoped **legacy cache-level** annotation is documented with removal conditions and tests in `quality/COMPATIBILITY_EXCEPTIONS.md`; the existing scoped encrypted-preferences annotation is documented there too. No global suppression or unfinished-code exception was added.

Additional tests were added for robots matching/delay/sitemaps, legacy/modern cache policy, actual Android cache eviction and disk fallback, persisted locale script/region, and encrypted-store reopening/plaintext absence. These are **pending hosted execution**. The previously silent disk-cache write failure is now reported without logging URLs, paths or content; in-memory fallback remains intact. The unfinished scanner now reports **179** candidates (one resolved silent catch), with no waivers. Local checker tests still pass 27 cases.

### Build and device milestone

Run [36572157017](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36572157017), source `3366e46`, confirms **app production Kotlin compiles with warnings-as-errors still enabled** and debug APKs are assembled. The device lane has **40 passing results, zero failures/errors/skips** across the executed library unit/device and app device tests—not 40 app UI tests. Five targeted app instrumentation methods passed: all registered tab navigation/Back; query and route restoration after recreation; actual cache trim/disk rehydration; memory fallback after disk-write failure; encrypted preference reopening with no plaintext credential in the file.

App unit tests are still blocked by one existing test-source warning: redundant Json construction at `YouTubeCaptionFetcherTest.kt:84`. The next correction reuses the same configured serializer without changing any assertions. App lint now executes and reports **470 errors / 14 hints**; library lint reports 52/51 errors (dependency overlap). Detekt reports **2,790 weighted issues**. Formatting, unfinished-code and interaction-evidence gates still fail. APK creation and these passing device tests **do not qualify the release**; no screenshot or performance-regression claim is made.

### Compiler continuation file manifest

Changes from `6102e35` through the verified source, plus this evidence update (`A` created, `M` modified). No runtime dependencies added in this continuation.

```text
M	app/src/androidTest/java/info/plateaukao/einkbro/searchhh/PersistenceTest.kt
A	app/src/androidTest/java/info/plateaukao/einkbro/unit/ImageCacheBehaviorTest.kt
M	app/src/main/java/info/plateaukao/einkbro/activity/BrowserActivity.kt
M	app/src/main/java/info/plateaukao/einkbro/activity/ToolbarConfigActivity.kt
M	app/src/main/java/info/plateaukao/einkbro/activity/delegates/InputBarDelegate.kt
M	app/src/main/java/info/plateaukao/einkbro/activity/delegates/TaskMenuDelegate.kt
A	app/src/main/java/info/plateaukao/einkbro/browser/BrowserMemoryTrimmer.kt
M	app/src/main/java/info/plateaukao/einkbro/data/remote/OpenAiRepository.kt
M	app/src/main/java/info/plateaukao/einkbro/preference/AiConfig.kt
M	app/src/main/java/info/plateaukao/einkbro/preference/TtsConfig.kt
M	app/src/main/java/info/plateaukao/einkbro/searchhh/SearchhhData.kt
M	app/src/main/java/info/plateaukao/einkbro/searchhh/local/PortalCrawler.kt
M	app/src/main/java/info/plateaukao/einkbro/searchhh/local/PublicSearch.kt
A	app/src/main/java/info/plateaukao/einkbro/searchhh/local/SearchhhRobots.kt
M	app/src/main/java/info/plateaukao/einkbro/service/TtsNotificationManager.kt
M	app/src/main/java/info/plateaukao/einkbro/unit/EinkImageCache.kt
A	app/src/main/java/info/plateaukao/einkbro/unit/ImageCacheTrimPolicy.kt
M	app/src/main/java/info/plateaukao/einkbro/unit/LocaleManager.kt
M	app/src/main/java/info/plateaukao/einkbro/view/WebViewJsBridge.kt
M	app/src/main/java/info/plateaukao/einkbro/view/dialog/compose/ETtsVoiceDialogFragment.kt
M	app/src/main/java/info/plateaukao/einkbro/view/dialog/compose/TtsSettingDialogFragment.kt
A	app/src/test/java/info/plateaukao/einkbro/browser/BrowserMemoryTrimmerTest.kt
M	app/src/test/java/info/plateaukao/einkbro/caption/YouTubeCaptionFetcherTest.kt
M	app/src/test/java/info/plateaukao/einkbro/preference/TtsConfigTest.kt
A	app/src/test/java/info/plateaukao/einkbro/searchhh/SearchhhRobotsTest.kt
A	app/src/test/java/info/plateaukao/einkbro/unit/ImageCacheTrimPolicyTest.kt
M	docs/INTERACTION_INVENTORY.md
M	docs/QUALITY_IMPLEMENTATION_REPORT.md
A	docs/quality/COMPATIBILITY_EXCEPTIONS.md
A	docs/quality/CONTINUATION-2026-09-29.md
A	docs/quality/compiler-warnings-1965719.txt
M	quality/interaction-inventory.json
M	tools/quality/ci_summary.py
M	tools/quality/tests/test_contracts.py
M	tools/quality/tests/test_unfinished.py
M	tools/quality/unfinished.py
A	docs/quality/verification-483b771.json
```

## APK download handoff — 2026-09-29

The user requested APK links. Rechecked the newer completed run [36575356166](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36575356166), source `a0fbc6236aee037a6599d285918fbfea973f0e29`: host 351 passed, device lane 367 combined passed, zero failures/errors/skips in those test results. The same static/unfinished/evidence gates fail; overall status remains **PARTIAL**, not release-approved. No application code or publishing gate changed in this handoff.

The actual non-expired APK artifact is `11037786858`, `android-build-test-lint-36575356166`, 132,761,063 bytes. [APK_DOWNLOADS.md](APK_DOWNLOADS.md) provides the official download link, exact variant filenames, Android minimum, extraction/install guidance and limitations. It is a ZIP containing APKs, not a direct APK URL. An attempted sandbox download failed with EOF at GitHub blob storage, so no local APK hash or attachment is claimed. No stale release was substituted for this build.

Local master verification was attempted again: `./gradlew --continue searchhhVerification -PuniversalApk` exited 1 because Java/JAVA_HOME is unavailable; adb is also absent. Repository Git metadata was restored to the fetched session-branch head with a mixed reset, preserving every working-tree file; the restored tree was clean before the documentation additions.

Files: created `docs/APK_DOWNLOADS.md`; updated this report. No dependencies added. Next implementation task remains behavior-tested system-TTS Play/Pause rather than the current no-op.

## Release-only delivery correction

The user rejects debug APK distribution. `docs/APK_DOWNLOADS.md` now states that no approved 0.4.1 release link exists, rather than recommending a debug ZIP. Pre-change audit is recorded in PROJECT_AUDIT.md.

Implemented: ordinary release is explicitly non-debuggable, version 0.4.1/code 5, optimized and privately signed when production credentials are provided; no debug-key fallback. Removed the saved-preference override that enabled WebView debugging in release and removed its redundant settings switch. Runtime dependency guard rejects test runners/mock libraries/runtime developer tooling in release. Unit tests remain separate source sets, with fresh release test provenance. Added a mandatory release compilation/test/lint lane to the aggregate CI quality check.

Publication now chains existing strict quality/backend/debug-device verification → signed release build → SDK signature/manifest/certificate-pin checks → exact release-device installation/launch/UI/crash/run-as checks → release-only APK/checksum publication. Debug artifacts remain internal CI evidence and are no longer published as GitHub release APKs. Signing caches are disabled, private key material is temporary and cleaned up, and no new APK is claimed yet.

Local checks: 32 quality checker tests pass (five new release-validator tests cover positive metadata and negative debug/test/instrumentation/identity/version/signature/profiling cases); inventory 617/7 is current; YAML and embedded bash syntax parse; release dependency-chain checks pass; diff check passes. Unfinished scan: 534 files / 179 findings / no checker errors, still fails. No runtime dependency was added; PyYAML was installed only under /tmp for workflow syntax validation.

Blocked: local Gradle still has no Java/SDK; listing Actions signing secrets returns HTTP 403, so credential availability cannot be confirmed. Hosted release build/runtime-dependency guard and device checks are pending; binary validation fixture tests do not certify an actual APK. No before/after visual evidence is claimed for the removed developer switch. Existing lint/Detekt/format/source/interaction failures remain release blockers. Next task: inspect the hosted release-build lane and correct its first build failure without weakening gates.

## R8 continuation — changes awaiting hosted execution

Pre-change audit and baseline recorded. Added compile-only Error Prone annotations 2.18.0 to resolve Tink's annotation references without adding an APK runtime dependency. Added two exact, upstream-reviewed Android/desktop R8 compatibility rules with owner, review date and removal conditions; no global warning suppression or disabled shrinking. Added `KtorAndroidCompatibilityTest` to exercise the real detector fallback on Android; existing authenticated Ktor/MCP and encrypted persistence tests remain intact. CI summaries now expose these relevant test results. Local checker tests still pass 33 cases; inventory and diff check pass. Release optimization/device regression results are pending, and signing/static/interaction gates still block delivery.

## R8 milestone and first lint fix

Run 36593371337, source `bdd0e59`: R8 optimization and unsigned release APK assembly **pass**. 327 release app unit tests pass. Device lane has 368 combined passing results, including the new real Ktor fallback test, authenticated HTTP/SSE/MCP test and encrypted-preference regression. No signed APK is claimed. Release lint now reports 471 errors (counts across dependency reports can overlap/change); other quality gates remain red.

Next small fix replaces private window-flag reflection and its silent catch with the actual panel theme's public API-34 move-animation attribute. Public enter/exit animation and panel colors are retained; pre-34 window movement follows supported platform behavior, not a hidden flag. Adds an Android test opening the real theme dialog, inspecting applied theme/window values, clicking its now-tagged OK button and asserting removal from the fragment manager. The widget/callback have two inventory rows backed by the same test, not two independent UI tests. Settings-to-dialog navigation is not tested by this direct-fragment test. This test is pending hosted execution; no screenshot/e-ink performance claim.

Lint reports now include bounded issue-group counts/priorities/first locations so subsequent fixes can target actual causes. Malformed XML remains visible; full groups are saved in CI JSON. Two checker regressions added: 35 local checker tests pass; inventory 617 candidates / 9 contracts is current; unfinished scan 536 files / 178 findings / zero checker errors, still fails. No new runtime dependencies or warning waivers in this lint batch.

## Sequential-remediation instruction — active issue only

Saved pre-change audit and `quality/SEQUENTIAL_REMEDIATION.md`. Only the toolbar-border DrawAllocation issue is being changed; no next issue has started. Added density-aware dash reuse, scalar arc geometry and unboxed center iteration. Android tests compare 144 cases against test-only pre-fix stamp/dash/classic rendering and check effect identity/invalidation. Device harness collects synthetic border images even when unrelated checks fail, preserving failures; two mock harness regressions cover extraction success/failure without claiming Android execution.

Verification now runs release R8 without release APK assembly. Private test APKs remain necessary for instrumentation, but they are not shipped; signed assembly/publication remain behind the existing strict gates. No runtime dependency or suppression added. Local checker suite passes 38 tests; inventory remains 617/9; unfinished review remains failing (539 files / 178 candidates). Android rendering/cache tests and lint closure are pending hosted execution. No release is produced or offered.

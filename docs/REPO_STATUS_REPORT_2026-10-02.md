# SEARCHHH repository status and recovery strategy

**Date:** 2026-10-02  
**Repository:** `regularshowrigby8-create/SEARCHHH`  
**Package:** `info.plateaukao.einkbro`  
**Branch intended for work:** `arena/01a0ea36-searchhh`

## Executive summary

SEARCHHH is a renamed EinkBro Android application with additional SEARCHHH search/backend features. The project is not release-ready and no verified APK has been produced. The current state is not one problem: it is a legacy Android codebase, a large static-analysis debt inventory, unfinished-code candidates, incomplete interaction contracts, Android Lint debt, and a CI/repository transport problem.

The latest documented authoritative numbers are:

| Area | Current documented result | Meaning |
|---|---:|---|
| Detekt | 2,788 occurrences / weighted findings in the documented baseline; a later hosted run reported 2,708 weighted issues | Full-repository Detekt is failing; counts must be reconciled from one exact hosted report before claiming progress |
| Android Lint | app debug/release: 458 errors and 14 hints; `ad-filter`: 53; `adblock-client`: 52 in the earlier matrix | Lint is failing in multiple modules/variants; aggregate counts overlap by variant |
| Unfinished scanner | 178 candidates in the documented snapshot; the bounded callback repair work reduced the current local lexical result to 116 before the latest hosted run | The gate is fail-closed and includes production, imported, test and defensive cases |
| Interaction inventory | 617 candidates, 9 test contracts, 42 scenarios | Behavioral coverage is incomplete; this is not proof that the UI works |
| Kotlin source | approximately 69,064 lines across 421 Kotlin files in the supplied audit | Full default Detekt rules on inherited code creates a very large debt surface |
| Broad catches | supplied audit found 146 actual sites; previous project count was 142 | The count needs one authoritative scan and reconciliation |
| APK | 0 verified artifacts | No release link is authorized |

The numbers are not additive bug counts. Detekt, Lint variants, SARIF/XML, and report packets can contain overlapping occurrences. The first requirement is a single, immutable hosted report manifest with checksums and variant labels.

## Repository structure

### Android application

- `app/`: primary Android application, package `info.plateaukao.einkbro`.
- `ad-filter/`: Android ad-filter library/module containing Kotlin and JavaScript assets.
- `adblock-client/`: native/Kotlin ad-block client with imported C/C++ code.
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`: Gradle build and wrapper configuration.
- `quality/detekt.yml`: Detekt configuration; it builds on the default configuration and currently overrides only a small number of exception rules.
- `app/src/main/assets/`: imported JavaScript and browser assets; these are not equivalent to first-party Kotlin source and require provenance-aware treatment.
- `app/src/androidTest` and `app/src/test`: instrumentation and JVM test surfaces.

### SEARCHHH additions

- `app/src/main/java/.../searchhh/`: SEARCHHH activity, local backend, MCP/device-facing components, public search and relay behavior.
- `backend/`: Python backend/search logic and tests.
- `js-tests/`: JavaScript tests for browser/user-script behavior.
- `test_server/`: deterministic local pages and fixtures used by browser checks.

### Quality/factory system

- `tools/factory/`: tool registry, safety validation, report routing, repair handlers, packetization and release-chain logic.
- `tools/quality/`: unfinished scanner, classifier, evidence transport, interaction inventory and release helpers.
- `.github/workflows/`: Android verification, factory diagnostics, report recovery, evidence retrieval, external tool coverage and CodeQL.
- `docs/quality/`: historical reports, verification manifests, interaction inventory, issue templates and remediation backlog.

The factory is extensive, but it does not itself make semantic source repairs. It can collect, classify, packetize, validate and enforce gates. The actual Kotlin behavior changes still require a bounded source packet and hosted verification.

## Current build and quality problems

### 1. Detekt policy versus inherited source

The project uses:

```kotlin
detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("quality/detekt.yml"))
    ignoreFailures = false
}
```

This means the full default rule set runs against inherited upstream code. The dominant classes reported by the hosted run include:

- `MagicNumber`
- `LongMethod`
- `CyclomaticComplexMethod`
- `ComplexCondition`
- `TooGenericExceptionCaught`
- `NestedBlockDepth`
- naming and formatting rules

Large files identified in the audit include `SiteSettingsDialogFragment.kt`, `BackupUnit.kt`, `BrowserActivity.kt`, `OpenAiRepository.kt`, `EBWebView.kt`, and `Toolbar.kt`. Splitting these files is a real refactoring program, not a safe mass-auto-fix.

A Detekt baseline or relaxed thresholds would make CI pass by changing the measurement policy, not by resolving the code. Under the current acceptance criteria that is not permitted.

### 2. Android Lint

The documented Lint state includes:

- app: 458 errors and 14 hints in debug/release reports.
- `ad-filter`: 53 errors.
- `adblock-client`: 52 errors.

Important categories include resource/locale issues, WebView security/configuration findings, API/dependency issues, resource translation, storage ownership and variant-specific findings. Lint counts must be reconciled by module, variant, rule and fingerprint; a single aggregate count is insufficient for closure.

### 3. Unfinished-code gate

The scanner intentionally fails on markers and suspicious no-op constructs, including:

- TODO/FIXME/HACK/XXX and related unfinished markers.
- silent catches.
- empty action callbacks.
- disabled tests.
- empty/constant-return functions.

The snapshot contained 178 candidates. Classification was:

```text
REAL_DEFECT              73
DEFENSIVE_ERROR_HANDLING 27
UNKNOWN                  34
IMPORTED_CODE            36
TEST_DOUBLE               5
LEGACY_NEEDS_REFACTOR     3
```

A bounded automatic repair tool was added for explicit empty Kotlin callbacks and branches. It made 128 mechanically safe changes; the local lexical count fell to 116. This does not resolve the semantic remainder.

### 4. Broad exception handling

The supplied source audit found 146 broad catch sites, compared with the earlier project count of 142. The discrepancy must be reconciled by a reproducible scanner that records:

```text
file
line
exception type
operation shape
module
variant
recovery behavior
fingerprint
```

The first packets already repaired include:

- `TabConfig.kt`: narrowed JSON recovery to `SerializationException` and removed an unnecessary nested broad catch.
- `DownloadHelper.kt`: narrowed MediaStore cleanup, destination fallback, URL decoding, package metadata and DownloadManager cursor catches.
- `SupernoteStorage.kt`: narrowed SAF permission and picker-launch recovery.

These changes are source repairs, but they are not yet proven by the hosted Android gates.

### 5. Interaction coverage

The documented inventory contains 617 interaction candidates and only 9 contracts. There are 42 scenarios, with many unverified or unassigned interactions. This means a static-analysis cleanup alone cannot establish that the app is behaviorally release-ready.

### 6. Hosted evidence transport

GitHub repository/run APIs worked, but artifact and job-log blob downloads intermittently returned `EOF`. Retrying the same blocked blob endpoint is not a remediation strategy.

The workflow was changed to process reports on the hosted runner while XML is still local, publish compact exact findings to the GitHub job summary, and generate the remediation queue before artifact upload. A retrying artifact downloader and manual retrieval workflow were also added for transient cases.

### 7. CI and release

Recent runs have failed in combinations of:

- policy/source unfinished-code gate.
- Detekt.
- KtLint.
- Android Lint.
- device/evidence.
- release checks.
- factory diagnostics.
- external tool coverage.
- CodeQL.

The aggregate `Searchhh checks and APK` workflow has also shown startup failures. No release should be attempted until workflow validity, required-job dependencies and all quality gates are independently green.

### 8. Repository checkout integrity

At report-generation time, the local checkout itself reports the intended branch at the original `b1cfb32` commit while the work history has also been force-pushed through later commits. `git ls-files` reports only one tracked file while the source tree appears as untracked. This is a serious repository-state issue: it can cause a local commit to omit source, or a force-push to overwrite hosted work unexpectedly.

Before any further repair, reconcile local and remote ancestry and verify:

```bash
git status --short
git ls-files | wc -l
git rev-parse HEAD
git ls-remote origin refs/heads/arena/01a0ea36-searchhh
```

Do not create another broad force-push until this is fixed. Every repair commit must have an auditable parent containing the full source tree.

## What has actually been achieved

- Hosted Android matrix exists and remains authoritative.
- Report processing was moved onto the hosted runner to avoid depending on blob retrieval.
- KtLint formatting is separated from Detekt semantic mutation.
- Detekt autocorrect is explicitly prohibited by the factory.
- Unfinished findings are classified and packetized.
- 128 safe empty-callback repairs were applied.
- Several storage/download/configuration broad catches were narrowed.
- Dependabot and CodeQL workflows were added.
- Emulator-runner support already exists in Android workflows.
- Local Python quality suite has repeatedly passed 131 tests.

These are infrastructure and bounded source improvements. They do not constitute an APK release.

## Faster strategy to reach the first APK

The project is stuck because it is trying to close the entire historical quality backlog before producing a first verifiable build. The faster compliant strategy is not to lower gates; it is to establish a small, honest critical path and attack blockers in dependency order.

### Phase 0 — repair repository state immediately

1. Stop force-pushing until local/remote ancestry is reconciled.
2. Ensure the complete source tree is tracked from the intended branch parent.
3. Run `git fsck --full`, inspect `git status`, and compare `git ls-tree -r HEAD` with the checkout.
4. Preserve all existing hosted repair commits before continuing.
5. Create a clean repair branch state with no accidental mass file additions/deletions.

This phase is essential. A quality fix cannot be trusted if the repository snapshot itself is ambiguous.

### Phase 1 — make CI diagnosable and cheap

1. Validate every workflow with a YAML/action linter.
2. Make report-generation and job-summary steps `always()`.
3. Separate lanes so a Detekt failure does not prevent Lint/device/release evidence from being generated.
4. Make the required-quality job consume explicit job conclusions, not inferred artifact availability.
5. Run only the failed lane after each repair packet; reserve the full matrix for integrated milestones.
6. Keep CodeQL and Dependabot informational until their workflows are green and understood.

### Phase 2 — produce a first debug APK through a narrow chain

The first artifact target should be a hosted **debug APK**, not a release claim:

1. `compileDebugKotlin`.
2. `assembleDebug`.
3. JVM unit tests for app and libraries.
4. Android Lint debug reports.
5. artifact upload.
6. SHA-256 checksum.

The artifact must remain non-release and clearly labeled. This gives the project a real build milestone without bypassing quality gates or pretending release approval.

### Phase 3 — close high-confidence mechanical findings

Run bounded packets in this order:

1. KtLint formatting-only changes.
2. Import and naming repairs where compiler-safe.
3. Locale/resource path corrections.
4. obvious `NameNotFoundException`, `SerializationException`, `IllegalArgumentException`, `IOException`, `SecurityException` and `SQLiteException` narrowing when the API contract proves the types.
5. Android Lint resource and ownership fixes with tests.

Each packet should be 5–15 files, one rule/operation shape, and must include the exact failed gate rerun.

### Phase 4 — broad exception packets

Use the supplied operation-shape grouping:

1. `DownloadHelper.kt` remaining file/DownloadManager/network catches.
2. `BackupUnit.kt` file/archive/serialization catches.
3. `data/remote/` HTTP/JSON/serialization catches.
4. `unit/` remaining file and preference catches.
5. `browser/` WebView and JavaScript bridge catches.
6. `searchhh/local/` backend/relay catches.

For each catch, document whether the correct behavior is:

```text
propagate
retry
return typed failure
show user-facing error
log and recover
cancel coroutine
```

Never replace `Exception` with an arbitrary narrow type merely to reduce the count.

### Phase 5 — reduce the Detekt backlog by dominant rule and file

After the build and Lint path is stable, process the highest-yield rules:

1. `ThemedEdgeBorderView.kt`: extract border-specific drawing functions and named constants.
2. `BackupUnit.kt`: split archive, storage and migration operations.
3. `OpenAiRepository.kt`: separate stream parsing, retry/failure handling and event lifecycle.
4. `EBWebView.kt`: split WebView configuration, navigation, bridge and lifecycle behavior.
5. `SiteSettingsDialogFragment.kt` and `Toolbar.kt`: extract cohesive UI sections.
6. Address `MagicNumber` only where names communicate domain meaning; do not create meaningless constant aliases.

This reduces many repeated findings while keeping changes reviewable.

### Phase 6 — unfinished and interaction closure

1. Resolve remaining production empty actions with real behavior or explicit tested no-op contracts.
2. Repair silent catches by deciding the recovery contract.
3. Handle imported code only with provenance and a justified regression test; do not silently ignore it.
4. Add contracts for the highest-risk 617 interaction candidates, starting with search, relay, WebView, download, settings and navigation flows.
5. Use emulator tests for critical flows; use Paparazzi only as supplemental JVM visual coverage, not as device evidence.

### Phase 7 — release gates and APK

Only after the prior phases:

1. Full hosted debug matrix passes.
2. Device/evidence lane passes with nonzero verified test results.
3. Release unit tests and release Lint pass.
4. Release dependency/security checks pass.
5. Release APK is built on the hosted runner.
6. APK is retrieved through a validated path.
7. SHA-256 is independently recomputed.
8. Release metadata records commit, workflow run, artifact name, checksum and gate results.
9. Only then publish the APK.

## Recommended operating model

Use three lanes, not one giant all-or-nothing loop:

### Repair lane

Small bounded source packet, exact gate rerun, no release artifact.

### Build milestone lane

Compile/test/Lint and debug APK generation. This should run frequently and give the project a tangible artifact milestone.

### Release lane

Full device evidence, release checks, artifact retrieval, checksum and metadata. This remains fail-closed.

The key acceleration is to stop spending every cycle on documentation and aggregate backlog bookkeeping. Keep one machine-generated manifest, one packet queue, and one hosted summary. Spend engineering time on the top source packet and rerun its exact gate.

## Definition of done

The repository is not done when the number of findings decreases. It is done when:

- the source tree and branch ancestry are trustworthy;
- all required hosted lanes are green;
- unfinished-code findings are closed or individually justified with tests;
- Detekt and Android Lint reports are reconciled by exact fingerprint;
- device evidence has real executed tests;
- release checks pass;
- the APK is retrieved and checksum-verified;
- release metadata is complete;
- a link is published only for that verified artifact.

Until then, the correct status remains:

```text
PARTIAL / BLOCKED
release_approved = false
verified_apk = none
```

# Searchhh Quality Policy v1

These are contribution requirements, not a replacement for the upstream software license or a claim to compel public readers. Agents must read `AGENTS.md`; humans must read this policy before proposing changes.

## Required working sequence

1. Inspect settings/build files, catalogue, manifest, launcher, all affected screens/navigation/themes/state/repositories/WebViews/tests/CI/docs. Save `PROJECT_AUDIT.md`, including unfinished work, dead-action candidates, security and performance risks.
2. Execute baseline assembly, tests, lint and available connected tests; record exact commands, exits, errors and preexisting/environment distinctions in `BASELINE_VERIFICATION.md`.
3. Maintain `INTERACTION_INVENTORY.md` and machine-readable inventory. Every control needs label/tag, expected action, observable state and meaningful passing behavior test. Dynamic/custom controls require manual closure; the lexical index is not exhaustive proof.
4. Keep stable IDs in `SearchhhTestTags`; never count declared-but-unused tags as implemented features.
5. Resolve unfinished implementations and silent errors. `failOnUnfinishedCode` scans production and tests, including injected scripts, backend and checker code. Generated module build outputs are excluded; ignored new source files are still scanned. No debt allowlist. Only exact, documented legitimate cases with regression tests, owner and expiry may be excepted.
6. Compiler warnings, lint warnings/errors, Detekt and ktlint violations must fail. Do not add blanket suppressions or a new lint/Detekt debt baseline. The old lint baseline remains historical evidence and is no longer applied.
7. Behavior tests must exercise search/start/stop/filter/save persistence/details/routes/back/theme/browser schemes/loading/empty/error/retry/offline/expiry/duplicates/accessibility. Tests must perform actions and assert consequences, not merely render text.
8. Register real routes centrally; test every visible destination and safe unknown-route fallback. Preserve entity/URL identities. Close drawers when navigating. Absent routes remain missing requirements.
9. Use AndroidX WebKit; default to HTTPS, require explicit HTTP approval, reject unsupported schemes, cancel TLS errors, audit privileged bridges, test controls and reuse/restoration with fixtures. Do not add Chromium or APK secrets.
10. Trace UI event → state holder/ViewModel → repository → observed state. Verify cancellation, retry and durable saves; do not substitute optimistic labels for confirmed effects.
11. UI changes require real before/after evidence, reachable route/source and executed behavior tests. Inspect actual layouts/menus/animations/accessibility.
12. Require light/dark home, drawer, loading/results/empty/error/filter/saved/browser/settings visual evidence. Prefer established screenshot regression tooling when safe; deterministic used previews plus a documented limitation are an interim measure, not invented pixel verification.
13. Audit keys/recomposition/WebView reuse/images/animation/startup/memory/dependencies. Add benchmarks where practical; record actual measurements or an explicit blocker.
14. CI must run format, Detekt, lint, unfinished checks, unit tests, assembly and device/evidence checks, upload APK/reports where produced, and fail on errors. No `continue-on-error` or successful fallback masking a check.
15. `searchhhVerification` is the full gate. `searchhhJvmVerification` is a host-only subset and cannot establish complete verification. Emulator absence must not silently omit UI checks.
16. Repeat fix/test/review; install/launch/check crashes when devices exist. Review inventory and diff before declaring results.
17. Update `QUALITY_IMPLEMENTATION_REPORT.md` with files/dependencies/tests/results/routes/WebView/visual/performance/risks/exact next task. Distinguish implemented, tested and actually passing.
18. Acknowledge this policy in commits and PRs. Maintainers must configure protected reviews/checks, including CODEOWNERS review for gate changes. No worker may weaken gates to approve their own unfinished work.

## Commands and evidence

```sh
python3 -m unittest discover -s tools/quality/tests -v
python3 tools/quality/interactions.py --write  # after reviewing source changes
python3 tools/quality/interactions.py --check
python3 tools/quality/unfinished.py
./gradlew --continue searchhhVerification -PuniversalApk
```

Python 3.11+, JDK 17, SDK 36 and a connected emulator (CI: API 35) are required. No local secret/provider keys are required. The master gate starts provenance recording automatically and requires fresh tests. It does not invent missing screenshot tasks: visual requirements remain failing contracts until proper screenshot tests/artifacts are supplied.

`quality/required-scenarios.json` covers required scenarios, including features not yet implemented. `quality/interaction-contracts.json` maps source candidates to behavioral tests. The evidence checker requires a current-commit run marker, fresh JUnit timestamps, non-skipped passing outcomes and an action followed by an assertion **inside the declared test method**. These structural checks cannot prove arbitrary program semantics or prevent a malicious maintainer changing the checker. Human review and protected checks remain necessary. JavaScript/Kotlin lexical scanning is intentionally conservative, not a compiler replacement; regex/template-literal constructs and dynamic controls require review. Custom test drivers must extend the checker with negative tests, not bypass it.

The inventory contains preview and overlapping callback/widget candidates. A finding is not automatically a live dead button. Investigate its call site and actual route; do not reclassify production debt as a legitimate exception merely to pass.

## Recorded acknowledgment

Each non-merge commit must end with this trailer (a separate final paragraph):

```text
Searchhh-Quality-Policy: accepted-v1
```

Each PR author must check the exact policy line in `.github/PULL_REQUEST_TEMPLATE.md`. CI checks the PR body and new non-merge commits. This records a contributor's assertion; it does not establish legal consent, identity beyond GitHub/git attribution, or private understanding. Blank separation before generated co-author footers is accepted; prose mentions are not. Generated merge commits are not treated as new contributor attestations. New-branch/dispatch checks can only evaluate the selected commit; protected PR review is required to prevent bypass through alternate history.

## Administrator action — currently BLOCKED

Main protection read returned HTTP 403 (`Resource not accessible by integration`). Policy files are on the session branch, not automatically installed on main. No protection was configured in this session.

After reviewing/merging these rules, a repository administrator must:
- Require PRs, approving reviews, CODEOWNERS approval, resolved conversations, dismissal of stale approvals and review of the latest push. A sole maintainer needs an additional trusted reviewer if their own PRs would otherwise require self-approval.
- Require the **`required-quality`** check from `.github/workflows/android-verification.yml` (confirm the exact check name in GitHub; reusable callers prefix their job names). Also require the existing `framework`, `backend-integration`, `android` and `device-smoke` checks so optional-backend/browser regressions cannot bypass review via the Android-only aggregate. Require branches up to date. The legacy workflow must also be enabled for merge-group events before using a merge queue.
- Restrict direct/force pushes and deletions; apply rules to administrators with no discretionary bypass where supported.
- Protect workflow/quality/policy changes with CODEOWNERS. Ensure Actions is enabled for PRs and merge queues. Preserve the commit trailer in squash-merge messages, or use merge commits after checking the original commits.

CI can block publication in the configured workflow; it cannot stop arbitrary external releases, a fork, someone reading the code, or an administrator removing rules. Do not promise universal forced agreement.

## Required final response headings

```text
STATUS: PASS / PARTIAL / BLOCKED
SUMMARY:
FILES CREATED:
FILES MODIFIED:
DEPENDENCIES ADDED:
COMMANDS RUN:
PASSED:
FAILED:
DEAD BUTTONS FOUND:
UNFINISHED CODE FOUND:
UI CHANGES VERIFIED:
WEBVIEW CHECKS:
REMAINING RISKS:
NEXT TASK:
```

Choose one actual status. PASS is prohibited with any failing/unexecuted required check. NEXT TASK must be one specific action.

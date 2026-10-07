# Expanded factory: delivered automation and remaining gaps

**STATUS: PARTIAL.** The implementation is verified; the requested 70–80% effort
reduction is not yet measured. App/release approval remains blocked.

## What you can see

- [Searchable factory map](FACTORY.html): 65 engines, source repositories,
  responsibilities, profile triggers, evidence scope and human-owned gaps.
- [Complete tool/repository reference](FLEET.md): all 64 commands and ownership.
- [Measurement method](MEASUREMENT.md): empty real-sample ledger, not invented
  savings.
- [Plan](EXPANSION_PLAN.md): what is implemented versus deferred.

The dashboard is a versioned execution snapshot, not a live green status board.
It shows source `3f061ae`. A repeat at `1171a4c` confirms the same totals and
new-engine outcomes. Nine further candidate repositories are explicitly PLANNED
ONLY.

## What was actually added

- **Playwright — PASS.** Real Chromium dashboard actions and owned HTML retry
  state checks.
- **axe-core — FAIL.** Eleven accessibility rule/page violation records across
  three owned HTML assets.
- **OpenAPI Spec Validator — PASS.** Actual FastAPI schema grammar; not live
  endpoint behavior.
- **pipdeptree — PASS.** Conflicts/cycles in the real QA dependency environment.
- **Taplo — PASS.** Gradle TOML syntax/structure without Java.
- **npm audit — FAIL.** Development dependency audit; local report has three
  vulnerabilities: one moderate and two high.

Npm's audit engine is counted once, not as an additional wrapper or one tool per
subcommand. Dependencies, browser binaries, tests and the orchestration glue do
not inflate the 64-engine count. Package pins and locks are development-only.
Playwright selects a fixed browser revision; its managed download is not the
same separate digest-pinning scheme used for the five existing upstream
binaries.

## What the tools now do automatically

1. **Recommend and run checks from changed paths.** `automation plan/check` maps
   paths to profiles, records why and conservatively selects all diagnostics for
   unknown paths. Fixed scanner scopes still require review; this is not proof
   of complete changed-file coverage. Native/release gates are never skipped.
2. **Run on relevant pushes.** Factory CI now includes app, library, backend, JS
   tests, Gradle and factory changes on this session branch. It currently runs
   all 56 eligible jobs; local change routing is scoped. It does not claim a
   reduced CI bill or automatically enable default-branch schedules.
3. **Produce review packets.** Eight location formats are supported: Ruff,
   Bandit, ESLint, HTML Validate, Stylelint, Gitleaks, axe-core and npm audit.
   Checksums, paths and size bounds are checked. Messages/snippets/secret values
   are not copied. Unsupported outputs remain explicit opaque jobs; malformed
   evidence is an error, never an empty pass.
4. **Preserve evidence and failures.** Source fingerprints, actual exit codes,
   typed reports, bounded output, artifacts and page-complete receipts remain.
   Zero-exit secret findings, missing tools and failures cannot become green.
5. **Exercise real browser behavior.** Search/no-match/status/reset/focus/count
   behavior is tested in the generated map. Actual error-page retry disables and
   labels itself, and reload restores it. Native URI handling is NOT verified.

Six real failed local scans yielded 479 aliases in 156 provisional file/rule
packets, with zero parse errors after correcting the Stylelint stderr adapter.
All six original failures remain failures. This is not 156 unique bugs, semantic
closure, or a measured percentage of labor saved. See
normalization-evidence.json.

## Executed verification

[Repeat hosted run 36665725670](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36665725670)
at source `1171a4c` executes **56 of 65 engines: 24 PASS / 9 REPORTED / 23 FAIL,
zero BLOCKED**. All source fingerprints match; all six new engines execute. The
workflow stays FAILURE because diagnostics fail. Native Gradle 5 and APK
inspectors 3 are not counted as new executions. Existing mandatory workflows are
unchanged.

- 105 local checker tests PASS; all hosted checker-test steps PASS.
- Factory Pylint/Mypy/Pyright/docstrings and manifest schema checks pass.
- Dashboard jsdom behavior and hosted Chromium behavior pass.
- 105 is a test count, not an engine count or whole-app functional coverage.
- Current unfinished scan: 557 files / 178 findings / zero errors; FAIL.
- Interaction inventory: 617 candidates / 9 contracts, current but incomplete.
- Local master cannot configure without Java/JAVA_HOME.

[Repeat receipts](verification-1171a4c.json) and
[first expanded receipts](verification-FACTORY002.json) retain job/source/report
hashes. All pages, unique tool IDs, selected coverage and source stability were
reconciled before saving them.

Hosted artifacts include factory.png, retry-before.png, retry-after.png and the
browser/axe reports. Chromium download locally fails TLS; artifact download from
this sandbox still fails storage EOF. Images were captured by passing hosted
browser execution but were not manually inspected here. No whole-app/native,
TalkBack, physical-device performance or production-signing pass is claimed. The
local dashboard serves HTTP 200 and has working DOM-tested controls.

A routed docs check actually ran: 2 PASS / 2 FAIL / 1 BLOCKED, correctly
returning nonzero. The blocker was an absent local Codespell environment after
restoration; link checking is also locally TLS-limited. No successful fallback
masked these outcomes.

The reported dependency names are `brace-expansion` and `undici` (high), and
`ip-address` (moderate).
[Exact advisory/chain review inputs](npm-advisory-review.json) are saved without
applying upgrades or declaring exploitability.

## The gaps I still handle

- Confirm root causes, priorities and cross-report aliases; assess real
  exploitability.
- Review the new dependency advisories and accessibility findings; do not
  blindly upgrade packages, rename selectors or suppress warnings.
- Author meaningful missing scenarios and minimal patches; verify start/stop,
  cancellation, persistence, retry and recovery instead of existence-only tests.
- Connect device journeys, native WebView bridges, screenshot references and
  measured physical-device budgets to real fixtures.
- Review false positives, license obligations, third-party trust and release
  evidence.

Maintainers must approve protected reviews, provider/signup consent and signing
keys. Code/CI cannot force universal agreement or self-authorize a release.
Third-party tools are not OS-sandboxed; pins are not an exhaustive upstream
audit.

## Later candidates, not added or counted

CodeQL, Schemathesis, Maestro, Roborazzi, Android Macrobenchmark, Perfetto,
OWASP ZAP, k6 and Renovate. The map lists each source repository and the exact
fixture, environment or authorization gap. No bot was authorized to create or
merge branches, perform unrestricted scans or update production dependencies.

## 70–80% target: exact remaining acceptance

No real effort samples exist yet. `automation measure` returns NOT_MEASURED with
exit 2. Use at least 10 comparable real paired work items against the already
partly automated FACTORY-001 baseline at `6f94604`—not a hypothetical50-command
manual process. Capture active selection, invocation, reading and verification
effort, keep failures/blockers in scope, and retain required checks. Review the
method and evidence before claiming any percentage. Total engineering, product
design, security decisions and native validation are not covered by that metric.

Next:use the issue workflow during S00 security ownership review. The supervisor
now classifies the narrow Edge TTS public protocol constant without emitting its
value; the policy result is2 cleartext findings. The latest no-provision run
returned1PASS/3FAIL/3BLOCKED because Bandit and remaining diagnostics still have
findings or missing binaries. Provision those environments in hosted CI and
resolve the cleartext compatibility boundary. Record the first paired routine-
effort work item. No production build or unrelated repair should precede the
standing sequential review process.

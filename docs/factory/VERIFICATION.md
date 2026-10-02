# Factory execution evidence

**Overall: PARTIAL. Production release: BLOCKED.** The bounded infrastructure
implementation is verified; failing diagnostics are not resolved app issues.

## Latest hosted execution

- Source: `25f9401983de16fd5fd1b029411239d211787095`.
- [Run 36662473189](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36662473189).
- 58 distinct declared engines; **50 actually executed** in this run.
- **20 PASS, 9 REPORTED, 21 FAIL, zero BLOCKED** among those 50.
- Workflow conclusion: **FAILURE**, intentionally preserving diagnostic
  failures.
- All four installation/checker-test lanes completed successfully. The core lane
  passed; Python, node and binary diagnostic lanes failed on checks.
- All 50 source-before/source-after fingerprints match. No timeout or output
  overflow was reported. This is not an OS sandbox or an app certification.
- [Compact receipts](verification-25f9401.json) retain all pages, job IDs,
  source identity, command exits, report hashes/counts and result distinctions.
  Page completeness, unique IDs, source SHA and selected-tool coverage were
  checked against the registry before saving them. Raw scanner logs are not
  committed. Fresh generated reports remain under ignored build output.

The five Gradle/native integrations and three APK inspectors were **not run by
this factory batch**. They require native prerequisites or an existing real APK.
Do not add them to the executed count or substitute a debug APK. Existing
mandatory Android/backend/device/signing workflows are unchanged.

## Actual results

PASS means that declared scoped check passed, not that the application passes.
REPORTED means inventory, instrumentation, graph or diagnostic output only. FAIL
includes findings requiring review; overlapping tools are not additive
unique-bug counts. Counts alone never establish that a credential is live or a
control is dead.

| Engine                | Result   | Exit |
| --------------------- | -------- | ---- |
| `actionlint`          | PASS     | 0    |
| `adb`                 | REPORTED | 0    |
| `ast-grep`            | PASS     | 0    |
| `bandit`              | FAIL     | 1    |
| `check-jsonschema`    | PASS     | 0    |
| `checkov`             | FAIL     | 1    |
| `codespell`           | PASS     | 0    |
| `coverage`            | REPORTED | 0    |
| `cyclonedx-py`        | REPORTED | 0    |
| `dependency-cruiser`  | PASS     | 0    |
| `deptry`              | FAIL     | 1    |
| `detect-secrets`      | FAIL     | 0    |
| `docker-compose`      | PASS     | 0    |
| `eslint`              | FAIL     | 1    |
| `gh`                  | REPORTED | 0    |
| `git`                 | PASS     | 0    |
| `gitleaks`            | FAIL     | 1    |
| `hadolint`            | FAIL     | 1    |
| `html-validate`       | FAIL     | 1    |
| `hypothesis`          | PASS     | 0    |
| `interrogate`         | PASS     | 0    |
| `jest`                | PASS     | 0    |
| `jscpd`               | FAIL     | 1    |
| `knip`                | PASS     | 0    |
| `markdown-link-check` | PASS     | 0    |
| `markdownlint`        | FAIL     | 1    |
| `mutmut`              | REPORTED | 0    |
| `mypy`                | PASS     | 0    |
| `npm-check-updates`   | REPORTED | 0    |
| `osv-scanner`         | FAIL     | 1    |
| `pip-audit`           | PASS     | 0    |
| `pip-licenses`        | REPORTED | 0    |
| `prettier`            | PASS     | 0    |
| `pylint`              | PASS     | 0    |
| `pyright`             | PASS     | 0    |
| `pytest`              | PASS     | 0    |
| `retire`              | PASS     | 0    |
| `reuse`               | FAIL     | 1    |
| `ripgrep`             | REPORTED | 0    |
| `ruff`                | FAIL     | 1    |
| `semgrep`             | PASS     | 0    |
| `shellcheck`          | FAIL     | 1    |
| `shfmt`               | FAIL     | 1    |
| `stylelint`           | FAIL     | 2    |
| `syft`                | REPORTED | 0    |
| `trivy`               | FAIL     | 1    |
| `vulture`             | FAIL     | 3    |
| `xenon`               | FAIL     | 1    |
| `yamllint`            | FAIL     | 1    |
| `zizmor`              | FAIL     | 14   |

## Observations needing review

- Gitleaks produced **2 redacted candidates**, not two confirmed live secrets.
  The final binary artifact retains the fully redacted JSON. Never paste raw
  credentials into tickets. Artifact name: `factory-binary-36662473189`, path
  ending `gitleaks/gitleaks.json`.
- Bandit produced **12 findings with zero scanner errors** in the corrected
  local invocation; the hosted receipt independently confirms 12 report rows.
- Deptry reports 6 dependency observations; JSCPD reports 203 clone pairs.
  Review dynamic imports, CLI-only dependencies and duplication semantics before
  acting. Do not remove dependencies or rename selectors merely to silence
  tools.
- OSV, Trivy, workflow/container analysis and other security/style/license
  checks fail. Pin the source and inspect the actual report before disposition.
- CycloneDX and Syft produced 14 and 15 component entries respectively. Those
  inventories have different extraction scopes; neither is a vulnerability pass.
- Detect-secrets exited zero with candidates; the factory correctly marked FAIL.
- Mutation execution is REPORTED, not a test-adequacy pass. An earlier local
  scratch run produced 193 mutants: 40 killed, 59 surviving and 94 uncovered.
  Those numbers describe that run only. Broader mutation coverage remains work.

## Executed tests and scope

- 88 standard-library checker tests pass locally at the final tooling source;
  all four hosted checker-test steps also passed.
- Strict backend test invocation: 43 passed, without suppressing its earlier
  Starlette warning. Added the supported, pinned development-only HTTPX2 test
  client; backend runtime requirements were not changed.
- Jest: 5 suites / 13 tests passed. Hypothesis: 3 property tests passed.
- Factory Pylint, Mypy, Pyright, docstring, schema and formatter checks passed.
- Interaction inventory: 617 candidates / 9 contracts, current but incomplete.
- Unfinished scan: 553 files / 178 findings / zero checker errors; **FAIL**.
- Local master: exits before configuration because Java/JAVA_HOME is absent. No
  new native, device, visual, performance or production-signing pass claimed.

## Failed attempts retained, not counted as successes

1. Original local actionlint wrapper and four binary downloads failed TLS EOF.
   Replaced the wrapper with a digest-pinned official binary; hosted downloads
   work. A metadata/availability probe never counted as execution.
2. Hosted run36661053230 executed48/50: ripgrep was missing and OSV's archive
   label was inconsistent. Provisioned the hosted prerequisite and corrected the
   archive contract, with a regression checking accepted archive types.
3. Run36661509568 invoked50, but Bandit's argument placement still caused a
   usage error. Corrected and executed it against real sources; reports now
   contain findings, not a parser failure. Added portable REUSE encoding support
   after the minimal local host could not load libmagic.
4. Knip initially could not resolve the declared Jest workspace dependency.
   Provisioning now creates an ignored cache link only if no installation
   exists. It does not replace an existing developer installation or alter
   package.json.
5. Strict backend tests initially stopped on the supported-client deprecation.
   Installing the pinned test dependency fixed that setup without warning
   filters.
6. Authentication expired during publication. After the user reconnected,
   verified the remote ancestry and every published file, restored only Git's
   local history/index and preserved the four pending file changes. Normal
   branch-only push succeeded; no force push, branch switch or content reset.

Caches/build outputs do not survive every workspace restoration. On a fresh
checkout or restored session, run doctor and explicit locked provisioning again;
previous installation evidence does not imply tools remain installed now.

## Next bounded work

FACTORY-001 closes only the requested development-infrastructure implementation.
Resume S00 semantic ownership, reviewing the new security candidates first. Run
`python3 -m tools.factory.factory triage`: 3,509 suggested file/rule packets
preserve all 17,890 recovered raw observations. They do not close S00 or
establish unique root causes. Maintain WIP1 and the existing
issue-to-verification sequence. No APK was produced or approved as this task's
deliverable.

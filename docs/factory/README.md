# Searchhh development factory

**Development automation, not a release shortcut.** Sixty-four named upstream
engines are wired to real commands. Installed, executed, clean, reported, failed
and blocked are different states. See [TOOLS.md](TOOLS.md) for each integration.
No tool here is added to the Android runtime, and none signs or publishes an
APK.

See [executed results and limitations](VERIFICATION.md): the latest hosted batch
executed 50 engines, with 20 PASS, 9 REPORTED and 21 FAIL. The workflow stays
red.

## Expanded factory map

Open [the searchable factory](FACTORY.html),
[the full tool/repository map](FLEET.md) and
[the expansion plan](EXPANSION_PLAN.md). Six new engines and change routing,
report normalization and an effort ledger extend the original58. Versioned
results stay labeled; the70–80% routine-effort target is not measured yet. See
[expanded execution results and gaps](EXPANSION_RESULTS.md).

```sh
python3 -m tools.factory.automation plan --paths backend/searchhh/api.py
python3 -m tools.factory.automation check --paths backend/searchhh/api.py
python3 -m tools.factory.automation report \
  --summary build/reports/factory/RUN/summary.json
python3 -m tools.factory.automation overview
python3 -m tools.factory.automation measure
```

`check` actually runs selected tools and preserves blockers/failures. Unknown
paths select all diagnostics conservatively; this does not bypass canonical
release gates or claim every changed file is covered by fixed scanner scopes.
`report` supports eight location formats; unsupported outputs remain explicit. A
nonzero result remains nonzero after report processing. No automatic repair,
merge, credential rotation or signing is added.

The new browser engines require explicit Node provisioning followed by
`python3 tools/factory/install.py --ecosystem browser`; Linux host libraries are
also required. CI provisions these separately. Tests use owned HTML only, block
external network requests, and never impersonate a working native bridge.

## Start

```sh
# Inspect jobs without installing/running them.
python3 -m tools.factory.factory list
python3 -m tools.factory.factory doctor

# Explicit installation, using reviewed hash/integrity locks.
python3 tools/factory/install.py --ecosystem python
python3 tools/factory/install.py --ecosystem node
python3 tools/factory/install.py --ecosystem binary

# Fast checks, or one exact tool. Missing tools fail closed.
python3 -m tools.factory.factory run --profile fast --workers 3
python3 -m tools.factory.factory run --tools pytest,jest,hypothesis --workers 3

# Turn the recovered findings into small review packets, without losing aliases.
python3 -m tools.factory.factory triage
```

The first explicit lock creation/update uses `install.py --lock`. Review changes
under `tools/factory/locks` and `tools/factory/npm/package-lock.json` before
using new resolutions. Normal execution never installs or upgrades anything.

Linux x86-64, Python 3.11 and Node 22.22.3 are the tested provisioning targets.
The uv bootstrap wheel and upstream binary assets have pinned SHA-256 digests;
Python environments use hash locks, npm uses an integrity lock and disabled
lifecycle scripts. Hashes establish artifact integrity, not a complete security
review of every transitive dependency. Binary packaging wrappers are disclosed
in `tools/factory/source-audit.json`; they count as their single upstream tool,
not extra tools. Public package/version metadata was researched on 2026-09-30.

## Work sequence

1. Run `triage`; inspect `build/reports/factory/triage.json`. File/rule packets
   preserve every raw observation and flag security review first. They do not
   establish that all observations share a root cause or that a control works.
2. Claim **one** bounded ticket. Inspect source/callers and choose the suggested
   tools, then add the actual regression required by the quality policy.
3. Run the relevant profiles before and after a reviewed correction. Never run
   all tools on every edit; that wastes work and produces unrelated noise.
4. Inspect each result and raw log. Confirm the intended findings disappeared,
   preserve other regressions, then run canonical verification. Factory checks
   do not replace `searchhhVerification`, backend integration or signed smoke.
5. Update ticket/evidence/queue. Only then take the next root-cause ticket.

S00 semantic ownership remains open. Generated packet identifiers are grouping
keys, **not confirmed stable root-cause identities**. The new user-requested
factory task does not mark any of the 458 app lint errors fixed.

## Profiles and limits

- `fast`, `core`: short orchestration, syntax and source checks.
- `python`, `javascript`, `web-assets`, `tests`: scoped analysis and real
  suites.
- `security`, `supply-chain`, `containers`, `ci`: explicit security/config
  audits.
- `docs`, `triage`, `evidence`: documentation, localization and evidence
  support.
- `mutation`: mutate a **scratch copy** of the actual factory safety module;
  surviving mutants are review evidence, not proof that tests are sufficient.
- `android`: existing Gradle/Lint/Detekt/ktlint/R8 engines, run serially with
  JDK17/SDK36. No unsigned/release APK assembly is added.
- `device`: actual ADB device inventory, not a device-test pass.
- `apk-inspection`: APK Signer, APK Analyzer and AAPT2 require an existing
  repository-relative APK supplied with `--apk`. An inspected debug APK is not
  an approved release. Factory never fabricates a test APK to turn this green.

`--all` is explicit and will report blockers where prerequisites are absent. The
hosted factory workflow runs 56 non-Gradle/non-APK-inspection integrations; the
five native Gradle engines remain in existing mandatory CI and three APK
inspectors require a real artifact. It triggers for factory changes or manual
dispatch, not for every unrelated production edit. Findings keep jobs red.

## Evidence and safety

Each job writes `result.json` and bounded `output.log` / `stderr.log` files
under `build/reports/factory/<run>/<tool>/`. The run summary records command
arguments, installed specification, source fingerprints, elapsed time, return
codes and log hashes. Elapsed time is observed execution time, **not a claimed
speedup**.

- `PASS`: the declared check returned clean according to its result contract.
- `REPORTED`: an inventory/search/graph was generated; not a clean quality gate.
- `FAIL`: findings, command error, timeout, output overflow, malformed evidence
  or a source change. Failed reports never become a passing fallback.
- `BLOCKED`: tool, SDK, executable or real APK prerequisite is missing.

Commands are fixed argv arrays, not interpolated shell snippets. Common write,
release and publishing commands are rejected. Each job checks protected source
before/after; a mutation fails visibly and is **never automatically reset**.
This detects changes, rather than claiming to OS-sandbox arbitrary third-party
code. Do not edit source while a batch is running: unrelated concurrent edits
also invalidate its source-stability evidence.

Timeouts kill scanner process groups. Combined stdout/stderr are capped at 8 MiB
and overflow fails. Provider/signing credentials are not inherited by ordinary
scanner jobs; only the GitHub evidence job receives an existing read-only GitHub
token. Generated Compose validation credentials are ephemeral and removed after
the job. Secret scanners use hashes/redaction. Treat all diagnostic output as
potentially sensitive; do not publish it as release assets.

Caches, environments, archives, mutation workspaces and node modules live under
ignored `build/`, not source or APK assets. Provisioning creates an ignored
`js-tests/node_modules` link to the locked cache only when no installation
already exists, so Knip resolves the actual workspace dependencies. The factory
uploads only compact results/install diagnostics with short retention, not raw
source-scan logs or credentials. No existing warning threshold, test or release
gate is relaxed.

## Checks and research

```sh
python3 -m unittest discover -s tools/quality/tests -v
python3 tools/quality/interactions.py --check
python3 tools/quality/unfinished.py
./gradlew --continue searchhhVerification -PuniversalApk
```

The standard-library factory tests exercise failures, zero-exit secret findings,
source mutation, duplicate tool identities, missing SDK/APK, process timeouts,
log limits, path traversal, secret environment filtering and triage retention.
Hypothesis additionally generates hostile path/argument inputs. These do not
replace app/UI behavior tests.

Official references informing the integration:
[uv isolation](https://docs.astral.sh/uv/concepts/tools/) for tool isolation,
[ast-grep scans](https://ast-grep.github.io/reference/cli/scan.html) for
configured syntax scans, and [Zizmor](https://zizmor.sh/) for offline workflow
auditing. Exact package/release metadata and wrapper upstreams are retained in
`source-audit.json`.

## Automatic slow-issue supervisor

For a bounded issue, let the factory provision missing selected environments,
retry once and save the complete status packet:

```sh
python3 -m tools.factory.auto_issue ISSUE-ID --category security
```

Supported categories are `security`, `webview`, `api`, `ui` and `dependency`.
The supervisor has two total attempts and a 15-minute provisioning bound per
ecosystem. It provisions only the selected issue families, records installer
logs, reruns the checks and stops with `BLOCKED_OR_FINDINGS` when work remains.
`READY_FOR_REVIEW` means only that the scoped run returned zero; it is not a
release or semantic issue closure. It never edits source or auto-approves a
security exception. Use `--no-provision` when the environment is already ready.

## Automatic remediation planning

Every automatic issue run now writes a rule-specific remediation plan beside its
supervisor packet. It assigns an owner, file and line, next action, status and
whether automatic editing is allowed. Security actions intentionally set
`auto_fix: false`; the factory handles setup, reproduction and routing while a
reviewer handles credential/compatibility decisions. Blocked tools become
factory prerequisite actions instead of disappearing from the handoff.


## Perplexity plan comparison

The complete retrieval and implementation comparison is in
[PERPLEXITY_LINK_RETRIEVAL.md](../PERPLEXITY_LINK_RETRIEVAL.md). It records all 32
retrieved chunks, separates real current behavior from catalogues and planned
work, and prevents the proposed routes/libraries/sources from being claimed as
implemented merely because they were named.

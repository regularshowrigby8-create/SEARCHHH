# A1 — CI baseline before the Lane A change

Measured 2026-10-07 (UTC) from GitHub run history, not inferred. Source of truth:
`gh api repos/regularshowrigby8-create/SEARCHHH/actions/runs` (378 runs fetched) and the
per-run job endpoints. Raw job **logs** were not retrievable (blob-storage `EOF`, see
`capabilities.md`), so every statement below comes from run/job/step metadata.

Baseline tip of the content branch: `37833d368434b54c96870862b7e0e2e47da307e5`
(`arena/3674d801-searchhh`, pushed 2026-10-07T01:56:17Z).

## Runs per push

The last three pushed SHAs on `arena/3674d801-searchhh`, with PR #2 open:

| Pushed SHA | Runs triggered for that SHA | Breakdown |
|---|---:|---|
| `37833d368` | 5 | Android verification ×2 (push) + Android verification (pull_request) + Searchhh checks and APK (pull_request) + CodeQL (pull_request) |
| `7cd2f67b3` | 4 | Android verification (push) + Android verification (pull_request) + Searchhh checks and APK (pull_request) + CodeQL (pull_request) |
| `11af91c4c` | 3 | Android verification (pull_request) + Searchhh checks and APK (pull_request) + CodeQL (pull_request) |

**4 runs per push** is the steady state (3 when the push is `[skip ci]`-marked, which
suppresses the push-triggered run only). One of the two `37833d368` push runs was
cancelled by the called workflow's own `cancel-in-progress` after 0.3 min — the two runs
were both for `event=push` on the same ref, so the group `android-quality-<workflow>-<ref>`
cancelled one *within* the same event type instead of ever merging the push and PR runs.

Repo-wide totals by workflow/event/branch (378 runs):

| Workflow | Event | Branch | Runs |
|---|---|---|---:|
| Searchhh checks and APK | push | `arena/01a0ea36-searchhh` | 118 |
| Android verification | push | `arena/01a0ea36-searchhh` | 104 |
| **Android verification** | **push** | **`arena/3674d801-searchhh`** | **9** |
| Searchhh checks and APK | pull_request | `arena/3674d801-searchhh` | 8 |
| Android verification | pull_request | `arena/3674d801-searchhh` | 8 |
| Android verification | merge_group | any | **0** |

The caller (`buid-app-workflow.yaml`) has **no push run at all** on `arena/3674d801-searchhh`,
because its filter is `branches: [arena/01a0ea36-searchhh]`. On the branch the project
actually works on, the only verification is the duplicate-prone `Android verification` pair.

## Emulator sessions per push

`device-and-evidence` (reusable workflow) and `device-smoke` / `release-smoke` (caller) are
the three jobs that boot `android-emulator-runner`.

* Measured on the 12 newest completed runs of the baseline tip: **6 executed emulator
  sessions**, i.e. **3 per push** — one in the push verification run, one in the PR
  verification run, one in the caller's nested `quality / device-and-evidence`.
* `device-smoke` and `release-smoke` were **skipped in every one of those runs**, because
  `required-quality` fails first and the chain is sequential. With a green quality gate the
  same push would boot 4 sessions (3 + `device-smoke`).

So the current steady state is **3 emulator sessions per push**, not "~4-7". 4 is the
measured ceiling once `quality` passes (the `release-smoke` session additionally requires a
release-eligible event). The historical 6-session figure quoted in the task brief was **not
reproduced** in the runs examined here.

## Per-job durations (baseline)

Across the 12 newest completed runs on the baseline tip, executed (non-skipped) jobs only:

| Job / matrix lane | n | total min | avg min | max min |
|---|---:|---:|---:|---:|
| `device-and-evidence` (emulator) | 9 | 83.4 | 9.3 | 12.1 |
| `android-host (release-checks)` | 9 | 73.1 | 8.1 | 10.4 |
| `android-host (build-test-lint)` | 9 | 48.1 | 5.3 | 7.2 |
| `android-host (formatting, ktlintFormat)` | 9 | 19.9 | 2.2 | 3.0 |
| `android-host (detekt)` | 9 | 18.1 | 2.0 | 2.5 |
| `policy-and-source` | 9 | 1.2 | 0.1 | 0.2 |
| `required-quality` | 9 | 0.5 | 0.1 | 0.1 |
| `Quality summary` | 9 | 1.2 | 0.1 | 0.1 |
| CodeQL `java-kotlin` (out of scope, for context) | 3 | 56.0 | 18.7 | 20.6 |

Wall clock per run: **avg 12.1 min**, min 0.3 (cancelled), max 20.6 (CodeQL).
Verification runs specifically: **10.5–11.3 min**; caller PR run: **10.1–12.4 min**.

Aggregate cost of one push, in runner-minutes: 83.4/9 + 12.1 ≈ **~21 runner-minutes per
verification run**, ×2 duplicate runs ≈ **~42 runner-minutes per push**, plus the caller's
nested repeat of the same lanes (≈ +20). `detekt`+`formatting` = 4.2 min of that is
re-executed inside `device-and-evidence`'s own `searchhhVerification` call.

## What this baseline proves about the waste

1. The `push` and `pull_request` triggers of `android-verification.yml` produce **two
   identical full verification runs** for the same SHA, and its concurrency group keys on
   `github.ref`, which differs between `refs/heads/...` and `refs/pull/N/merge`, so it
   cannot deduplicate them.
2. `device-and-evidence` (the most expensive job, avg 9.3 min) re-runs the *whole* JVM gate
   because `tools/quality/device_verification.sh` calls `searchhhVerification` →
   `searchhhJvmVerification` (detekt, ktlint, lint ×3, unit tests ×3, assembleDebug) — the
   same work the four `android-host` lanes already did.
3. The `formatting` lane costs 2.2 min, cannot fail on findings (`exit 0`), and no other
   lane consumes its output.
4. `release-build` / `release-smoke` / `publish-release-apk` executed **0 times in 127
   completed caller runs** (89 had the jobs; all skipped), so they burn no CI minutes today —
   the waste there is latent (a fixed `v0.4.1` tag and file name), not current.

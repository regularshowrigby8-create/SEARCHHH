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

## After — measured on the first hosted run of the reworked chain

Head SHA `1ae2f445fbcc440670c183174fc970823ef51fe3` on `arena/132b2d0f-searchhh`.
Run [37564054844](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/37564054844)
(`Searchhh checks and APK`, `pull_request`, 2026-10-07 02:52:22 → 03:03:21 UTC), jobs read
from the Jobs API with per-step conclusions and check annotations.

| job | min | conclusion |
|---|---:|---|
| `quality / policy-and-source` | 0.18 | failure — step 7 `Reject unfinished production and test code` only; steps 4-6 (131 checker tests, acknowledgment, inventory) passed |
| `quality / android-host (detekt, detekt ktlintCheck)` | 2.55 | failure |
| `quality / android-host (build-test-lint, …)` | 5.92 | failure — app lint 465 errors + 14 hints, `:ad-filter:lintDebug` 53, `:adblock-client:lintDebug` 52 |
| `quality / android-host (release-checks, …)` | 10.02 | failure |
| `quality / device-and-evidence` | 9.12 | failure |
| `quality / required-quality` | 0.07 | failure (fail-closed, unchanged) |
| `quality / Quality summary` | 0.12 | success |

Wall clock 10.98 min. Executed runner-minutes 27.97. Seven caller jobs (`framework`,
`backend-integration`, `android`, `device-smoke`, `release-build`, `release-smoke`,
`publish-release-apk`) were `skipped`, exactly as in the baseline.

Two further samples on the follow-up head SHA
`71601d11f2db50a51aa6f1d72614a1fc2a67313c`, after the concurrency correction: push run
[37565529998](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/37565529998)
(wall clock 10.4 min, 27.65 runner-min) and pull-request run
[37565533435](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/37565533435)
(10.75 min, 25.88 runner-min). Both completed without cancelling each other, both still show
`Android verification` runs for the branch = **0**, and both have the same job/step shape as the
first run.

Deltas against the table above, each tied to the step/annotation that shows it:

* **Runs per pushed SHA: 4 → 2** (one `push` chain on the exact head SHA + one `pull_request`
  chain on the merge ref), and **1** for a branch with no open PR. No `Android verification` run
  exists for this SHA at all, which was the point of A2: the old shape ran that workflow directly
  *and* through the caller, twice per event.
  The first iteration keyed concurrency on `github.workflow` + `head_ref || ref_name` only, and run
  [37563976951](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/37563976951)
  was caught **cancelled by the PR run** the moment PR #3 opened (02:53:00). That is dedupe, but it
  was too much: a `pull_request` run checks out `refs/pull/N/merge`, so cancelling the branch-`push`
  run removes the only evidence recorded against the exact head SHA that `required-quality` is
  judged on. The event is therefore part of the group key in `a709e01`+ (see the follow-up commit),
  which keeps the win — repeated pushes to the same ref still supersede their own kind — and leaves
  the exact-SHA evidence intact. Recorded here because the mistake and its correction are both
  evidence about the gate.
* **Emulator sessions per pushed SHA: 3 executed → 2** while a PR is open (one per surviving
  chain), and 2 → 1 for a branch with no PR. Per chain it is now always exactly 1, and the device
  job's duration is unchanged (9.42 and 8.42 min against a 9.3 min baseline average), i.e. the
  session got lighter in duplicated work, not shorter by skipping its own job.
* **Host lanes 4 → 3**, and ktlint now bites instead of self-healing: the detekt lane carries
  `> Task :ktlintKotlinScriptCheck FAILED`, `:ad-filter:ktlintMainSourceSetCheck FAILED`,
  `:adblock-client:ktlintMainSourceSetCheck FAILED` and fails the job.
* **No host static check runs inside the emulator session any more.** Baseline device-job
  diagnostics listed `:failOnUnfinishedCode FAILED`, `:app:ktlintAndroidTestSourceSetCheck FAILED`
  and `:detekt` "Analysis failed with 2602 weighted issues"; the reworked device job lists only
  `> Task :verifyInteractionEvidence FAILED`. The split removed duplicated *work*, not coverage:
  `evidence.py` reads only `app/build/test-results` and `app/build/outputs/androidTest-results`,
  and the same run still recorded fresh app JVM results beside a live device
  (`test_counts: {passed: 371, failure: 0, error: 0, skipped: 0}`).
* **Per-chain runner-minutes went up, and that is not explained away.** Three samples after the
  change: 27.97 (`1ae2f44` PR), 27.65 (`71601d1` push), 25.88 (`71601d1` PR) against a 21.0
  baseline average — driven by `release-checks` 10.0/8.1 and `build-test-lint` 5.9/5.3
  (after/before averages), neither of which this change touches, plus `ktlintCheck` joining the
  detekt lane. The saving therefore comes from chain *count*, not chain cost: per pushed SHA with
  an open PR ≈84 → ≈54 executed runner-minutes (4 chains → 2), and ≈42 → ≈27 for a branch with no
  PR. Three samples on two SHAs is still thin; re-measure over ≥5 pushes before quoting any trend,
  and if per-chain cost stays above ~25 min the next task is release-checks, not the lanes A
  changed.
* **Nothing was relaxed to get there.** `required-quality` still fails, everything downstream is
  still skipped, and no baseline, `continue-on-error` or suppression was added.

### Forward consequence of deleting the `formatting` lane (for tickets C and E)

`quality-formatting.log` and `android-formatting-<run_id>` are no longer produced for new runs.
`quality-report-recovery.yml` is unaffected because its matrix pins historical run ids
(`android-formatting-36628443911`, `android-detekt-36628444279`) and those artifacts are
immutable and still downloadable. But from now on a `format`-group recovery of a *new* run must
read `android-detekt-<run_id>` (`report_transport.selected()` accepts
`*/build/reports/ktlint/*.txt`), and `report_transport.py`'s console-only fallback — which
requires a file literally named `quality-formatting.log` plus `format_report_mode:
console-only-no-machine-report` — can no longer be satisfied. Naming is intentionally untouched
here; the parser/format decision belongs to ticket E.

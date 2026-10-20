# PROPOSAL (not applied): replace the hard-coded `arena/01a0ea36-searchhh` push filter with `arena/**`

**Status: proposal only. Nothing in this patch has been applied to the repository.**
`AGENTS.md` is a policy file and the workflow triggers define the release gates, so both are owner-owned;
`AGENTS.md` itself says to write a proposal and ask rather than edit. The file
`docs/ci-plan/proposals/arena-all-triggers.patch` next to this document is the same diff in
`git apply` form, verified with `git apply --check` at commit `c69b650` (exit 0, clean apply) and
then confirmed unapplied (`grep -c` still finds the old branch name in all six files).

## Why: the named branch is frozen, so four of the five workflows have not run in 5 days

Measured through the GitHub API on 2026-10-07, all runs ever recorded per workflow:

| workflow | runs | branches ever seen | last run |
|---|---:|---|---|
| `factory.yml` | 41 | `arena/01a0ea36-searchhh` only | 2026-10-02 `e745e4e` |
| `codeql.yml` | 33 | `01a0ea36` ×25, `3674d801` ×8 | 2026-10-07 `37833d3` |
| `quality-report-recovery.yml` | 20 | `arena/01a0ea36-searchhh` only | 2026-10-02 `4729595` |
| `debug-apk-milestone.yml` | 19 | `arena/01a0ea36-searchhh` only | 2026-10-02 `9c205f3` |
| `external-tool-coverage.yml` | 9 | `arena/01a0ea36-searchhh` only | 2026-10-02 `e745e4e` |

`refs/heads/arena/01a0ea36-searchhh` is still at `9c205f33f5bba71157df130bb3f7782abf534481` and has not
moved, while the two branches actually under work are `arena/3674d801-searchhh` (`37833d3`) and
`arena/132b2d0f-searchhh` (`c69b650`). Of the 27 runs recorded on the branch this session works on,
**only 2 of the 9 workflows ever fired** — `Searchhh checks and APK` (22) and
`Android SDK provisioning diagnostic` (5) — because `buid-app-workflow.yaml` is the one file already
using `arena/**` (line 7). The other four have been silent since the branch they name stopped moving.

Note the single exception proves the mechanism rather than the filter: CodeQL's 8 runs on
`arena/3674d801-searchhh` came from its `pull_request:` trigger, not from `push:` — its `push:` filter
has never matched a live branch.

## Consistency argument

`buid-app-workflow.yaml` already carries this exact change, with a comment explaining it:

> `# arena/** instead of the stale single-branch name arena/01a0ea36-searchhh, which no`
> `# longer matches the branch under work (arena/3674d801-searchhh per BASELINE_VERIFICATION).`

So today the repo has one workflow that follows every `arena/*` lane and five that follow a dead branch
name. The patch makes the six agree, and updates the one sentence in `AGENTS.md` that instructs agents
to push only to that dead branch — the sentence an agent following literally today would push to
`arena/01a0ea36-searchhh` and get no CI at all.

## Pattern semantics, so the reviewer can check it without looking anything up

* GitHub filters `push.branches` against the branch name with fnmatch-style globs: `*` stops at `/`,
  `**` crosses it. `arena/**` therefore matches `arena/132b2d0f-searchhh` **and** a nested
  `arena/team/thing`, while `arena/*` would match only the first. It does **not** match a branch
  literally named `arena`.
* `arena/**` is what `buid-app-workflow.yaml` already uses, so this patch does not invent a dialect.
* Negation (`!pattern`) and YAML list form are both supported; no other key is touched.

## Consequences a reviewer should weigh before applying

1. **Runner cost moves, it does not shrink.** `external-tool-coverage`, `factory`,
   `debug-apk-milestone` and `quality-report-recovery` all carry `paths:` filters, so they still run only
   on relevant changes. **`codeql.yml` has no `paths:` filter on its `push:` block** — after this patch
   every `arena/*` push, including documentation-only commits, starts a CodeQL analysis. That is the
   expensive one: `docs/ci-plan/baseline.md` measured the longest single run in this repo at 20.6 min
   and attributes it to CodeQL, and the fast PR lane was scoped precisely to avoid paying that per
   push. Recommended companion change, deliberately **not** included in the patch because it is a second
   decision: add to `codeql.yml` under `push:` a
   `paths-ignore: ['docs/**', '*.md', '.github/workflows/**']` block, or key CodeQL to
   `merge_group`/`pull_request` only.
2. **Other agents' lanes start consuming the same queue.** `arena/**` matches every lane, including
   branches this session does not own. That is the intent (lanes are `arena/*` by construction), but it
   means concurrency groups and artifact names must stay unique per run id — they do today
   (`*-<run_id>` naming), which is why the patch is safe on that axis.
3. **`debug-apk-milestone.yml` is the owner's APK transport.** It is in the patch because the task asked
   for "the five other workflows", but it was previously declared out of my scope to change. If you
   want it left alone, delete that one hunk (`git apply --exclude=.github/workflows/debug-apk-milestone.yml`).
4. **`quality-report-recovery.yml` keeps pinning old run ids.** It hard-codes
   `android-formatting-36628443911` / `android-detekt-36628444279` as its recovery source; widening the
   branch filter does not make those pins live, it only makes the workflow run again on new pushes. Its
   artifact names are exactly the ones I must not rename, so this patch does not touch them.
5. **This does not fix `workflow_dispatch` or `schedule`, and no filter change can.** Those two triggers
   are resolved against the **default branch**, and `main` here contains only `README.md` (verified:
   `GET /contents/.github/workflows?ref=main` → 404; `GET /git/trees/b1cfb32a…` → one blob). Consequences
   that stay true after this patch is applied: `codeql.yml`'s `schedule: cron '23 3 * * 1'` never fires,
   and no `workflow_dispatch:` in the repository can be used — the whole history has
   **0 dispatch runs and 0 schedule runs** (`GET /actions/runs?event=workflow_dispatch` → `total_count: 0`,
   same for `schedule`) against 362 `push` and 43 `pull_request` runs. The rule, the 403 that blocked my
   live test of it, and four ways to resolve it are in the companion proposal
   `dispatch-and-default-branch.md` in this same directory.

## How to apply, when you decide to

```bash
git apply --check docs/ci-plan/proposals/arena-all-triggers.patch   # dry run; remove --check to apply
# or apply a subset:
git apply --exclude=.github/workflows/debug-apk-milestone.yml docs/ci-plan/proposals/arena-all-triggers.patch
python3 -c "import yaml,glob; [yaml.safe_load(open(f)) for f in glob.glob('.github/workflows/*')]"  # parses
git push                                                             # then: gh run list --workflow=factory.yml
```

Reversal is one line per file: put the old `branches:` value back; no state, no data, and no gate
depends on the new pattern.

## The diff

```diff
--- a/AGENTS.md
+++ b/AGENTS.md
@@ -35,6 +35,6 @@
 - First assignment: **S00, reconcile semantic issue ownership and cross-report duplicates** using `docs/quality/recovered-6abf29b/README.md` and its verified raw occurrence export. Full report recovery is closed; the old 28 omitted lint groups are now recovered. Raw records are not stable root-cause tickets. Review newly exposed security findings before confirming the queued S01 mirrored-background correction.
 - Preserve prior border/SAF/EXIF regressions, strict thresholds, stable app identity and production signing. No release or debug substitute between repairs.
 - Update ticket/index, queue, sequential log and implementation report with code SHA, run/check IDs, failures and the exact next action. Never infer a passing gate from counts, absent annotations or old evidence.
-- This handoff uses branch `arena/01a0ea36-searchhh`; push only that branch without force or switching branches. Observe all existing policy acknowledgment and review requirements.
+- Push only to the single `arena/*` working branch assigned to this task (CI accepts any branch under `arena/`); never force-push, and never switch the working branch mid-task. Observe all existing policy acknowledgment and review requirements.
 
 Repository policy/CI cannot force readers or forks to agree. Required review/check enforcement needs GitHub administrator protection; do not claim it is active without evidence.
--- a/.github/workflows/codeql.yml
+++ b/.github/workflows/codeql.yml
@@ -1,7 +1,7 @@
 name: CodeQL security analysis
 on:
   push:
-    branches: [arena/01a0ea36-searchhh]
+    branches: [arena/**]
   pull_request:
     branches: [main]
   schedule:
--- a/.github/workflows/debug-apk-milestone.yml
+++ b/.github/workflows/debug-apk-milestone.yml
@@ -2,7 +2,7 @@
 # Hosted debug artifact retrieval is intentionally external to this workflow.
 on:
   push:
-    branches: [arena/01a0ea36-searchhh]
+    branches: [arena/**]
     paths:
       - 'app/**'
       - 'ad-filter/**'
--- a/.github/workflows/external-tool-coverage.yml
+++ b/.github/workflows/external-tool-coverage.yml
@@ -1,7 +1,7 @@
 name: External tool coverage
 on:
   push:
-    branches: [arena/01a0ea36-searchhh]
+    branches: [arena/**]
     paths:
       - '.mega-linter.yml'
       - 'app/**'
--- a/.github/workflows/factory.yml
+++ b/.github/workflows/factory.yml
@@ -2,7 +2,7 @@
 "on":
   workflow_dispatch:
   push:
-    branches: [arena/01a0ea36-searchhh]
+    branches: [arena/**]
     paths:
       - "tools/factory/**"
       - "tools/quality/tests/test_factory*.py"
--- a/.github/workflows/quality-report-recovery.yml
+++ b/.github/workflows/quality-report-recovery.yml
@@ -1,7 +1,7 @@
 name: Recover existing quality reports
 on:
   push:
-    branches: [arena/01a0ea36-searchhh]
+    branches: [arena/**]
     paths:
       - .github/workflows/quality-report-recovery.yml
       - tools/quality/report_transport.py
```

---

Generated by the Lane A/E tooling pass. Numbers above were read from the GitHub REST API on
2026-10-07 against `regularshowrigby8-create/SEARCHHH`; the last-run SHAs are `head_sha` values from
`GET /actions/workflows/{id}/runs`. No file outside `docs/ci-plan/proposals/` was changed by this
proposal, and no policy file was edited.

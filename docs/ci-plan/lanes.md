# CI speed-up lanes, scope and open defaults

Mirror of the Linear project **SEARCHHH CI speed-up** (`e45e48a2-7e78-45eb-a162-5b03005c95d8`,
team `Savagee`): Lane A = `SAV-12` (In Progress), and `SAV-13`…`SAV-17` = Lanes B…F (Backlog).
All six are linked to `SAV-9`; `SAV-12` blocks `SAV-13` and `SAV-15`, and `SAV-13` blocks
`SAV-17`. A "CI speed-up — Lane A (executed)" section was also appended to the Notion page
`SEARCHHH Execution Plan` (`3f2cc7cc-dade-81d3-935f-c935ebbe47a0`), linking the GitHub runs
rather than restating their status.
Kept in-repo because Linear can be unavailable to a reviewer and because `AGENTS.md` requires
the plan of record to travel with the code. **Only Lane A is executed in this run**; B–F are
tickets, not work in progress (`AGENTS.md` work-in-progress limit: one root-cause ticket).
Lane A's implementation and its measured after-state are recorded in
`docs/ci-plan/baseline.md` ("After" section); its per-fact verification is in
`docs/ci-plan/fact-check.md`.

| Lane | Scope | Done when | Blockers | State here |
|---|---|---|---|---|
| **A** | De-duplicate triggers, move concurrency to the caller, split the device lane, drop the `formatting` lane, gate releases on tag/dispatch, `contents: read`, SDK diagnostic | One push → one verification run and one emulator session | none | **Executed, pushed (`1ae2f44`), measured; `SAV-12` stays In Progress pending owner review** |
| **B** | Fast PR lane that reports `PARTIAL` and never release approval | Feedback time measured and a target agreed | A | Backlog, not started |
| **C** | OSV-Scanner + gitleaks on PRs | Both run and findings are reported | none | Backlog, not started |
| **D** | Trial a nested-virtualization runner for the emulator job only (Depot / Namespace / RunsOn / Blacksmith) | Emulator job time compared before/after | A + baseline timings | Backlog, not started |
| **E** | Triage lint/Detekt debt into fixes vs fingerprinted exceptions (`AGENTS.md` rule 4); decide whether `app/lint-baseline.xml` is active and allowed | Each finding routed; baseline decision recorded | none | Backlog, not started |
| **F** | Trial one AI reviewer | Trial reported with accept/reject data | B | Backlog, not started |

## Defaults assumed in this run — **pending owner confirmation**

These are recorded so that silence is not mistaken for agreement. None of them weakens a gate,
so execution proceeded on the parts where they are not load-bearing.

1. **A fast PR lane that reports `PARTIAL` and never release approval is acceptable.**
   Used only as a scoping boundary for Lane B; Lane A does not create a partial lane and does
   not let a PR imply release approval. `required-quality` stays fail-closed.
2. **Detekt keeps all default rules — no tuning in this run.**
   Confirmed held: `quality/detekt.yml` is untouched, `buildUponDefaultConfig = true`,
   `maxIssues: 0`, `ignoreFailures = false`.
3. **Releases run only on a pushed tag matching `v*` or `workflow_dispatch`.**
   Implemented in A5. This is *stricter* than today for the branch-push path and is the item
   most likely to need an owner override, because the release jobs currently never execute at
   all (fact 4), so no existing release cadence is preserved by this change.

## Deliberately out of scope this run

`debug-apk-milestone.yml` (the owner's APK transport), `codeql.yml`, `factory.yml`,
`external-tool-coverage.yml`, `evidence-retrieval.yml`, and `quality-report-recovery.yml`.
Observations about them are filed as comments on tickets C and F, not as edits:

* `codeql.yml`, `factory.yml`, `external-tool-coverage.yml`, `debug-apk-milestone.yml` and
  `quality-report-recovery.yml` all repeat the same hard-coded
  `branches: [arena/01a0ea36-searchhh]` filter, which is why the caller never runs on the
  branch actually under work. A repo-wide branch-filter change is a policy-adjacent edit to the
  owner's transports and is left for the owner.
* `factory.yml` also carries `paths:` filters including `app/**` and `*.gradle.kts`, so it
  re-runs 50 tool engines on changes that also trigger the quality chain — the largest
  remaining duplication, out of scope by instruction.
* `debug-apk-milestone.yml` and `quality-report-recovery.yml` both use
  `permissions: contents: write` / `actions: read` at workflow level; the milestone workflow
  creates `debug-apk-<sha>` releases on every qualifying push. Left untouched by instruction.

## A7 follow-up: a dispatch-only probe cannot run from a branch

The brief asked for a throwaway **dispatch-only** diagnostic. Implemented that way first, then
found to be inert by measurement rather than assumption:

* `gh workflow run android-sdk-provisioning-diagnostic.yml --ref arena/132b2d0f-searchhh` →
  `HTTP 404` from `GET /repos/…/actions/workflows/<file>`.
* `GET /repos/…/actions/workflows` lists **7** registered workflows — every other file in
  `.github/workflows/` — and not this one.

Registration of an Actions workflow is lazy: with no file on the **default branch** and no runs
yet, the workflow is absent from `GET /actions/workflows`, has no entry in the Actions UI, and
`workflow_dispatch` has no endpoint to hit. So a probe that lives solely on a feature branch and
triggers only on `workflow_dispatch` can never answer its own question. It registered itself into
`active` state (id 377047595) the moment the trigger below produced its first run, from this same
branch — i.e. the fix was to give it any trigger that can fire from the branch; dispatch works
afterwards too.

Resolution, keeping the brief's intent: `workflow_dispatch` stays (it works once the file reaches
the default branch), and a **path-scoped `pull_request`** trigger is added so the PR that
introduces the probe produces the answer. Safety properties unchanged: no checkout, no secrets,
5-minute timeout, `contents: read`, it gates nothing, and every branch of the step ends in
`echo`, so it exits 0 whether or not the packages exist — it cannot add a red check to a PR.
The path filter is limited to files that could change the answer, so unrelated PRs never pay for
it, and no push ever does.

The provisioning step in `android-verification.yml` and its "Verify imported Android SDK" guard
remain in place regardless of the result; removing them is an owner decision, because the runner
image is GitHub's to change under us.

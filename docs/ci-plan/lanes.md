# CI speed-up lanes, scope and open defaults

Mirror of the Linear project **SEARCHHH CI speed-up** (six issues, all linked to `SAV-9`).
Kept in-repo because Linear can be unavailable to a reviewer and because `AGENTS.md` requires
the plan of record to travel with the code. **Only Lane A is executed in this run**; B–F are
tickets, not work in progress (`AGENTS.md` work-in-progress limit: one root-cause ticket).

| Lane | Scope | Done when | Blockers | State here |
|---|---|---|---|---|
| **A** | De-duplicate triggers, move concurrency to the caller, split the device lane, drop the `formatting` lane, gate releases on tag/dispatch, `contents: read`, SDK diagnostic | One push → one verification run and one emulator session | none | **In Progress — executed in this run** |
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

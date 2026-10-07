# Capability check — CI speed-up run (Lane A)

Date: 2026-10-07 (UTC). Repository: `regularshowrigby8-create/SEARCHHH`.
Session branch: `arena/132b2d0f-searchhh`.

Every line below was probed in this session; nothing here is inferred from a previous run.

| # | Capability | Result | How it was probed / what it means |
|---:|---|---|---|
| 1 | Read files in the repo | **BLOCKED → repaired** | The delivered checkout at `b1cfb32` was a shallow, single-branch clone of `main` containing only `README.md`. `AGENTS.md`, `CLAUDE.md`, `docs/QUALITY_RULES.md`, `.github/workflows/*`, `build.gradle.kts`, `tools/quality/*` were all absent. Read-only fetch of the two `arena/*-searchhh` refs proved the tree exists there; the session branch was then fast-forwarded to `37833d3` (no force, no branch switch). Per `AGENTS.md` rule 5 the *original* state is recorded as BLOCKED, not PASS. |
| 2 | Write files in the repo | **YES** | `docs/ci-plan/` created, file written and read back in the working tree. |
| 3 | `git push` | **YES** | First probed by `git push --dry-run origin arena/132b2d0f-searchhh` → `* [new branch]`, exit 0, then executed for real: `arena/132b2d0f-searchhh` created on the remote at `1ae2f445fbcc440670c183174fc970823ef51fe3`. No force push, no other branch written. |
| 4 | Read GitHub Actions run history | **YES (metadata) / BLOCKED (raw logs)** | `gh` 2.23.0 is authenticated as `regularshowrigby8-create`. 378 runs, per-run job timings, step conclusions, artifact names, branches, tags and releases were all read. Downloading **raw job logs** fails with a blob-storage `EOF` (2 attempts, e.g. job `112586879560`); this is the same storage-EOF limitation already recorded in `docs/BASELINE_VERIFICATION.md`. Conclusions below therefore rest on step-level results, not log bodies. |
| 5 | Run Python | **YES** | `python3` 3.11.2. `unittest discover -s tools/quality/tests` = 131 tests, `OK`, exit 0. `interactions.py --check` = 622 candidates / 9 contracts, exit 0. `unfinished.py` = 590 files / 116 findings, exit 1 (**pre-existing** debt, unchanged by this run). |
| 6 | Run Gradle (JDK 17) | **NO → BLOCKED** | No `java`, no `javac`, no `gradle` on `PATH`; `ANDROID_HOME`/`ANDROID_SDK_ROOT` unset; `/opt` has no SDK. `./gradlew --version` fails with `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH` — identical to every Gradle entry in `docs/BASELINE_VERIFICATION.md`. So `./gradlew help`, `./gradlew tasks --all` and the new `searchhhDeviceEvidence` task are **unverified locally**; the workflow change must be proven by the hosted run. |
| 7 | `actionlint` / `yamllint` | **NO** | Neither binary is installed; no shellcheck either. Validation falls back to `python3 -c "yaml.safe_load"` per changed file plus `git diff --check`. Flagged so the reviewer does not assume an actionlint pass. |
| 8 | Create/update Linear issues | **YES** | Connector loaded; team `Savagee` (`SAV`), existing SAV-1…SAV-11 listed to avoid duplicates. Writes used in this run: project `SEARCHHH CI speed-up` (`e45e48a2-7e78-45eb-a162-5b03005c95d8`), issues `SAV-12`…`SAV-17` (Lane A In Progress, B–F Backlog), 6 `related` links to `SAV-9`, 3 `blocks` edges, and findings comments on `SAV-14` and `SAV-17`. |
| 9 | Create/update Notion pages | **YES (read + append)** | Notion connector validated. The existing page `SEARCHHH Execution Plan` (`3f2cc7cc-dade-81d3-935f-c935ebbe47a0`) was found by search, read back, and then **appended** with a new "CI speed-up — Lane A (executed) · 2026-10-07" section in two `append_block_children` calls (24 blocks, block ids returned, existing content untouched — the append is additive at the end of the page). That write is the proof for this row; before it, only the read half was established. |

## Consequences for this run

* Lane A can be implemented and statically validated, but **no Gradle-level proof** (new task graph) is available locally. It is BLOCKED, not passed.
* Facts about *executed* CI behaviour are verifiable only from run metadata; where a fact needed a log body, this file says so.
* No gate was weakened to obtain a green result: `required-quality` is untouched, `unfinished.py` still exits 1 on the pre-existing 116 findings, and no baseline/suppression was added.

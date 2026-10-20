# PROPOSAL (not applied): make manual and scheduled triggers reachable, or stop claiming them

**Status: proposal only — no file outside `docs/ci-plan/proposals/` was changed.**

## What was asked

Report the default branch; say whether `.github/workflows/android-sdk-provisioning-diagnostic.yml` and
`.github/workflows/buid-app-workflow.yaml` exist on it and whether `workflow_dispatch` can actually be
triggered for them; test with the API where possible, otherwise mark BLOCKED.

## Answers

| question | answer | evidence |
|---|---|---|
| default branch | **`main`**, at commit `b1cfb32a48368a8d4daf88a368f93b65a5a0632d` | `GET /repos/regularshowrigby8-create/SEARCHHH` → `default_branch: main`; `GET /git/ref/heads/main` → that SHA |
| what is on it | **exactly one file: `README.md`.** There is no `.github/` directory at all | `GET /git/trees/b1cfb32a…` → 1 entry (`blob README.md`); `GET /contents/.github/workflows?ref=main` → **404** |
| `android-sdk-provisioning-diagnostic.yml` on main | **No** — 404 on `GET /contents/.github/workflows/android-sdk-provisioning-diagnostic.yml?ref=main` | same call |
| `buid-app-workflow.yaml` on main | **No** — 404 likewise | same call |
| can `workflow_dispatch` be triggered for them | **No, for both** — see the rule below. Both files *declare* `workflow_dispatch:`, and both are registered (`state: active`), but the trigger is inert while the file is absent from `main` | rule [1](https://docs.github.com/actions/using-workflows/events-that-trigger-workflows) + the two 404s; corroborated by `GET /actions/runs?event=workflow_dispatch` → `total_count: 0` over the repository's entire history |

### The rule that decides it

GitHub's own reference for `workflow_dispatch`: *"This event will only trigger a workflow run if the
workflow file exists on the default branch… On the GitHub UI, the 'Run workflow' button will be present
if the workflow file exists on the default branch. Once a workflow has run at least once, you can
dispatch it against any branch or tag via the GitHub API or GitHub CLI."*
Source: <https://docs.github.com/actions/using-workflows/events-that-trigger-workflows>

Read the two sentences together, because they are often mis-quoted as contradicting each other: the
*default-branch requirement* is about where the **workflow file** lives, and the *"any branch or tag"*
sentence is only about the **`ref` you pass**. Running once (which these workflows have, 362 push runs)
makes a workflow dispatchable *against other branches* — it does not make a workflow dispatchable whose
file is missing from `main`. Community reports match this precisely: a workflow that runs on push,
appears in the Actions list, shows no "Run workflow" button, and support's answer was
*"The workflow must exist in the default branch of the repo"*
(<https://github.com/orgs/community/discussions/172914>).

Same mechanism, one extra victim: `codeql.yml` declares `schedule: cron '23 3 * * 1'`, and per the docs
*"Scheduled workflows will only run on the default branch"* → that cron has never fired and never will
while `main` has no workflows. Measured: `GET /actions/runs?event=schedule` → `total_count: 0`.

## What is BLOCKED, and why it does not change the verdict

I attempted the live test the task asked for and **could not complete it from this sandbox**:

```
POST /repos/…/actions/workflows/377047595/dispatches  -f ref=arena/132b2d0f-searchhh   → HTTP 403
POST /repos/…/actions/workflows/369617922/dispatches  -f ref=arena/132b2d0f-searchhh   → HTTP 403
POST /repos/…/actions/workflows/377047595/dispatches  -f ref=main                       → HTTP 403
gh workflow run android-sdk-provisioning-diagnostic.yml --ref arena/132b2d0f-searchhh   → HTTP 403
{"message":"Resource not accessible by integration"}    headers: X-Oauth-Scopes: (empty)
```

That 403 is **my credential**, not the repository: the sandbox token is a GitHub App installation token
with an empty scope set and no `actions:write`, while every read call above succeeded. It is therefore a
permission block on the *demonstration*, and I am not reporting "dispatch is broken" on its authority —
the verdict rests on the file-absence-on-`main` fact plus the documented rule. To see the failure with
your own credentials, any one of these is enough:

```bash
gh workflow run android-sdk-provisioning-diagnostic.yml --ref arena/132b2d0f-searchhh
gh api -X POST repos/regularshowrigby8-create/SEARCHHH/actions/workflows/377047595/dispatches \
  -f ref=arena/132b2d0f-searchhh
```

Expected if the analysis is right: a 404/422 saying the workflow cannot be dispatched because it is not
configured on the default branch — *not* a 204. If instead a 204 comes back and a run appears, my
reading of the rule is wrong and the fix below becomes optional rather than necessary; that result
should be recorded here rather than silently dropped.

## Options, in the order I would recommend them

**A. Put the CI on `main` (root cause; fixes dispatch, schedule, and `merge_group` at once).**
`main` is one commit deep and contains only `README.md`, so every workflow, every policy file and the
whole gate system exists solely on `arena/*` branches. That is also why `android-verification.yml`'s
`merge_group:` trigger has produced **0 runs** in this repository's history (`event=merge_group` → 0)
despite 43 `pull_request` runs: merge queues need branch protection on a real base branch. Concretely:
open a PR from the current head of `arena/3674d801-searchhh` (or this lane) into `main`, merge it, and
`workflow_dispatch`/`schedule`/`merge_group` all become live with no YAML change at all. This is a
release-adjacent action on a branch that has never held code, so it is the owner's call, not mine.

**B. Designate a CI branch as the default** (e.g. make `arena/01a0ea36-searchhh`— or a fresh `ci/main` —
the default branch). Same effect on dispatch/schedule, without merging anything into `main`. Costs:
every default-branch-only behaviour (code scanning alerts, badge resolution, protection rules) follows
the pointer. `git push origin --delete`/rename is not needed; the setting is repository metadata.

**C. Stop claiming triggers that cannot fire (documentation honesty, zero risk).**
Remove `workflow_dispatch:` from the workflows that will stay `push`-only, and remove `codeql.yml`'s
dead `schedule:` block, or annotate them `# inert until CI lands on the default branch`. Cheapest
option, and it does not require deciding A or B. Note this is the *opposite* of the temptation to add a
self-triggering hack: `workflow_dispatch` from another workflow (`gh workflow run` inside a job) would
still hit the same default-branch rule and would also be a way to re-enter the gate from inside itself.

**D. Add a dispatcher stub on `main`.** One file `.github/workflows/dispatch-relay.yml` living on `main`
with `workflow_dispatch`, which then `workflow_call`s or dispatches the real lane. This makes manual
runs possible without moving the whole suite, but it puts a *trigger path* on the branch that the rest of
the policy says is not yet a code branch, and it adds a second place where run parameters are decided.
I would not do this without an explicit decision; it is listed because it is the only option that keeps
`main` as `README.md`-only.

## What I did not do

No workflow file, no trigger, no branch, and no repository setting was changed. `evidence-retrieval.yml`
was found to be **unregistered** (9 workflow files on disk, 8 in `GET /actions/workflows`) because it is
`workflow_dispatch`-only and has never run — exactly the lazy-registration behaviour recorded in
`.github/workflows/android-sdk-provisioning-diagnostic.yml`'s own comment block; that file is left as
the only place explaining why it was originally written push-triggered instead of dispatch-only.

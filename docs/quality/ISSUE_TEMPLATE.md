# Bounded issue record template

Copy to `docs/quality/issues/<stable-id>.md` when an actual finding/root cause is identified. Populate all fields before implementation; use explicit “not applicable + reason” where warranted. This template is not a real issue or a waiver. Add each raw occurrence to `issues/index.json`; a shared root-cause ticket must enumerate all owned occurrences.

## Identity and assignment

- Stable ticket ID / queue phase / root-cause title:
- Status (`queued`, `auditing`, `reproducing`, `implementing`, `verifying`, `closed`, `blocked`, `reopened`):
- Owner/role / last updated:
- Prerequisite ticket IDs / dependent tickets:
- Code baseline SHA / tool and version / workflow-run-check IDs:
- Priority / risk rationale / scope decision (if any):

## Exact occurrence inventory

For each: occurrence ID; tool/rule/severity; repository path; symbol/resource key; line hint; normalized diagnostic; report variant/module/path/hash; primary and secondary locations. Include duplicate-report aliases rather than silently discarding them. Record raw counts before and expected after. Complete reports, not top-N summaries, establish absence.

## Pre-edit audit and reproduction

- Reachable caller/control/route and data flow:
- Existing implementation/dependencies/tests:
- Root-cause explanation (not just copied rule text):
- Reproduction commands, exits and evidence:
- Intended change and acceptance criteria:
- Allowed files and explicit non-goals:
- Compatibility: API levels, persistence/identity, threading/cancellation, errors/resource ownership:
- Security/signing/WebView implications:
- Before visual/performance evidence or explicit blocker:

## Minimal implementation plan

1. Regression or diagnostic reproduction to establish current defect.
2. Small production change preserving unrelated behavior.
3. Failure/boundary/lifecycle tests; affected cross-module/Android tests.
4. Per-rule comparison, full required checks, evidence review.

Record the independent expected result/oracle. For behavior changes, do not derive the expected answer by calling the changed implementation. Do not silently replace a formerly meaningful assertion with a weaker one. Explain deliberate expected-behavior changes, such as correcting previously ignored mirrored images.

## Executed verification table

| Command / run / check | Source SHA | Exit / test counts | Passed, failed or blocked | Evidence path + hash / limitation |
| --- | --- | --- | --- | --- |

Use actual rows, not “should pass.” Record failed attempts and red-before-fix evidence separately. Never sum overlapping JVM/device-lane totals. Missing/zero/stale/skipped test results do not establish success. Report paths alone do not prove execution.

## Diagnostic reconciliation

- Owned occurrences resolved (IDs and reason):
- New findings introduced and resolved (or ticket remains open):
- Inherited failures remaining (IDs/owning tickets):
- Report totals before/after, including severity changes:
- Duplicate/relocated findings and identity mapping:
- Exceptions: exact fingerprint, reason, owner, expiry/review/removal condition and executed regression (only if legitimately required):

## UI/runtime evidence

- Control tag/label/action → observed state/navigation/durable result:
- Real caller exercised vs mocked boundary:
- Screenshot/reference/current paths/hashes and provenance:
- Actual runtime/backend/device evidence, not catalogue count:
- Accessibility/performance/device-format coverage and limitations:

## Closure or blocker

- Final tested **code SHA**, docs-only follow-up SHA if different:
- Evidence review: regression passes, owned finding absent, no introduced debt, older relevant regressions retained:
- Global gates still failing (bounded closure is not project PASS):
- Blocker: exact file/symbol/failed command/missing access or user decision; next executable action:
- Next ticket (only after verified closure) / release status:

Update index, queue, sequential remediation log and implementation report before handoff. A blocker is not a completed issue. No release is authorized by closing this record alone.

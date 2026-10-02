# FACTORY-003 — automatic app issue work and security policy contracts

Status: VERIFYING. User-directed factory expansion; production source is not
silently auto-modified. Owner: current agent, maintainer review for security and
release decisions.

## Purpose

The factory must perform repeatable work around an app issue, not only list a
scanner result. This ticket adds a product-owned `app-security-policy` engine
and `issue_workflow.py`. The engine produces bounded file/rule/line findings for
HTTPS, cleartext, WebView mixed content/debugging/file access, TLS cancellation,
exported surfaces and possible hard-coded credentials. The workflow selects a
bounded category of relevant tools, runs them, writes a plan and an issue packet,
and only reports `VERIFIED_PENDING_REVIEW` after a requested scoped run returns
zero. It never auto-fixes source, hides failures, closes semantic ownership or
approves release.

## Current execution

- Registry: **65** concrete engines.
- Local policy scan: **FAIL with 3 findings**: two global cleartext permissions
  and one hard-coded credential candidate. TLS delegation, mixed-content setting,
  debug restriction and file-URL policy are recognized correctly.
- Local checker suite: **106 PASS**.
- Existing S00 production change remains separate: HTTP is now opt-in and mixed
  content is denied, but broad manifest/network cleartext compatibility requires
  review rather than an automatic rewrite.

## Test run result — 2026-09-30

The first bounded issue run used the new workflow. `app-security-policy` returned
three findings; six additional security tools were BLOCKED locally because their
ignored environments/binaries are unavailable after workspace restoration. The
workflow correctly stayed `VERIFYING` and nonzero. Safe compact evidence is in
[security-run-current.json](../../factory/security-run-current.json); it contains
no credential values.

The policy now reduces the result to two broad cleartext permissions. The
previous TTS candidate is classified by a narrow, documented public-protocol
contract for the Edge TTS request format; its value is never emitted. The earlier
S00 code fix handles top-level HTTP approval and WebView mixed content. The broad
cleartext settings cannot be removed blindly without breaking the explicit HTTP
compatibility path; this remains a separate compatibility decision.

## Next verification

Provision the six blocked tools and rerun this same issue workflow in hosted CI.
Then handle one finding at a time: reproduce, assign ownership, make a minimal fix
or documented exception, and rerun the relevant category. The factory should be
extended with a precise contract whenever a failure is not reproducible or its
scope is too broad. Device/WebView behavior and release gates remain required.

## Slow-work supervisor test

`tools/factory/auto_issue.py` now owns selected-environment provisioning,
bounded retry and status recording. A test run for `s00-current-security` did
this automatically:

- Attempt 1: `1 FAIL / 6 BLOCKED`.
- Provisioned Python successfully in 7.74 seconds.
- Binary provisioning failed quickly and was preserved in its installer log.
- Attempt 2: `2 FAIL / 3 BLOCKED / 2 PASS`.
- Final state: `BLOCKED_OR_FINDINGS`, never falsely green.

The supervisor stopped after its two-attempt bound. It reduced manual setup and
retry work, but did not pretend that the remaining cleartext finding or blocked
security scanners were resolved.

## Per-run remediation plans

The supervisor now writes `remediation-plan-attempt-N.json` when an app policy
report is present. It assigns every finding to a bounded owner/action and adds
blocked tool prerequisites. It never generates an automatic source edit for
security findings; `auto_fix_count` remains zero. This is deliberate: setup and
triage are automatable, while changing cleartext compatibility or rotating a
credential without ownership is unsafe.

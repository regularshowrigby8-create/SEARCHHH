# Mandatory Searchhh contribution rules

Read `docs/QUALITY_RULES.md` before editing. This applies to every agent and contributor working in this repository. More specific instructions may add requirements, not waive these gates. Preserve upstream license notices and existing architecture.

1. Audit the relevant production code, routes, state flow, tests, dependencies and CI before implementing. Save/update the project audit; execute and record baseline commands first.
2. Implement behavior, not catalogues, placeholder handlers or claims. A listed codebase/model/source is not an integrated runtime. Never represent a reserved test tag as an implemented screen.
3. Map changed controls in the interaction inventory and contracts. Use stable semantic IDs. Test an action followed by observable state/navigation/data changes. Existence-only assertions do not establish functionality.
4. Keep failures visible. No blanket baselines, disabled tests, ignored CI failures, swallowed exceptions, removal of unfinished markers without resolving the underlying issue, or suppression to make checks green. Legitimate exceptions need exact fingerprints, reasons, owner, expiry and regression tests.
5. Run `python3 -m unittest discover -s tools/quality/tests -v`, `python3 tools/quality/interactions.py --check`, and `./gradlew --continue searchhhVerification`. The last command requires JDK 17, Android SDK and an emulator. Missing environment is BLOCKED, not PASS.
6. Treat invalid TLS as a cancellation. Do not add unsafe native bridges, blanket cleartext exceptions, embedded secrets, or a Chromium source build. Preserve URL/result identities and cancellation/persistence behavior.
7. UI claims require before/after evidence, source/route, behavior test and executed result. State unimplemented screenshot/performance/tooling requirements explicitly; never manufacture screenshots or metrics.
8. Finish with `docs/QUALITY_IMPLEMENTATION_REPORT.md` and the required status sections in `docs/QUALITY_RULES.md`. Never declare PASS while a required gate fails or is unexecuted.
9. Include `Searchhh-Quality-Policy: accepted-v1` as a commit trailer; check the PR acknowledgment after reading the policy. Do not check it on behalf of an unrelated contributor.

## Next-agent assignment and sequential execution

**Factory expansion:** [FACTORY-002](docs/quality/issues/FACTORY-002.md) has
verified infrastructure:64 wired engines,56 executed, change routing, review
packets and a [complete repository/ownership map](docs/factory/FLEET.md).
Its70–80% routine-effort acceptance remains OPEN / NOT MEASURED. Use the factory
during S00 security ownership review to collect real paired effort observations;
do not invent savings or start unrelated production repairs. Read
[the results and gaps](docs/factory/EXPANSION_RESULTS.md).

**Factory checkpoint:** [FACTORY-001](docs/quality/issues/FACTORY-001.md) is
closed for its bounded infrastructure scope. Read the
[executed evidence](docs/factory/VERIFICATION.md) and use scoped factory jobs.
Fifty engines executed; failing checks remain unresolved. Resume S00 ownership
and security review below, not S01 or release. Listings and doctor availability
never count as execution or quality approval.

Before choosing work, read `docs/quality/NEXT_AGENT_WORK_ORDER.md`, `docs/quality/REMEDIATION_BACKLOG.json`, and the latest `docs/QUALITY_IMPLEMENTATION_REPORT.md`. The work order covers complete finding inventory, rule-specific repair, functional construction and final release; use `docs/quality/ISSUE_TEMPLATE.md` for bounded tickets.

- Work-in-progress limit: **one root-cause implementation ticket**. Audit/reproduce/fix/execute verification/close before starting another; record exact blockers rather than claiming completion.
- First assignment: **S00, reconcile semantic issue ownership and cross-report duplicates** using `docs/quality/recovered-6abf29b/README.md` and its verified raw occurrence export. Full report recovery is closed; the old 28 omitted lint groups are now recovered. Raw records are not stable root-cause tickets. Review newly exposed security findings before confirming the queued S01 mirrored-background correction.
- Preserve prior border/SAF/EXIF regressions, strict thresholds, stable app identity and production signing. No release or debug substitute between repairs.
- Update ticket/index, queue, sequential log and implementation report with code SHA, run/check IDs, failures and the exact next action. Never infer a passing gate from counts, absent annotations or old evidence.
- This handoff uses branch `arena/01a0ea36-searchhh`; push only that branch without force or switching branches. Observe all existing policy acknowledgment and review requirements.

Repository policy/CI cannot force readers or forks to agree. Required review/check enforcement needs GitHub administrator protection; do not claim it is active without evidence.

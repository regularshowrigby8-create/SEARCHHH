# S00-ownership — reconcile raw occurrences into semantic tickets

- Status: queued (not implemented). Owner: next agent. Phase S00 remains open.
- Prerequisite completed: S00-transport, recovery run 36635551405, original source 6abf29b. Raw export verified by strict parser, per-report ktlint footer counts, independent Detekt SARIF count and repeat-export byte equality.
- Inputs: `../recovered-6abf29b/{README.md,manifest.json,occurrences.json}`; `../REMEDIATION_BACKLOG.json`; `../ISSUE_TEMPLATE.md`.
- Coverage: 458 app lint errors/14 hints across all 34 rules, repeated release variant and library overlap; Detekt 2788; formatting 13875; unfinished 178; 617 interaction candidates/9 contracts; 42 scenarios, 38 declared blockers.
- Do not reopen report-download troubleshooting or call raw occurrence addresses stable ticket IDs. Do not count 17,890 raw records as unique bugs or 617 candidates as dead buttons.

## Exact next procedure

1. Validate dataset/exporter hashes in manifest and inspect source/report provenance. All source/runtime files remain at verified baseline; new Python tool files have their own local tests/scan evidence.
2. Audit actual source symbols/resource keys for each occurrence family. Create stable semantic IDs from rule + repository path + enclosing symbol/resource key + normalized diagnostic/context. Detect collisions; line hints and raw report ordinals must not become stable identity. Preserve source locations and all secondary links.
3. Alias repeated debug/release/dependency observations of the same cause without deleting raw occurrences. Do not conflate independent same-message findings at different symbols or distinct tools' requirements. Record uncertainty explicitly until source review resolves it.
4. Assign every occurrence to a queued, owner-designated bounded ticket (or a parent with concrete children required before implementation); map each rule to S02–S09 and construction-dependent issues to S10–S14. Avoid creating 17,890 mostly blank Markdown files. The index may hold compact occurrence/alias records; concrete implementation tickets use ISSUE_TEMPLATE.md. No bulk phase counts as one root-cause implementation ticket.
5. Review `InsecureBaseConfiguration`, backup transfer rules, JavaScript/bridge origins and `LocalBackend` context ownership first. Distinguish actual security/lifecycle bugs from legitimate browser/application-context use. Document any justified safety-priority reordering ahead of S01 before implementation; do not blindly disable required browsing features or add broad suppressions.
6. Map all 42 scenarios and dynamic/static control dispositions to real source and pending test/implementation tickets. Contract presence is not execution proof. Preserve 38 existing blocker declarations until their acceptance criteria actually pass.
7. Reconcile every raw report total to its assigned/aliased occurrences. Audit any parser/compiler coverage limitations in manifest rather than treating absent matching messages as zero warnings. Keep independent library release lint explicitly unexecuted where not part of these original lanes.
8. S00 closes only after every occurrence/scenario has an accountable disposition, all duplicates/collisions are reconciled, rule-specific acceptance is defined and source/report totals agree. Full project status remains PARTIAL while required gates fail. Then choose exactly one ready production ticket according to the reviewed priority queue.

No production file changes, tool count inflation, threshold relaxation or release assembly belongs to this ticket.

# S00-transport — recover complete existing static reports

- Phase: S00. Status: CLOSED (executed recovery run36635551405; see final section). Owner: Arena agent.
- Baseline: 030984f; reports to recover: 6abf29b / delivery run 36628444279.
- Defect: storage download fails (EOF/URLError), local Java missing; bounded annotations omit 28 app rule groups. Current index cannot be exhaustive.
- Allowed scope: new report transport/recovery workflow/tests and S00 documentation. No app, dependency, quality threshold or release workflow changes.
- Plan: read-only original-artifact download on hosted runner → hash/provenance envelope → bounded gzip/base64 pages → validated decode. Include full static machine reports and quality logs, plus exact-source interaction snapshots. Reject incomplete/mixed/corrupt/oversized payloads and unsafe paths. Separate report-source and recovery-source SHAs.
- Acceptance: negative and round-trip tests pass; actual hosted artifact recovery succeeds; decoded report hashes/counts can be reconciled with original source metadata. Original red workflows remain red. S00 itself remains open until occurrence ownership/reconciliation is complete.
- Known baseline results: 44 checker tests PASS, index consistency PASS, unfinished 178 FAIL, local master blocked by missing Java. Original reports: host 360/release 336 passing tests but overall quality FAIL; not release evidence.
- Next: implement and execute recovery; retain both failure and success evidence. Do not start S01.

## First executed recovery — 95745b7 / run 36634426459

Hosted original-artifact downloads succeed. Four pack jobs succeed, formatting pack fails; overall recovery FAIL. Actual retrieved annotation messages are truncated at exactly 4096 characters, so the decoder rejects every incomplete bundle (no false PASS). Fix transport to 3000-character data pages, at most eight notices per step, at most 96 pages across twelve steps. Preserve full-payload checksums/counts and size caps; add a >50-page regression against the observed 4096-character boundary. Accept real ktlint plain-text reports as well as XML, without accepting logs alone as a report. Add explicit failure annotations so a failed pack exposes its reason. Re-execution required; issue still open.

## Second executed recovery — 1a3d8e3 / run 36634666703

Host, release and source payloads decode completely with valid hashes: all 34 app lint groups are now available (458 errors + 14 hints), libraries 53/52. Detekt is correctly rejected: its check retains only 50 total annotations while payload needs 73 pages. Split that bundle across two deterministic jobs with at most 40 data pages per job; retain identical whole-payload provenance/checksum. Formatting reports still fail the explicit required-report check; add bounded available-file diagnostics to identify the real artifact layout instead of treating absent XML as an empty clean report. S00/transport remain open.

## Third recovery — 8282ce3 / run 36634913699

Host/release/source validate again. Both Detekt shards reconstruct all eight selected files (XML, SARIF, full log and exact-source snapshots), with 73 data pages and valid complete-payload/file hashes. Formatting's available-file manifest proves the original artifact contains **no ktlint machine report**, only the full `quality-formatting.log`, CI summary and configuration-cache HTML. This is not an empty clean formatting report. Support this observed console-only format explicitly: require a full hashed log with actual file/line/column diagnostics AND failed ktlint task markers, and label `console-only-no-machine-report`. Missing/plain noise logs still fail. Machine-readable ktlint rule IDs may remain unavailable; preserve original diagnostic messages and this limitation during S00 reconciliation.

## Fourth recovery — b7df7db / run 36635060669

Formatting remains correctly rejected. Inspection of original check **109611299928** reveals the exact cause: plugin `io.gitlab.arturbosch.detekt:1.23.8` could not resolve from configured repositories, so Gradle failed during configuration at `build.gradle.kts:3` before ktlint ran. The missing report is **not** evidence that ordinary ktlint reporting is console-only. The prior console-only fallback is not sufficient for this failed run and does not accept it. Use the existing standalone formatting artifact **android-formatting-36628443911** (371552 bytes) from **identical source 6abf29b**, instead of inventing findings from a pre-execution failure. All other original artifacts remain pinned to 36628444279; each run/source is validated independently. Preserve this diagnosed unsuccessful attempt.

## Fifth recovery — cdde8e7 / run 36635365329

The executed standalone formatting artifact is present and passes required-report validation, but its complete payload exceeds the prior 80-page transport capacity; pack fails before publishing any page. Increase bounded total capacity to 200 pages across at most five jobs, still **40 data pages/job, eight/step, 3000 characters/page**. This is evidence transport capacity, not a quality threshold or permission to omit findings. Keep the raw 32 MiB cap and full digest/page-count checks. Emit actual required size on overflow. Formatting uses five deterministic shards; Detekt remains two. Re-execute before closure.

## CLOSED — executed source 0778e68 / run 36635551405

All ten recovery jobs succeed. Local decoder validates all five groups: host 9 files, release 7, source 6, Detekt 8, formatting 17. Formatting occupies 100 data pages over five bounded shards (two empty final shards safely emit no data), Detekt 73 pages over two shards; no missing/truncated pages are accepted. Whole-bundle/file hashes and pinned original run/source identities validate. 59 checker tests pass locally and in recovery jobs. Previous failed attempts are retained above. This closes **transport only**, not original quality failures or S00's semantic occurrence/ticket reconciliation. Next: strict normalized occurrence export with report totals and actual missing-owner status.

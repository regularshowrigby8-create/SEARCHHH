# FACTORY-002: less routine work, not a larger catalogue

User-authorized infrastructure expansion; WIP1. S00 is paused, not closed.

## Target and measurement

Target a 70–80% reduction in **active manual check-selection, invocation,
report-reading and repeat-verification time**, not 70–80% of all engineering. No
measured percentage exists yet. Counted tools, elapsed CI minutes, passing
checks and grouped diagnostics are not human-time savings. Use at least ten
paired, comparable work items, record actual before/after active minutes and
retain all required checks. Report failures and blocked work separately.

## Implement now

1. Add six complementary engines: Playwright, axe-core, OpenAPI Spec Validator,
   pipdeptree, Taplo and npm's audit engine. Pin new packages and retain
   integrity locks. No app/runtime dependencies. A browser prerequisite may be
   blocked; installation or listing alone will not count as execution.
2. Route changed files to scoped tool selections, conservatively falling back to
   full diagnostic selection for unknown paths. Never skip canonical native,
   backend, device or signing gates. Record reasons and unsupported
   prerequisites.
3. Parse supported machine-readable reports into location/rule packets without
   snippets or secret values; preserve aliases and opaque failures. Do not
   declare unsupported output empty/clean or call grouping semantic
   deduplication.
4. Generate a searchable local HTML factory map with every tool/repository,
   responsibilities, historical versus current evidence, queued additions and
   human-owned gaps. Verify filtering/reset behavior rather than screenshots
   only.
5. Expand automatic hosted checks to app/backend/tooling changes. Keep
   diagnostics red on findings; do not enable unattended source fixes, PR merges
   or releases.
6. Record actual outcomes, limitations and a reproducible manual-effort ledger.

## Deliberately not claimed implemented

CodeQL dataflow, Schemathesis service fuzzing, Maestro device journeys,
Roborazzi native screenshots, Macrobenchmark/Perfetto performance, ZAP service
DAST, k6 load tests and Renovate PR automation need separate fixture,
environment, scope or authorization work. List upstream repositories and owners;
do not count them among implemented engines or promise automatic bug repair.

## Baseline

At6f94604:88 checker tests pass; interaction index617/9 current but incomplete;
unfinished553 files/178 findings/0 checker errors FAIL. Local master cannot
start without Java/JAVA_HOME. Existing factory evidence at25f9401 executes50
of58: 20PASS/9REPORTED/21FAIL. This is historical evidence, not a current clean
build.

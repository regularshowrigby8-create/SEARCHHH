# Perplexity response sections 17–20

The referenced Perplexity page was read through the available response sections,
including the linked sections 17, 18, 19 and 20. The actionable guidance was:

- Keep the existing factory architecture; do not create a Kotlin-only factory.
- Use Gradle-aware KtLint and Detekt handlers for Android repositories.
- Prefer `./gradlew ktlintCheck` and `./gradlew detekt` over arbitrary binaries.
- Emit Checkstyle, SARIF, HTML and Markdown reports where the configured plugin supports them.
- Treat KtLint formatting as a separately approved, reviewable mutation.
- Keep Detekt report-only by default; manually review semantic findings.
- Detect languages from changed files and project markers, selecting multiple profiles.
- Normalize findings into a common schema and preserve raw evidence.
- Distinguish `AVAILABLE`, `BLOCKED`, `EXECUTED_PASS`, `EXECUTED_FINDINGS`,
  `EXECUTED_FAIL`, `SKIPPED`, `RECHECK_REQUIRED` and `VERIFIED`.
- Use bounded repair packets: at most 10 files, 300 changed lines, 25 findings,
  2 repair attempts and 6 tools.
- Serialize source mutations and use a mutation lock.
- Prioritize runtime, security, WebView, API, unfinished-code and device blockers
  ahead of style and modernization debt.
- Do not publish an APK unless source, static, Android, device, signing and
  release-smoke gates all pass.
- Treat a missing artifact or unavailable tool as unavailable evidence, never as PASS.

Implemented in this follow-up:

- `tools/factory/run_status.py` adds the fail-closed execution vocabulary and
  release decision helper.
- `tools/quality/tests/test_run_status.py` covers skipped, blocked, partial and
  successful gate states.
- Existing language routing and Gradle report normalization remain the canonical
  implementation; no parallel Kotlin-only factory was added.

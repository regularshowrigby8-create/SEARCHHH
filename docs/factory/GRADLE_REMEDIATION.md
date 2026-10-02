# Gradle quality remediation queue

`tools/factory/gradle_quality_queue.py` converts actual KtLint Checkstyle XML
and Detekt XML into bounded remediation packets. It is report-only:

- It never changes source.
- It never creates a Detekt baseline.
- It never suppresses findings.
- It never treats missing reports as a pass.
- It marks release approval false.
- KtLint packets are formatting-priority and still require compile/tests.
- Detekt packets require agent review because long methods, exception handling,
  complexity and architecture findings are not safe blind fixes.

Example hosted usage after the real Gradle tasks run:

```sh
python3 tools/factory/gradle_quality_queue.py \
  --ktlint app/build/reports/ktlint/checkstyle-main.xml \
  --detekt app/build/reports/detekt/detekt.xml \
  --output build/reports/factory/gradle-remediation-queue.json
```

The queue is intentionally separate from unverified external source adapters.
External tools can be trialled only when their command, version, license,
output and result are recorded. A tool that cannot produce verifiable evidence
is `BLOCKED`, not a passing replacement for KtLint, Detekt or Android Lint.

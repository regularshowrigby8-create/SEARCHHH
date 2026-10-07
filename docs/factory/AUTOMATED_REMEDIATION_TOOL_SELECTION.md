# Automated remediation tool selection

Research date: 2026-09-30. These tools are selected by failure type. A tool is
not considered integrated merely because it appears in this document; it must
have a pinned version, handler, report, bounded mutation policy and recheck.

## Selected tools

| Failure | Tool | Mode | Automatic action | Gate |
|---|---|---|---|---|
| Kotlin formatting | Gradle KtLint | hosted Gradle task | approved formatting only | KtLint + compile + tests |
| Kotlin complexity and code smells | Detekt 1.23.8 | hosted Gradle task | report-only by default | Detekt + targeted tests |
| API/resource/WebView lint | Android Lint | hosted Gradle task | only explicit safe suggestions | Lint + API/device tests |
| Kotlin/Gradle semantic migration | OpenRewrite | dry-run first | explicit recipe only | diff review + compile/tests |
| dependency drift | Renovate or Dependabot | pull-request bot | dependency PR only | full quality gate |
| Compose/UI visual regression | Paparazzi or Roborazzi | JVM test | record/verify snapshots | screenshot tests |
| device screenshot evidence | Dropshots | emulator/device | no source mutation | device smoke |
| security rule remediation | Semgrep autofix | SARIF/rule fix only | approved rule fixes only | security recheck |
| SARIF reachability | CodeQL/Semgrep/Trivy reports | report-only | classify reachable/unreachable | human/security review |

Detekt officially supports Gradle tasks and Checkstyle, HTML, Markdown and SARIF
reports. It also supports explicit auto-correction, but this repository keeps
semantic Detekt correction disabled by default:

- https://detekt.dev/docs/gettingstarted/gradle/
- https://github.com/detekt/detekt

Android Lint supports structural correctness, security, performance, usability,
accessibility and internationalization checks, plus SARIF and safe suggestion
modes. Its suggestions must still be reviewed:

- https://developer.android.com/studio/write/lint
- https://googlesamples.github.io/android-custom-lint-rules/user-guide.html

OpenRewrite provides semantic Kotlin/Java/Gradle transformations. Its
`rewriteDryRun` mode produces a patch without editing source, so it is suitable
for packet generation before an explicit mutation:

- https://openrewrite.github.io/rewrite-gradle-plugin/
- https://docs.openrewrite.org/recipes/kotlin/format/autoformat

Renovate and Dependabot are dependency-update PR generators, not automatic
approval systems. Every update must pass the repository gates:

- https://docs.renovatebot.com/
- https://docs.github.com/en/code-security/dependabot

Paparazzi, Roborazzi and Dropshots provide complementary visual evidence. JVM
screenshots are useful for fast feedback, while device screenshots remain needed
for system UI, WebView and emulator behavior:

- https://developer.android.com/agents/skills/testing/testing-setup/skill
- https://github.com/sergio-sastre/Android-screenshot-testing-playground

Semgrep provides rule-based fixes only where a rule explicitly supplies a safe
fix. Security fixes remain reportable and reviewable, never blanket mutations:

- https://semgrep.dev/products/product-updates/accelerate-remediation-with-semgrep-autofix/

## Packet policy

Automation may mutate only when all conditions hold:

1. The tool and recipe are pinned.
2. The worktree/base commit matches the packet.
3. The mutation lock is free.
4. The packet is at most 10 files, 300 changed lines and 25 findings.
5. The rule is on the approved-safe list.
6. The complete diff is uploaded.
7. Compile/tests and the originating gate rerun.
8. Security, WebView, API, auth, manifest, migration and release changes have
   explicit human approval.

No tool can automatically decide product behavior, classify an imported asset
as safe, or prove a device failure is fixed. Those remain explicit review and
evidence steps.

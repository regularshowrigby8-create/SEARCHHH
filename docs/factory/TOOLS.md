# Incorporated factory tools

Historical58-engine command reference from FACTORY-001. The complete current
64-engine map, code repositories, responsibilities and human-owned gaps are in
[FLEET.md](FLEET.md); tools.json remains the executable source of truth.

These 58 distinct upstream engines have concrete runner commands and
provisioning. Support packages, wrappers, subcommands and test cases are not
additional tools. A declared integration is not a claim that its latest result
passes. Consult the execution records for installed, executed, reported, failed
and blocked states.

## 1. Git (`git`)

Reject whitespace errors in the current patch

[Upstream](https://git-scm.com/docs/git-diff)

- Provisioning: system; existing environment; doctor required.
- Profiles: `core`, `fast`.

```json
["git", "diff", "--check"]
```

## 2. GitHub CLI (`gh`)

Collect actual same-branch CI state without publishing

[Upstream](https://cli.github.com/manual/gh_run_list)

- Provisioning: system; existing environment; doctor required.
- Profiles: `evidence`.

```json
[
  "gh",
  "run",
  "list",
  "--branch",
  "arena/01a0ea36-searchhh",
  "--limit",
  "10",
  "--json",
  "databaseId,headSha,status,conclusion,name"
]
```

## 3. ripgrep (`ripgrep`)

Locate WebView security-sensitive call sites

[Upstream](https://github.com/BurntSushi/ripgrep)

- Provisioning: system; existing environment; doctor required.
- Profiles: `triage`.

```json
[
  "rg",
  "--line-number",
  "--glob",
  "*.kt",
  "onReceivedSslError|addJavascriptInterface|javaScriptEnabled",
  "app/src/main"
]
```

## 4. Gradle (`gradle`)

Inspect actual resolved app runtime dependencies

[Upstream](https://docs.gradle.org/current/userguide/viewing_debugging_dependencies.html)

- Provisioning: project; gradle/libs.versions.toml and
  gradle/wrapper/gradle-wrapper.properties.
- Profiles: `android`.

```json
[
  "./gradlew",
  "--continue",
  ":app:dependencies",
  "--configuration",
  "releaseRuntimeClasspath",
  "-PuniversalApk",
  "--max-workers=2"
]
```

## 5. Android Lint (`android-lint`)

Run app/library debug and app release static gates

[Upstream](https://developer.android.com/studio/write/lint)

- Provisioning: project; gradle/libs.versions.toml and
  gradle/wrapper/gradle-wrapper.properties.
- Profiles: `android`.

```json
[
  "./gradlew",
  "--continue",
  ":app:lintDebug",
  ":ad-filter:lintDebug",
  ":adblock-client:lintDebug",
  ":app:lintRelease",
  "-PuniversalApk",
  "--max-workers=2"
]
```

## 6. Detekt (`detekt`)

Preserve strict Kotlin complexity/error-handling gate

[Upstream](https://detekt.dev/docs/gettingstarted/gradle/)

- Provisioning: project; gradle/libs.versions.toml and
  gradle/wrapper/gradle-wrapper.properties.
- Profiles: `android`.

```json
["./gradlew", "--continue", "detekt", "-PuniversalApk", "--max-workers=2"]
```

## 7. ktlint (`ktlint`)

Check Kotlin formatting without rewriting source

[Upstream](https://pinterest.github.io/ktlint/)

- Provisioning: project; gradle/libs.versions.toml and
  gradle/wrapper/gradle-wrapper.properties.
- Profiles: `android`.

```json
["./gradlew", "--continue", "ktlintCheck", "-PuniversalApk", "--max-workers=2"]
```

## 8. R8 (`r8`)

Verify release shrinking and runtime exclusions without APK assembly

[Upstream](https://r8.googlesource.com/r8/)

- Provisioning: project; gradle/libs.versions.toml and
  gradle/wrapper/gradle-wrapper.properties.
- Profiles: `android`.

```json
[
  "./gradlew",
  "--continue",
  ":app:verifyReleaseRuntimeDependencies",
  ":app:minifyReleaseWithR8",
  "-PuniversalApk",
  "--max-workers=2"
]
```

## 9. Android Debug Bridge (`adb`)

Report real attached devices; absence cannot certify device tests

[Upstream](https://developer.android.com/tools/adb)

- Provisioning: sdk; existing environment; doctor required.
- Profiles: `device`.

```json
["{sdk:adb}", "devices", "-l"]
```

## 10. APK Signer (`apksigner`)

Inspect actual APK signature without signing

[Upstream](https://developer.android.com/tools/apksigner)

- Provisioning: sdk; existing environment; doctor required.
- Profiles: `apk-inspection`.

```json
["{sdk:apksigner}", "verify", "--verbose", "--print-certs", "{apk}"]
```

## 11. APK Analyzer (`apkanalyzer`)

Inspect actual decoded manifest

[Upstream](https://developer.android.com/tools/apkanalyzer)

- Provisioning: sdk; existing environment; doctor required.
- Profiles: `apk-inspection`.

```json
["{sdk:apkanalyzer}", "manifest", "print", "{apk}"]
```

## 12. AAPT2 (`aapt2`)

Inspect APK package/resource metadata

[Upstream](https://developer.android.com/tools/aapt2)

- Provisioning: sdk; existing environment; doctor required.
- Profiles: `apk-inspection`.

```json
["{sdk:aapt2}", "dump", "badging", "{apk}"]
```

## 13. ruff (`ruff`)

Fast Python defect/style triage

[Upstream](https://docs.astral.sh/ruff)

- Provisioning: python; 0.16.9.
- Profiles: `python`, `fast`.

```json
[
  "{pybin:ruff}/ruff",
  "check",
  "backend/searchhh",
  "tools/quality",
  "tools/factory",
  "--output-format",
  "json"
]
```

## 14. pylint (`pylint`)

Semantic checks on factory orchestration

[Upstream](https://github.com/pylint-dev/pylint)

- Provisioning: python; 4.1.1.
- Profiles: `python`.

```json
[
  "{pybin:pylint}/pylint",
  "--output-format=json",
  "tools/factory/factory.py",
  "tools/factory/install.py",
  "tools/factory/safety.py",
  "tools/factory/ci_evidence.py"
]
```

## 15. mypy (`mypy`)

Type-check orchestration boundaries

[Upstream](https://www.mypy-lang.org/)

- Provisioning: python; 2.3.1.
- Profiles: `python`.

```json
[
  "{pybin:mypy}/mypy",
  "--explicit-package-bases",
  "--check-untyped-defs",
  "--cache-dir",
  "{out}/mypy-cache",
  "tools/factory/factory.py",
  "tools/factory/install.py",
  "tools/factory/safety.py",
  "tools/factory/ci_evidence.py"
]
```

## 16. pyright (`pyright`)

Independent type/data-flow checks

[Upstream](https://github.com/Microsoft/pyright#readme)

- Provisioning: npm; 1.1.414.
- Profiles: `python`.

```json
[
  "{nodebin}/pyright",
  "--project",
  "tools/factory/config/pyright.json",
  "--outputjson"
]
```

## 17. bandit (`bandit`)

Python security patterns

[Upstream](https://bandit.readthedocs.io/)

- Provisioning: python; 1.9.4.
- Profiles: `security`, `python`.

```json
[
  "{pybin:bandit}/bandit",
  "-r",
  "-f",
  "json",
  "backend/searchhh",
  "tools/factory/factory.py",
  "tools/factory/install.py",
  "tools/factory/safety.py",
  "tools/factory/ci_evidence.py"
]
```

## 18. semgrep (`semgrep`)

Local security rules, no cloud scan

[Upstream](https://semgrep.dev)

- Provisioning: python; 1.178.0.
- Profiles: `security`.

```json
[
  "{pybin:semgrep}/semgrep",
  "scan",
  "--config",
  "tools/factory/config/semgrep.yml",
  "--metrics",
  "off",
  "--disable-version-check",
  "--error",
  "--json",
  "backend/searchhh",
  "tools/factory"
]
```

## 19. pip-audit (`pip-audit`)

Audit declared backend Python dependencies

[Upstream](https://pypi.org/project/pip-audit/)

- Provisioning: python; 2.10.1.
- Profiles: `supply-chain`.

```json
[
  "{pybin:pip-audit}/pip-audit",
  "-r",
  "backend/requirements.txt",
  "-f",
  "json",
  "--progress-spinner",
  "off"
]
```

## 20. deptry (`deptry`)

Find missing/unused backend requirement declarations

[Upstream](https://deptry.com)

- Provisioning: python; 0.25.1.
- Profiles: `python`.

```json
[
  "{pybin:deptry}/deptry",
  "backend",
  "--requirements-files",
  "backend/requirements.txt",
  "--json-output",
  "{out}/deptry.json",
  "--package-module-name-map",
  "PyYAML=yaml"
]
```

## 21. vulture (`vulture`)

Find unreachable Python candidates for review

[Upstream](https://github.com/jendrikseipp/vulture)

- Provisioning: python; 2.16.
- Profiles: `python`.

```json
["{pybin:vulture}/vulture", "backend/searchhh"]
```

## 22. xenon (`xenon`)

Bound backend cyclomatic complexity

[Upstream](https://xenon.readthedocs.org/)

- Provisioning: python; 0.9.3.
- Profiles: `python`.

```json
[
  "{pybin:xenon}/xenon",
  "--max-absolute",
  "B",
  "--max-modules",
  "B",
  "--max-average",
  "A",
  "backend/searchhh"
]
```

## 23. interrogate (`interrogate`)

Check factory public documentation coverage

[Upstream](https://interrogate.readthedocs.io)

- Provisioning: python; 1.7.0.
- Profiles: `docs`.

```json
[
  "{pybin:interrogate}/interrogate",
  "--fail-under",
  "100",
  "-vv",
  "tools/factory"
]
```

## 24. pytest (`pytest`)

Execute real backend tests

[Upstream](https://docs.pytest.org/en/latest/)

- Provisioning: python; 9.1.1.
- Profiles: `tests`.

```json
[
  "{pybin:qa}/pytest",
  "backend/tests",
  "-q",
  "-W",
  "error",
  "--strict-config",
  "--strict-markers"
]
```

## 25. coverage (`coverage`)

Measure existing quality test coverage

[Upstream](https://github.com/coveragepy/coveragepy)

- Provisioning: python; 7.16.2.
- Profiles: `tests`.

```json
[
  "{pybin:qa}/coverage",
  "run",
  "--data-file",
  "{out}/.coverage",
  "--source",
  "tools.quality,tools.factory",
  "-m",
  "unittest",
  "discover",
  "-s",
  "tools/quality/tests",
  "-q"
]
```

## 26. Hypothesis (`hypothesis`)

Generate adversarial factory safety inputs

[Upstream](https://hypothesis.readthedocs.io/)

- Provisioning: python; 6.168.3.
- Profiles: `tests`.

```json
["{pybin:qa}/pytest", "tools/factory/properties", "-q"]
```

## 27. mutmut (`mutmut`)

Mutation-test a scratch copy of actual safety logic

[Upstream](https://github.com/boxed/mutmut)

- Provisioning: python; 3.8.0.
- Profiles: `mutation`.

```json
["{pybin:mutmut}/mutmut", "run", "--max-children", "2"]
```

## 28. codespell (`codespell`)

Find actual English typos without replacing text

[Upstream](https://github.com/codespell-project/codespell)

- Provisioning: python; 2.4.3.
- Profiles: `docs`.

```json
[
  "{pybin:codespell}/codespell",
  "README.md",
  "AGENTS.md",
  "docs/factory",
  "tools/factory",
  "--skip",
  "*.json,*.lock,*/locks/*"
]
```

## 29. yamllint (`yamllint`)

Validate workflow/Compose YAML strictly

[Upstream](https://github.com/adrienverge/yamllint)

- Provisioning: python; 1.38.0.
- Profiles: `ci`, `fast`.

```json
[
  "{pybin:yamllint}/yamllint",
  "--strict",
  ".github/workflows",
  "backend/compose.yml",
  "tools/factory/config/semgrep.yml"
]
```

## 30. check-jsonschema (`check-jsonschema`)

Validate concrete factory registry schema

[Upstream](https://github.com/python-jsonschema/check-jsonschema)

- Provisioning: python; 0.38.2.
- Profiles: `core`, `fast`.

```json
[
  "{pybin:check-jsonschema}/check-jsonschema",
  "--schemafile",
  "tools/factory/tool.schema.json",
  "tools/factory/tools.json"
]
```

## 31. reuse (`reuse`)

Audit source license/SPDX completeness

[Upstream](https://reuse.software/)

- Provisioning: python; 6.2.0.
- Profiles: `supply-chain`.

```json
["{pybin:reuse}/reuse", "lint"]
```

## 32. pip-licenses (`pip-licenses`)

Inventory installed backend QA dependency licenses

[Upstream](https://github.com/raimon49/pip-licenses)

- Provisioning: python; 5.5.5.
- Profiles: `supply-chain`.

```json
[
  "{pybin:pip-licenses}/pip-licenses",
  "--python",
  "{pybin:qa}/python",
  "--format",
  "json"
]
```

## 33. cyclonedx-py (`cyclonedx-py`)

Generate declared-requirement SBOM (not full APK SBOM)

[Upstream](https://github.com/CycloneDX/cyclonedx-python/#readme)

- Provisioning: python; 7.5.0.
- Profiles: `supply-chain`.

```json
[
  "{pybin:cyclonedx-py}/cyclonedx-py",
  "requirements",
  "backend/requirements.txt",
  "--output-format",
  "JSON",
  "--output-file",
  "{out}/backend.cdx.json"
]
```

## 34. detect-secrets (`detect-secrets`)

Hash-only working-tree secret candidates

[Upstream](https://github.com/Yelp/detect-secrets)

- Provisioning: python; 1.5.0.
- Profiles: `security`.

```json
["{pybin:detect-secrets}/detect-secrets", "scan", "--no-verify"]
```

## 35. zizmor (`zizmor`)

Audit workflow permissions/injection offline

[Upstream](https://docs.zizmor.sh)

- Provisioning: python; 1.30.1.
- Profiles: `ci`, `security`.

```json
["{pybin:zizmor}/zizmor", "--offline", "--format", "json", ".github/workflows"]
```

## 36. shellcheck (`shellcheck`)

Check actual CI shell scripts

[Upstream](https://github.com/ryanrhee/shellcheck-py)

- Provisioning: python; 0.11.0.1.
- Profiles: `ci`, `fast`.

```json
[
  "{pybin:shellcheck}/shellcheck",
  "--format=json",
  "tools/quality/device_verification.sh",
  "mainframer.sh"
]
```

## 37. shfmt (`shfmt`)

Show shell formatting diff without writing

[Upstream](https://github.com/MaxWinterstein/shfmt-py)

- Provisioning: python; 4.2.0.
- Profiles: `ci`.

```json
[
  "{pybin:shfmt}/shfmt",
  "-d",
  "tools/quality/device_verification.sh",
  "mainframer.sh"
]
```

## 38. actionlint (`actionlint`)

Check workflow expressions and structure

[Upstream](https://github.com/Mateusz-Grzelinski/actionlint-py)

- Provisioning: github-binary; v1.7.12.
- Profiles: `ci`, `fast`.

```json
["{gobin}/actionlint", "-color"]
```

## 39. hadolint (`hadolint`)

Check backend Dockerfile best practices

[Upstream](https://github.com/justin-yan/pybin)

- Provisioning: python; 2.15.1.
- Profiles: `containers`.

```json
["{pybin:hadolint}/hadolint", "--format", "json", "backend/Dockerfile"]
```

## 40. eslint (`eslint`)

Check browser assets and test JS without fixes

[Upstream](https://eslint.org)

- Provisioning: npm; 10.11.0.
- Profiles: `javascript`.

```json
[
  "{nodebin}/eslint",
  "--config",
  "tools/factory/config/eslint.config.cjs",
  "app/src/main/assets",
  "js-tests",
  "--format",
  "json"
]
```

## 41. prettier (`prettier`)

Check factory docs/config formatting without writes

[Upstream](https://prettier.io)

- Provisioning: npm; 3.9.9.
- Profiles: `docs`, `fast`.

```json
[
  "{nodebin}/prettier",
  "--check",
  "tools/factory/config/*",
  "tools/factory/tools.json",
  "docs/factory/*.md",
  "--prose-wrap",
  "always"
]
```

## 42. jest (`jest`)

Execute existing translation/browser JS tests

[Upstream](https://jestjs.io/)

- Provisioning: npm; 29.7.0.
- Profiles: `tests`.

```json
[
  "{nodebin}/jest",
  "--config",
  "js-tests/package.json",
  "--runInBand",
  "--env",
  "{node}/node_modules/jest-environment-jsdom/build/index.js"
]
```

## 43. jscpd (`jscpd`)

Locate cross-file duplication before extracting helpers

[Upstream](https://jscpd.dev)

- Provisioning: npm; 5.3.3.
- Profiles: `triage`.

```json
[
  "{nodebin}/jscpd",
  "app/src/main/java",
  "backend/searchhh",
  "js-tests",
  "--format",
  "kotlin,python,javascript",
  "--reporters",
  "json",
  "--output",
  "{out}/duplicates",
  "--threshold",
  "0"
]
```

## 44. knip (`knip`)

Audit JS test entrypoints and dependency use

[Upstream](https://knip.dev)

- Provisioning: npm; 6.38.0.
- Profiles: `javascript`.

```json
[
  "{nodebin}/knip",
  "--config",
  "{root}/tools/factory/config/knip.json",
  "--directory",
  "js-tests",
  "--reporter",
  "json"
]
```

## 45. depcruise (`dependency-cruiser`)

Enforce JS test dependency boundaries

[Upstream](https://github.com/sverweij/dependency-cruiser)

- Provisioning: npm; 18.4.0.
- Profiles: `javascript`.

```json
[
  "{nodebin}/depcruise",
  "--config",
  "tools/factory/config/dependency-cruiser.cjs",
  "--output-type",
  "json",
  "js-tests"
]
```

## 46. npm-check-updates (`npm-check-updates`)

Report JS dependency upgrades without applying them

[Upstream](https://github.com/raineorshine/npm-check-updates)

- Provisioning: npm; 23.1.0.
- Profiles: `supply-chain`.

```json
[
  "{nodebin}/npm-check-updates",
  "--packageFile",
  "js-tests/package.json",
  "--jsonUpgraded"
]
```

## 47. markdownlint-cli2 (`markdownlint`)

Check work instructions and factory docs

[Upstream](https://github.com/DavidAnson/markdownlint-cli2)

- Provisioning: npm; 0.23.3.
- Profiles: `docs`.

```json
["{nodebin}/markdownlint-cli2", "README.md", "AGENTS.md", "docs/factory/*.md"]
```

## 48. markdown-link-check (`markdown-link-check`)

Check factory documentation links with bounded timeout

[Upstream](https://github.com/tcort/markdown-link-check#readme)

- Provisioning: npm; 3.15.0.
- Profiles: `docs`.

```json
[
  "{nodebin}/markdown-link-check",
  "--config",
  "tools/factory/config/links.json",
  "docs/factory/README.md"
]
```

## 49. html-validate (`html-validate`)

Validate actual browser HTML templates

[Upstream](https://html-validate.org)

- Provisioning: npm; 11.16.1.
- Profiles: `web-assets`.

```json
[
  "{nodebin}/html-validate",
  "--config",
  "tools/factory/config/htmlvalidate.json",
  "--formatter",
  "json",
  "app/src/main/assets/*.html"
]
```

## 50. stylelint (`stylelint`)

Validate actual reader/browser CSS

[Upstream](https://stylelint.io)

- Provisioning: npm; 17.15.0.
- Profiles: `web-assets`.

```json
[
  "{nodebin}/stylelint",
  "--config",
  "tools/factory/config/stylelint.cjs",
  "--formatter",
  "json",
  "app/src/main/assets/*.css"
]
```

## 51. retire (`retire`)

Audit vendored browser JS for vulnerable libraries

[Upstream](https://github.com/RetireJS/retire.js#readme)

- Provisioning: npm; 5.7.0.
- Profiles: `security`.

```json
[
  "{nodebin}/retire",
  "--path",
  "app/src/main/assets",
  "--outputformat",
  "json",
  "--outputpath",
  "{out}/retire.json"
]
```

## 52. osv-scanner (`osv-scanner`)

Audit dependency metadata against OSV

[Upstream](https://github.com/google/osv-scanner)

- Provisioning: github-binary; v2.6.0.
- Profiles: `supply-chain`.

```json
[
  "{gobin}/osv-scanner",
  "scan",
  "source",
  "--lockfile=backend/requirements.txt",
  "--format=json"
]
```

## 53. syft (`syft`)

Generate source-directory SBOM for dependency review

[Upstream](https://github.com/anchore/syft)

- Provisioning: github-binary; v1.52.0.
- Profiles: `supply-chain`.

```json
[
  "{gobin}/syft",
  "scan",
  "dir:backend",
  "--output",
  "cyclonedx-json={out}/backend-syft.cdx.json"
]
```

## 54. trivy (`trivy`)

Audit container configuration security

[Upstream](https://github.com/aquasecurity/trivy)

- Provisioning: github-binary; v0.74.0.
- Profiles: `containers`.

```json
["{gobin}/trivy", "config", "--format", "json", "--exit-code", "1", "backend"]
```

## 55. gitleaks (`gitleaks`)

Audit committed history with redacted secret output

[Upstream](https://github.com/gitleaks/gitleaks)

- Provisioning: github-binary; v8.30.1.
- Profiles: `security`.

```json
[
  "{gobin}/gitleaks",
  "git",
  "--redact",
  "--no-banner",
  "--report-format",
  "json",
  "--report-path",
  "{out}/gitleaks.json",
  "."
]
```

## 56. checkov (`checkov`)

Audit Dockerfile security independently of style

[Upstream](https://github.com/bridgecrewio/checkov)

- Provisioning: python; 3.3.20.
- Profiles: `containers`.

```json
[
  "{pybin:checkov}/checkov",
  "-f",
  "backend/Dockerfile",
  "--framework",
  "dockerfile",
  "--output",
  "json",
  "--skip-download"
]
```

## 57. Docker Compose (`docker-compose`)

Validate real backend service wiring without starting services

[Upstream](https://docs.docker.com/reference/cli/docker/compose/config/)

- Provisioning: system; existing environment; doctor required.
- Profiles: `containers`.

```json
[
  "docker",
  "compose",
  "--env-file",
  "{out}/compose.env",
  "-f",
  "backend/compose.yml",
  "config",
  "--quiet"
]
```

## 58. ast-grep (`ast-grep`)

Find Kotlin SSL bypass candidates using syntax, not text replacement

[Upstream](https://ast-grep.github.io)

- Provisioning: npm; 0.45.3.
- Profiles: `triage`, `security`.

```json
[
  "{nodebin}/ast-grep",
  "scan",
  "--rule",
  "tools/factory/config/ssl-rule.yml",
  "--json",
  "app/src/main/java"
]
```

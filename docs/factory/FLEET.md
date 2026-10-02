# Every factory tool and repository

64 implemented command integrations, not64 passing checks. Nine later candidates
are listed separately and are not counted.57 GitHub repository URLs were checked
using repository metadata; Android source and GitLab locations are official
source links, not GitHub metadata checks. Wrappers and support packages do not
increase the engine count.

Latest expansion:56 executed at1171a4c,24PASS/9REPORTED/23FAIL. All six
additions executed. See [results and limitations](EXPANSION_RESULTS.md). The
per-entry FACTORY-001 historical labels below are retained for comparison.

## Automatic versus human-owned work

Tools select suggested checks, execute bounded jobs and emit observations. Eight
machine-readable location formats feed review packets; other reports stay
explicitly opaque. The agent/reviewer owns root-cause confirmation, patch
design, security disposition, native/device scenarios, reference approval and
release. The70–80% target concerns active routine-checking/triage effort, not
all engineering. No measured savings are claimed.

## 1. Git (`git`)

[Source repository](https://github.com/git/git)

**Handles:** Reject whitespace errors in the current patch.

**Selection profiles:** core, fast.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
["git", "diff", "--check"]
```

## 2. GitHub CLI (`gh`)

[Source repository](https://github.com/cli/cli)

**Handles:** Collect actual same-branch CI state without publishing.

**Selection profiles:** evidence.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/BurntSushi/ripgrep)

**Handles:** Locate WebView security-sensitive call sites.

**Selection profiles:** triage.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/gradle/gradle)

**Handles:** Inspect actual resolved app runtime dependencies.

**Selection profiles:** android.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

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

[Source repository](https://android.googlesource.com/platform/tools/base/)

**Handles:** Run app/library debug and app release static gates.

**Selection profiles:** android.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

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

[Source repository](https://github.com/detekt/detekt)

**Handles:** Preserve strict Kotlin complexity/error-handling gate.

**Selection profiles:** android.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

```json
["./gradlew", "--continue", "detekt", "-PuniversalApk", "--max-workers=2"]
```

## 7. ktlint (`ktlint`)

[Source repository](https://github.com/ktlint/ktlint)

**Handles:** Check Kotlin formatting without rewriting source.

**Selection profiles:** android.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

```json
["./gradlew", "--continue", "ktlintCheck", "-PuniversalApk", "--max-workers=2"]
```

## 8. R8 (`r8`)

[Source repository](https://r8.googlesource.com/r8/)

**Handles:** Verify release shrinking and runtime exclusions without APK
assembly.

**Selection profiles:** android.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

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

[Source repository](https://android.googlesource.com/platform/packages/modules/adb/)

**Handles:** Report real attached devices; absence cannot certify device tests.

**Selection profiles:** device.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

```json
["{sdk:adb}", "devices", "-l"]
```

## 10. APK Signer (`apksigner`)

[Source repository](https://android.googlesource.com/platform/tools/apksig/)

**Handles:** Inspect actual APK signature without signing.

**Selection profiles:** apk-inspection.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

```json
["{sdk:apksigner}", "verify", "--verbose", "--print-certs", "{apk}"]
```

## 11. APK Analyzer (`apkanalyzer`)

[Source repository](https://android.googlesource.com/platform/tools/base/)

**Handles:** Inspect actual decoded manifest.

**Selection profiles:** apk-inspection.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

```json
["{sdk:apkanalyzer}", "manifest", "print", "{apk}"]
```

## 12. AAPT2 (`aapt2`)

[Source repository](https://android.googlesource.com/platform/frameworks/base/)

**Handles:** Inspect APK package/resource metadata.

**Selection profiles:** apk-inspection.

**Evidence scope:** Prerequisite-dependent; not executed in original factory
batch.

**Agent/reviewer gap:** Real SDK/device/APK prerequisites and canonical
production signer/device gates remain required.

```json
["{sdk:aapt2}", "dump", "badging", "{apk}"]
```

## 13. ruff (`ruff`)

[Source repository](https://github.com/astral-sh/ruff)

**Handles:** Fast Python defect/style triage.

**Selection profiles:** python, fast.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/pylint-dev/pylint)

**Handles:** Semantic checks on factory orchestration.

**Selection profiles:** python.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
[
  "{pybin:pylint}/pylint",
  "--output-format=json",
  "tools/factory/factory.py",
  "tools/factory/install.py",
  "tools/factory/safety.py",
  "tools/factory/ci_evidence.py",
  "tools/factory/automation.py",
  "tools/factory/factory_map.py"
]
```

## 15. mypy (`mypy`)

[Source repository](https://github.com/python/mypy)

**Handles:** Type-check orchestration boundaries.

**Selection profiles:** python.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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
  "tools/factory/ci_evidence.py",
  "tools/factory/automation.py",
  "tools/factory/factory_map.py"
]
```

## 16. pyright (`pyright`)

[Source repository](https://github.com/microsoft/pyright)

**Handles:** Independent type/data-flow checks.

**Selection profiles:** python.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
[
  "{nodebin}/pyright",
  "--project",
  "tools/factory/config/pyright.json",
  "--outputjson"
]
```

## 17. bandit (`bandit`)

[Source repository](https://github.com/PyCQA/bandit)

**Handles:** Python security patterns.

**Selection profiles:** security, python.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Review exploitability, trust boundaries and false
positives; never expose or auto-rotate keys.

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

[Source repository](https://github.com/semgrep/semgrep)

**Handles:** Local security rules, no cloud scan.

**Selection profiles:** security.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review exploitability, trust boundaries and false
positives; never expose or auto-rotate keys.

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

[Source repository](https://github.com/pypa/pip-audit)

**Handles:** Audit declared backend Python dependencies.

**Selection profiles:** supply-chain.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

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

[Source repository](https://github.com/osprey-oss/deptry)

**Handles:** Find missing/unused backend requirement declarations.

**Selection profiles:** python.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/jendrikseipp/vulture)

**Handles:** Find unreachable Python candidates for review.

**Selection profiles:** python.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
["{pybin:vulture}/vulture", "backend/searchhh"]
```

## 22. xenon (`xenon`)

[Source repository](https://github.com/rubik/xenon)

**Handles:** Bound backend cyclomatic complexity.

**Selection profiles:** python.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/econchick/interrogate)

**Handles:** Check factory public documentation coverage.

**Selection profiles:** docs.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review semantic truth, accessibility and evidence
claims; style checks cannot establish correctness.

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

[Source repository](https://github.com/pytest-dev/pytest)

**Handles:** Execute real backend tests.

**Selection profiles:** tests.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Author missing behavioral assertions; passing existing
tests does not prove feature completeness.

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

[Source repository](https://github.com/coveragepy/coveragepy)

**Handles:** Measure existing quality test coverage.

**Selection profiles:** tests.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Author missing behavioral assertions; passing existing
tests does not prove feature completeness.

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

[Source repository](https://github.com/HypothesisWorks/hypothesis)

**Handles:** Generate adversarial factory safety inputs.

**Selection profiles:** tests.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Author missing behavioral assertions; passing existing
tests does not prove feature completeness.

```json
["{pybin:qa}/pytest", "tools/factory/properties", "-q"]
```

## 27. mutmut (`mutmut`)

[Source repository](https://github.com/boxed/mutmut)

**Handles:** Mutation-test a scratch copy of actual safety logic.

**Selection profiles:** mutation.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Author missing behavioral assertions; passing existing
tests does not prove feature completeness.

```json
["{pybin:mutmut}/mutmut", "run", "--max-children", "2"]
```

## 28. codespell (`codespell`)

[Source repository](https://github.com/codespell-project/codespell)

**Handles:** Find actual English typos without replacing text.

**Selection profiles:** docs.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review semantic truth, accessibility and evidence
claims; style checks cannot establish correctness.

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

[Source repository](https://github.com/adrienverge/yamllint)

**Handles:** Validate workflow/Compose YAML strictly.

**Selection profiles:** ci, fast.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/python-jsonschema/check-jsonschema)

**Handles:** Validate concrete factory registry schema.

**Selection profiles:** core, fast.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
[
  "{pybin:check-jsonschema}/check-jsonschema",
  "--schemafile",
  "tools/factory/tool.schema.json",
  "tools/factory/tools.json"
]
```

## 31. reuse (`reuse`)

[Source repository](https://github.com/fsfe/reuse-tool)

**Handles:** Audit source license/SPDX completeness.

**Selection profiles:** supply-chain.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

```json
["{pybin:reuse}/reuse", "lint"]
```

## 32. pip-licenses (`pip-licenses`)

[Source repository](https://github.com/raimon49/pip-licenses)

**Handles:** Inventory installed backend QA dependency licenses.

**Selection profiles:** supply-chain.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

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

[Source repository](https://github.com/CycloneDX/cyclonedx-python)

**Handles:** Generate declared-requirement SBOM (not full APK SBOM).

**Selection profiles:** supply-chain.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

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

[Source repository](https://github.com/Yelp/detect-secrets)

**Handles:** Hash-only working-tree secret candidates.

**Selection profiles:** security.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Review exploitability, trust boundaries and false
positives; never expose or auto-rotate keys.

```json
["{pybin:detect-secrets}/detect-secrets", "scan", "--no-verify"]
```

## 35. zizmor (`zizmor`)

[Source repository](https://github.com/zizmorcore/zizmor)

**Handles:** Audit workflow permissions/injection offline.

**Selection profiles:** ci, security.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Review exploitability, trust boundaries and false
positives; never expose or auto-rotate keys.

```json
["{pybin:zizmor}/zizmor", "--offline", "--format", "json", ".github/workflows"]
```

## 36. shellcheck (`shellcheck`)

[Source repository](https://github.com/koalaman/shellcheck)

**Handles:** Check actual CI shell scripts.

**Selection profiles:** ci, fast.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
[
  "{pybin:shellcheck}/shellcheck",
  "--format=json",
  "tools/quality/device_verification.sh",
  "mainframer.sh"
]
```

## 37. shfmt (`shfmt`)

[Source repository](https://github.com/mvdan/sh)

**Handles:** Show shell formatting diff without writing.

**Selection profiles:** ci.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
[
  "{pybin:shfmt}/shfmt",
  "-d",
  "tools/quality/device_verification.sh",
  "mainframer.sh"
]
```

## 38. actionlint (`actionlint`)

[Source repository](https://github.com/rhysd/actionlint)

**Handles:** Check workflow expressions and structure.

**Selection profiles:** ci, fast.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
["{gobin}/actionlint", "-color"]
```

## 39. hadolint (`hadolint`)

[Source repository](https://github.com/hadolint/hadolint)

**Handles:** Check backend Dockerfile best practices.

**Selection profiles:** containers.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
["{pybin:hadolint}/hadolint", "--format", "json", "backend/Dockerfile"]
```

## 40. eslint (`eslint`)

[Source repository](https://github.com/eslint/eslint)

**Handles:** Check browser assets and test JS without fixes.

**Selection profiles:** javascript.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/prettier/prettier)

**Handles:** Check factory docs/config formatting without writes.

**Selection profiles:** docs, fast.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review semantic truth, accessibility and evidence
claims; style checks cannot establish correctness.

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

[Source repository](https://github.com/jestjs/jest)

**Handles:** Execute existing translation/browser JS tests.

**Selection profiles:** tests.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Author missing behavioral assertions; passing existing
tests does not prove feature completeness.

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

[Source repository](https://github.com/kucherenko/jscpd)

**Handles:** Locate cross-file duplication before extracting helpers.

**Selection profiles:** triage.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/webpro-nl/knip)

**Handles:** Audit JS test entrypoints and dependency use.

**Selection profiles:** javascript.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/sverweij/dependency-cruiser)

**Handles:** Enforce JS test dependency boundaries.

**Selection profiles:** javascript.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/raineorshine/npm-check-updates)

**Handles:** Report JS dependency upgrades without applying them.

**Selection profiles:** supply-chain.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

```json
[
  "{nodebin}/npm-check-updates",
  "--packageFile",
  "js-tests/package.json",
  "--jsonUpgraded"
]
```

## 47. markdownlint-cli2 (`markdownlint`)

[Source repository](https://github.com/DavidAnson/markdownlint-cli2)

**Handles:** Check work instructions and factory docs.

**Selection profiles:** docs.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Review semantic truth, accessibility and evidence
claims; style checks cannot establish correctness.

```json
["{nodebin}/markdownlint-cli2", "README.md", "AGENTS.md", "docs/factory/*.md"]
```

## 48. markdown-link-check (`markdown-link-check`)

[Source repository](https://github.com/tcort/markdown-link-check)

**Handles:** Check factory documentation links with bounded timeout.

**Selection profiles:** docs.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review semantic truth, accessibility and evidence
claims; style checks cannot establish correctness.

```json
[
  "{nodebin}/markdown-link-check",
  "--config",
  "tools/factory/config/links.json",
  "docs/factory/README.md"
]
```

## 49. html-validate (`html-validate`)

[Source repository](https://gitlab.com/html-validate/html-validate)

**Handles:** Validate actual browser HTML templates.

**Selection profiles:** web-assets.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/stylelint/stylelint)

**Handles:** Validate actual reader/browser CSS.

**Selection profiles:** web-assets.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/RetireJS/retire.js)

**Handles:** Audit vendored browser JS for vulnerable libraries.

**Selection profiles:** security.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review exploitability, trust boundaries and false
positives; never expose or auto-rotate keys.

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

[Source repository](https://github.com/google/osv-scanner)

**Handles:** Audit dependency metadata against OSV.

**Selection profiles:** supply-chain.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

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

[Source repository](https://github.com/anchore/syft)

**Handles:** Generate source-directory SBOM for dependency review.

**Selection profiles:** supply-chain.

**Evidence scope:** Historical25f9401: REPORTED.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

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

[Source repository](https://github.com/aquasecurity/trivy)

**Handles:** Audit container configuration security.

**Selection profiles:** containers.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
["{gobin}/trivy", "config", "--format", "json", "--exit-code", "1", "backend"]
```

## 55. gitleaks (`gitleaks`)

[Source repository](https://github.com/gitleaks/gitleaks)

**Handles:** Audit committed history with redacted secret output.

**Selection profiles:** security.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Review exploitability, trust boundaries and false
positives; never expose or auto-rotate keys.

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

[Source repository](https://github.com/bridgecrewio/checkov)

**Handles:** Audit Dockerfile security independently of style.

**Selection profiles:** containers.

**Evidence scope:** Historical25f9401: FAIL.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/docker/compose)

**Handles:** Validate real backend service wiring without starting services.

**Selection profiles:** containers.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

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

[Source repository](https://github.com/ast-grep/ast-grep)

**Handles:** Find Kotlin SSL bypass candidates using syntax, not text
replacement.

**Selection profiles:** triage, security.

**Evidence scope:** Historical25f9401: PASS.

**Agent/reviewer gap:** Review exploitability, trust boundaries and false
positives; never expose or auto-rotate keys.

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

## 59. Playwright (`playwright`)

[Source repository](https://github.com/microsoft/playwright)

**Handles:** Real Chromium control actions on repository HTML and factory
dashboard; not Android WebView certification.

**Selection profiles:** browser, tests.

**Evidence scope:** New integration; see expansion execution evidence.

**Agent/reviewer gap:** Native WebView bridge, device accessibility and full app
flows remain unverified.

```json
["node", "tools/factory/browser_checks.cjs", "smoke", "{out}"]
```

## 60. axe-core (`axe-core`)

[Source repository](https://github.com/dequelabs/axe-core)

**Handles:** Accessibility violations in rendered repository HTML; manual/native
accessibility remains required.

**Selection profiles:** browser, accessibility.

**Evidence scope:** New integration; see expansion execution evidence.

**Agent/reviewer gap:** Native WebView bridge, device accessibility and full app
flows remain unverified.

```json
["node", "tools/factory/browser_checks.cjs", "accessibility", "{out}"]
```

## 61. pipdeptree (`pipdeptree`)

[Source repository](https://github.com/tox-dev/pipdeptree)

**Handles:** Detect conflicts and cycles in the real isolated backend QA
environment.

**Selection profiles:** python, supply-chain.

**Evidence scope:** New integration; see expansion execution evidence.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

```json
[
  "{pybin:pipdeptree}/pipdeptree",
  "--python",
  "{pybin:qa}/python",
  "--warn",
  "fail",
  "--json-tree"
]
```

## 62. OpenAPI Spec Validator (`openapi-spec-validator`)

[Source repository](https://github.com/python-openapi/openapi-spec-validator)

**Handles:** Validate the actual FastAPI-generated schema without starting
services or claiming endpoint behavior.

**Selection profiles:** api, python.

**Evidence scope:** New integration; see expansion execution evidence.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
["{pybin:openapi}/python", "tools/factory/openapi_check.py", "{out}"]
```

## 63. Taplo (`taplo`)

[Source repository](https://github.com/tamasfe/taplo)

**Handles:** Validate Gradle version-catalog TOML syntax without requiring Java.

**Selection profiles:** android-config, fast.

**Evidence scope:** New integration; see expansion execution evidence.

**Agent/reviewer gap:** Confirm the actual root cause and make one reviewed
repair; scanner scope is not whole-app verification.

```json
["{nodebin}/taplo", "lint", "gradle/libs.versions.toml"]
```

## 64. npm audit (`npm-audit`)

[Source repository](https://github.com/npm/cli)

**Handles:** Audit locked development npm dependencies; no automatic upgrades.

**Selection profiles:** supply-chain.

**Evidence scope:** New integration; see expansion execution evidence.

**Agent/reviewer gap:** Review compatibility, licensing and advisory relevance;
no unattended upgrades.

```json
["{nodebin}/npm", "audit", "--prefix", "{node}", "--json"]
```

## Later candidates — not implemented or counted

### CodeQL

[Source repository](https://github.com/github/codeql)

Handles: Interprocedural Kotlin/Java/Python/JS security analysis.

Prerequisite/gap: Build extraction, query scope, policy and resource budget; not
enabled.

### Schemathesis

[Source repository](https://github.com/schemathesis/schemathesis)

Handles: Generated API request/response property tests.

Prerequisite/gap: Isolated real PostgreSQL/Redis lifecycle and
destructive-operation boundaries.

### Maestro

[Source repository](https://github.com/mobile-dev-inc/maestro)

Handles: Device start/stop/save/navigation journeys.

Prerequisite/gap: Real test APK/emulator, semantic selectors and durable-state
assertions.

### Roborazzi

[Source repository](https://github.com/takahirom/roborazzi)

Handles: Native screen image regression.

Prerequisite/gap: Reachable screens, deterministic fixtures and reviewed
references.

### Macrobenchmark

[Source repository](https://android.googlesource.com/platform/frameworks/support/)

Handles: Android startup and scrolling performance.

Prerequisite/gap: Release-like internal build, physical-device sampling and
agreed budgets.

### Perfetto

[Source repository](https://android.googlesource.com/platform/external/perfetto/)

Handles: CPU, memory, scheduling and frame traces.

Prerequisite/gap: Repeatable device scenarios and interpretation; not a
pass/fail budget by itself.

### OWASP ZAP

[Source repository](https://github.com/zaproxy/zaproxy)

Handles: DAST against a disposable owned backend.

Prerequisite/gap: Authenticated fixture, allowlisted target and no third-party
crawling.

### k6

[Source repository](https://github.com/grafana/k6)

Handles: Backend concurrency, latency and error-rate checks.

Prerequisite/gap: Representative workloads, resource budget and safe dedicated
test services.

### Renovate

[Source repository](https://github.com/renovatebot/renovate)

Handles: Reviewed dependency update proposals.

Prerequisite/gap: Maintainer GitHub App authorization and PR policy; no
activation or auto-merge.

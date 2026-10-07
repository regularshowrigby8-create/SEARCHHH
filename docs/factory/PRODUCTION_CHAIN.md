# Searchhh production verification chain

This is an executable handoff structure, not a claim that every job currently passes.
Each stage consumes the previous receipt and may only hand off PASS or explicitly reviewable evidence.
FAIL, BLOCKED, missing prerequisites and missing evidence stop release approval.

Registered engines: **70**; minimum requested: **50**.

## source: Repository, policy and unfinished-source evidence

Tools: `git`, `gh`, `interrogate`, `codespell`, `yamllint`, `check-jsonschema`, `shellcheck`, `actionlint`, `prettier`, `markdownlint`, `markdown-link-check`, `finding-lifecycle`

Handoff: `source tree → source`; output `build/reports/factory/chain/source.json`.

## static: Language, formatting and complexity checks

Tools: `ripgrep`, `pytest`, `coverage`, `hypothesis`, `mutmut`, `shfmt`, `jest`, `jscpd`

Handoff: `source → static`; output `build/reports/factory/chain/static.json`.

## security: Secrets, WebView, dependency and supply-chain checks

Tools: `semgrep`, `pip-audit`, `reuse`, `pip-licenses`, `cyclonedx-py`, `detect-secrets`, `zizmor`, `npm-check-updates`, `retire`, `osv-scanner`, `syft`, `gitleaks`, `ast-grep`, `npm-audit`

Handoff: `static → security`; output `build/reports/factory/chain/security.json`.

## backend: API, schema and service checks

Tools: `ruff`, `pylint`, `mypy`, `pyright`, `bandit`, `deptry`, `vulture`, `xenon`, `hadolint`, `trivy`, `checkov`, `docker-compose`, `pipdeptree`, `openapi-spec-validator`

Handoff: `security → backend`; output `build/reports/factory/chain/backend.json`.

## android: Compile, unit tests, lint and release checks

Tools: `gradle`, `android-lint`, `detekt`, `ktlint`, `r8`, `taplo`, `ui-contract-policy`, `import-android-sdk`, `language-router`

Handoff: `backend → android`; output `build/reports/factory/chain/android.json`.

## device: Emulator, accessibility and behavioral evidence

Tools: `adb`, `eslint`, `knip`, `dependency-cruiser`, `html-validate`, `stylelint`, `playwright`, `axe-core`, `app-security-policy`, `production-chain`

Handoff: `android → device`; output `build/reports/factory/chain/device.json`.

## release: APK signature, manifest, smoke and delivery checks

Tools: `apksigner`, `apkanalyzer`, `aapt2`

Handoff: `device → release`; output `build/reports/factory/chain/release.json`.

## Stop rule

A later stage cannot erase an earlier FAIL or BLOCKED result.

A catalogue or URL is provenance, not execution evidence.

# Explicit factory repair handlers

The normal factory engines are read-only. Repair handlers are separate and must
be explicitly requested; they never run automatically as part of a quality pass.

## KtLint

```sh
SEARCHHH_ALLOW_SOURCE_FORMATTING=1 \
  python3 tools/factory/repair_handlers.py ktlint --apply
```

This invokes the project `ktlintFormat` task, records an exit report and leaves
the diff for review. It does not commit, push, sign or release anything.
Without `--apply`, the handler runs `ktlintCheck`.

## Detekt

```sh
SEARCHHH_ALLOW_SOURCE_FORMATTING=1 \
  python3 tools/factory/repair_handlers.py detekt --apply
```

This requests Detekt auto-correction where the configured Detekt rules support
it. Findings without safe auto-correct support remain failures and require an
owner to fix them. The handler does not add suppressions or alter baselines.

## Policy/source

```sh
python3 tools/factory/repair_handlers.py policy
```

The policy handler verifies the exact `Searchhh-Quality-Policy:
accepted-v1` trailer. It does not rewrite Git history or manufacture an
acknowledgment. If absent, it emits a remediation report requiring a new
compliant follow-up commit.

All handlers force `release_approved: false`. A handler result is not a quality
pass; the complete required workflow must still run afterward.

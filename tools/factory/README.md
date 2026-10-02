# Development factory

Read [the factory guide](../../docs/factory/README.md),
[58 tool integrations](../../docs/factory/TOOLS.md) and
[the execution plan](../../docs/factory/PLAN.md).

From repository root:

```sh
python3 -m tools.factory.factory list
python3 -m tools.factory.factory doctor
python3 -m tools.factory.factory run --profile fast --workers 3
```

Doctor is availability only. Missing prerequisites and quality findings remain
blocked/failed; reports do not approve the app or permit a release.

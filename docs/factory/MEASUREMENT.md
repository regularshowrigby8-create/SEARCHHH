# Measuring the 70–80% target

Current state: **NOT MEASURED**. No fabricated timing, example savings or
tool-count percentage substitutes for observation. CI elapsed time is not active
human effort.

Baseline: the already partly automated FACTORY-001 workflow at `6f94604`, not a
hypothetical manual 50-command process. Do not attribute old automation as a new
gain.

Record at least 10 comparable before/after work items in effort-samples.json.
Each record needs a unique `id`, numeric `before_minutes`, numeric
`after_minutes` and boolean `required_checks_complete`. Observe active routine
check-selection, invocation, report-reading and repeat-verification time for the
same scope. Keep failure/blocker handling in the time; never omit required
checks to improve a score. Document method, observer, source/run links and
work-item comparability alongside the ledger. Existing failed checks remain
failed; completeness means they ran, not that all findings were fixed.
Self-reported measurements need review.

```sh
python3 -m tools.factory.automation measure \
  --samples docs/factory/effort-samples.json
```

Reduction = 1 −(total active after minutes / total active before minutes).
Negative values mean regression. Fewer than 10 samples returns NOT_MEASURED with
exit 2; invalid, duplicate, nonfinite or incomplete samples cannot produce a
result. Even 70% measured here would not mean 70% of product design, security
judgments, root-cause repair or native validation has been automated.

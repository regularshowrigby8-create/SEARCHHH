# Searchhh development factory plan

One active infrastructure task, requested by the user. S00 ownership remains
unfinished.

1. **Research and audit** the existing checks and 58 named upstream tools.
   Distinct engines count once; wrappers, subcommands and individual test cases
   do not inflate the count.
2. **Provision** new Python tools in isolated, pinned environments; pin npm
   tools with an integrity lock; keep compiler/SDK prerequisites explicit.
   Record install failures instead of silently falling back to latest. No new
   Android/runtime dependency.
3. **Route** findings by language, rule, file and risk into suggested small work
   packets. Preserve raw occurrence aliases; suggestions are not semantic
   closure or permission to mass-refactor.
4. **Execute** only declared argv commands (no shell interpolation), with
   source-change detection, process-group timeouts, bounded logs and explicit
   PASS/REPORTED/FAIL/BLOCKED states. Normal runs do not install anything or
   repair source. No release/sign/publish commands.
5. **Validate** with standard-library negative tests and independent property
   tests; exercise actual tools on this repository, retaining findings as
   failures rather than baselining them away.
6. **Integrate CI** as a read-only workflow triggered by manual dispatch or
   factory-code changes only. Existing mandatory Android/backend/signing gates
   remain unchanged. Optional factory diagnostics never substitute for full
   release verification.
7. **Hand off** exact tools/versions/commands, installed-versus-executed status,
   real failures/blockers, how to select the next work packet, and remaining
   limitations. No timing improvement claimed without measurement.

Safety boundaries: one root-cause repair at a time, no blanket formatting or
dependency upgrade, no self-approved quality suppression, no secrets in reports
or APK, and no debug APK offered as a release. Some tools produce
inventories/graphs rather than gates; their successful report generation is not
a PASS for the app.

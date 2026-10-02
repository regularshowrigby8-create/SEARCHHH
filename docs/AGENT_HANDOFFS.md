# Searchhh agent handoff rules

Every UI or app agent must work one bounded ticket:

1. Read `AGENTS.md`, `docs/QUALITY_RULES.md`, the current work order and the
   relevant issue ticket.
2. Inspect the existing route, state, repository, WebView and tests before edits.
3. Reuse `SearchhhRoute`, `SearchhhMenuRegistry`, `SearchhhDesignSystem`, stable
   `SearchhhTestTags` and existing state holders.
4. Do not add placeholder routes, empty handlers, unused dependencies or copied
   sample-app architecture.
5. Test an action followed by observable state/navigation/persistence change.
6. Run the factory UI contract policy and the required checker suite.
7. Record blocked Android/device/visual evidence precisely.
8. Finish with files, dependencies, tests, failures, risks and exactly one next
   bounded task. Never claim release approval from a UI preview.

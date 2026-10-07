# Hosted production-chain failure analysis

Run inspected: `36735644919` / Searchhh checks and APK, commit
`3340ef1`. This is an evidence analysis, not a pass claim.

## Why the run failed

1. **Unfinished-code gate** failed in `tools/quality/unfinished.py`.
   The local reproduction scanned 569 files and found 178 findings, including
   imported JavaScript assets, silent catches, empty-action candidates and
   constant-return reviews. The scanner is intentionally fail-closed and the
   exceptions file is empty. This must be triaged by provenance and behavior;
   mass suppression would hide defects.
2. **KtLint** failed across `ad-filter`, `adblock-client`, Gradle scripts,
   Android tests and app sources. Confirmed examples include
   `ThemedEdgeBorderView.kt:123`, `SupernoteStorage.kt:21`, `:44` and `:67`.
   The explicit repair handler exists but was not silently run by a required
   gate because source mutation needs review.
3. **Detekt** failed in its strict task. It must be rerun after formatting and
   then handled by rule/root cause; no baseline or blanket suppression is valid.
4. **Android lint** failed with hundreds of existing findings. The latest run
   reduced the app count after the API guards, but still reported MissingTranslation,
   UseKtx, OldTargetApi, dependency/version, locale, WebView JavaScript and
   resource API issues. Lint configuration is strict, so the release lane stops.
5. **Device verification** failed during the Android 35 emulator exercise. Its
   evidence must be downloaded and inspected before any device claim; emulator
   availability alone is not a pass.
6. **Release** did not run. Because required quality and device jobs failed,
   release-build, release-smoke and publish-release-apk were skipped. No APK
   link exists for this run.

## Production handoff structure

`tools/factory/production_chain.py` builds a seven-stage chain from all 68
registered engines:

```text
source → static → security → backend → android → device → release
```

Each stage writes a receipt path and receives the previous stage receipt. A
FAIL, BLOCKED result, missing prerequisite or missing evidence stops release
approval. The generated list of all 68 engines is in
[PRODUCTION_CHAIN.md](PRODUCTION_CHAIN.md).

## Required next sequence

1. Repair unfinished findings one bounded root-cause packet at a time, starting
   with imported/vendor provenance and real silent error paths.
2. Run the explicit KtLint handler on a hosted JDK/SDK workspace, review its
   diff, then rerun Detekt.
3. Repair Detekt findings without suppressions or baselines.
4. Resolve lint groups by rule and module, beginning with API guards and the
   95 missing translations, then rerun debug and release lint.
5. Re-run emulator verification and inspect crash/instrumentation evidence.
6. Run the production chain and only then permit release signing, APK smoke and
   the downloadable artifact.

The chain is designed to make a zero-failure build possible through sequential
repair, but it does not manufacture a zero result when required evidence fails.

# Recovered quality baseline — source 6abf29b

**Status: complete raw report recovery; S00 semantic reconciliation still PARTIAL.**

All ten jobs in [recovery run 36635551405](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36635551405) passed. All five bundles decode with complete page counts, pinned source/run provenance, whole-payload SHA-256 and per-file SHA-256 checks. Recovery tooling source: `0778e6847b1b9629feab18892c9ec4737e956151`. Original report source: `6abf29b57982b93fe929674c70230bb17f6940cf`. Original quality workflows still FAIL; no release is approved.

## Files and interpretation

- `occurrences.json`: compact full raw diagnostic export, including all secondary lint locations, exact rules/messages, file/line/column hints, immutable-report occurrence addresses, complete original snapshots and compiler-prefix/task-failure extracts. Raw quality logs and duplicate HTML/SARIF text are not committed wholesale.
- `manifest.json`: dataset hash, exporter hash, original report hashes, per-report/rule totals, run/check IDs, limitations and unsuccessful recovery attempts.
- Raw addresses are **not stable semantic ticket IDs**. Records are not automatically deduplicated, assigned or fixed. The same cause can appear in debug/release, dependency reports and different tools. Do not sum these counts as unique bugs.

## Actual report totals

| Report category | Raw count | Meaning |
| --- | ---: | --- |
| App debug lint | 458 errors + 14 hints | All 34 rule groups recovered |
| App release lint | 458 errors + 14 hints | Same-source variant; not additional unique bugs |
| ad-filter debug lint | 53 errors | Includes dependency overlap |
| adblock-client debug lint | 52 errors | Includes dependency overlap |
| Detekt | 2,788 | XML count independently matches SARIF results |
| ktlint | 13,875 | Eleven full reports; every parsed rule count matches its original summary footer |
| Unfinished scan | 178 candidates | Original 542-file scan, zero checker errors; semantic audit still required |
| Interaction inventory | 617 candidates / 9 contracts | Not a dead-button or passing-behavior count |
| Required scenarios | 42, 38 with declared blockers | All snapshot entries retained; no execution adjudication invented |

## All app lint rule groups (debug; release overlaps)

| Rule | Error occurrences | Hint occurrences |
| --- | ---: | ---: |
| AndroidGradlePluginVersion | 2 | 0 |
| AppBundleLocaleChanges | 1 | 0 |
| AppLinkUrlError | 3 | 0 |
| AutoboxingStateCreation | 0 | 9 |
| AutoboxingStateValueProperty | 8 | 0 |
| ChromeOsAbiSupport | 1 | 0 |
| ClickableViewAccessibility | 2 | 0 |
| ComposableNaming | 3 | 0 |
| ConstantLocale | 1 | 0 |
| DataExtractionRules | 1 | 0 |
| DiscouragedApi | 3 | 0 |
| GradleDependency | 27 | 0 |
| InlinedApi | 4 | 0 |
| InsecureBaseConfiguration | 1 | 0 |
| IntentFilterUniqueDataAttributes | 2 | 0 |
| LocaleFolder | 1 | 0 |
| LogNotTimber | 127 | 0 |
| MissingTranslation | 92 | 0 |
| ModifierParameter | 13 | 0 |
| NewApi | 3 | 0 |
| NewerVersionAvailable | 30 | 0 |
| OldTargetApi | 2 | 0 |
| PictureInPictureIssue | 1 | 0 |
| PluralsCandidate | 2 | 0 |
| RtlHardcoded | 3 | 0 |
| SetJavaScriptEnabled | 3 | 0 |
| StaticFieldLeak | 1 | 0 |
| TrimLambda | 0 | 5 |
| Typos | 3 | 0 |
| UnusedResources | 15 | 0 |
| UseKtx | 83 | 0 |
| UseTomlInstead | 15 | 0 |
| VectorPath | 3 | 0 |
| ViewConstructor | 2 | 0 |

## Detekt rules (complete XML)

| Rule | Occurrences |
| --- | ---: |
| detekt.MagicNumber | 1058 |
| detekt.MaxLineLength | 472 |
| detekt.FunctionNaming | 228 |
| detekt.TooGenericExceptionCaught | 155 |
| detekt.ReturnCount | 131 |
| detekt.LongParameterList | 94 |
| detekt.NewLineAtEndOfFile | 93 |
| detekt.LongMethod | 92 |
| detekt.SwallowedException | 78 |
| detekt.TooManyFunctions | 73 |
| detekt.CyclomaticComplexMethod | 67 |
| detekt.WildcardImport | 54 |
| detekt.NestedBlockDepth | 27 |
| detekt.UnusedPrivateMember | 21 |
| detekt.TooGenericExceptionThrown | 20 |
| detekt.PrintStackTrace | 18 |
| detekt.UnusedParameter | 16 |
| detekt.UnusedPrivateProperty | 16 |
| detekt.ComplexCondition | 11 |
| detekt.LoopWithTooManyJumpStatements | 11 |
| detekt.ConstructorParameterNaming | 8 |
| detekt.ThrowsCount | 6 |
| detekt.FunctionParameterNaming | 5 |
| detekt.MatchingDeclarationName | 5 |
| detekt.EmptyElseBlock | 3 |
| detekt.EmptyIfBlock | 3 |
| detekt.LargeClass | 3 |
| detekt.VariableNaming | 3 |
| detekt.EmptyCatchBlock | 2 |
| detekt.EmptyFunctionBlock | 2 |
| detekt.EmptyKtFile | 2 |
| detekt.MayBeConst | 2 |
| detekt.ClassNaming | 1 |
| detekt.ExplicitItLambdaParameter | 1 |
| detekt.ForEachOnRange | 1 |
| detekt.ForbiddenComment | 1 |
| detekt.ImplicitDefaultLocale | 1 |
| detekt.InstanceOfCheckForException | 1 |
| detekt.IteratorNotThrowingNoSuchElementException | 1 |
| detekt.UseCheckOrError | 1 |
| detekt.UtilityClassWithPublicConstructor | 1 |

## Formatting scope

| Report | Occurrences |
| --- | ---: |
| `ad-filter/build/reports/ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.txt` | 2 |
| `ad-filter/build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.txt` | 224 |
| `ad-filter/build/reports/ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.txt` | 3 |
| `adblock-client/build/reports/ktlint/ktlintAndroidTestSourceSetCheck/ktlintAndroidTestSourceSetCheck.txt` | 6 |
| `adblock-client/build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.txt` | 201 |
| `adblock-client/build/reports/ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.txt` | 8 |
| `app/build/reports/ktlint/ktlintAndroidTestSourceSetCheck/ktlintAndroidTestSourceSetCheck.txt` | 269 |
| `app/build/reports/ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.txt` | 54 |
| `app/build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.txt` | 12422 |
| `app/build/reports/ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.txt` | 675 |
| `build/reports/ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.txt` | 11 |

## Newly visible risk triage — no production changes yet

- `InsecureBaseConfiguration`: `app/src/main/res/xml/network_security_config.xml:3` permits cleartext globally. Audit actual browsing, explicit HTTP approval and local backend requirements before changing defaults; record whether this safety work must precede the previously queued mirrored-background fix.
- `DataExtractionRules`: manifest backup/transfer policy needs API-specific review, including credential exclusion. Do not assume `allowBackup=false` covers every modern transfer path.
- `SetJavaScriptEnabled` (three locations): audit origins/bridges/schemes; disabling necessary browser JavaScript blindly is not a safe correctness fix.
- `StaticFieldLeak`: `LocalBackend.kt:123` requires checking application-context ownership versus an actual Activity leak, not assuming the warning proves a leak.
- Largest newly enumerated app group: `LogNotTimber` (127). Preserve diagnostics and redact secrets when migrating logging; do not delete error reporting to satisfy lint.

## Why earlier formatting recovery was rejected

Delivery formatting check `109611299928` never reached ktlint: Detekt plugin resolution failed at `build.gradle.kts:3`. Its artifact is not a clean/empty report. The recovered executed formatting artifact comes from standalone run `36628443911` at the identical source SHA. Host/release/Detekt/source artifacts come from run `36628444279`. Every original workflow remains marked failed.

## Reproduce and next action

Read `../issues/S00-transport.md` for all failed attempts and closure. Retrieve check annotations using the IDs in `manifest.json`; combine shards by report group. Run `report_transport.py decode --source 6abf29b57982b93fe929674c70230bb17f6940cf --group GROUP --annotations ANNOTATIONS.json --output /tmp/s00-bundles/GROUP.json`. Then:

```sh
python3 -m tools.quality.report_inventory --bundles /tmp/s00-bundles \
  --output /tmp/recovered-occurrences.json
python3 -m unittest discover -s tools/quality/tests -v
```

For portable hashes use the exporter bytes identified in `manifest.json`; its recovery-source provenance will differ if reports are retransported by a later workflow commit, even when underlying report bytes are identical.

**Next S00 action:** assign every recovered occurrence and unmet scenario to an explicit owner/ticket; create stable semantic IDs with collision-aware cross-report alias mapping and rule-specific regression/closure requirements. Audit the security priorities before confirming production order. Do not start S01 or mark S00 complete merely because the raw data is now available.

# Lane E — inventory of current quality debt

Inventory only. **No fix, no baseline, no suppression, no exception was added by this task** — see
§8. `AGENTS.md` work-in-progress limit stays honoured: this is a measurement pass on the ticket,
not a second implementation lane.

Ticket: `SAV-16` (Lane E). Note for the record: the brief called this "SAV-17 or the matching
ticket"; `SAV-17` is Lane F (AI reviewer), so the matching ticket is **`SAV-16`** and that is where
this file is linked from.

## 0 · Provenance — and how much of this is actually countable

| tool | numbers below come from | basis | exact source |
|---|---|---|---|
| Android Lint | check annotations `Lint priorities (bounded; full groups in ci-summary.json)` | **partial census** (top groups per report, bounded) | run [37564054844](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/37564054844), head SHA `1ae2f445fbcc440670c183174fc970823ef51fe3`; jobs `112607785842` (debug) and `112607785980` (release) |
| Lint totals | `> Task` diagnostics in the same annotations | totals only | same jobs; plus the `quality-*` log line "Lint found 465 errors and 14 hints" |
| Detekt | annotation `Analysis failed with 2602 weighted issues` | **total only; per-rule BLOCKED** | job `112607785974` (detekt lane), same run/SHA |
| ktlint | failing task names + finding lines in `Strict Android quality failed` | **task list + bounded sample; per-rule BLOCKED** | job `112607785974`, same run/SHA |
| unfinished-code | `python3 tools/quality/unfinished.py --report …` executed in this workspace | **complete census** | tree `f1e07d0665fcd535c1160f800d58ed6dd27dcce9`; report sha256 prefix `fea6cc47df6dc67c`; reproduced twice, byte-identical |
| historical per-rule | in-repo recovered records `docs/quality/verification-{4358317,6abf29b,d390b4f}.json`, `docs/BASELINE_VERIFICATION.md` | clearly-labelled history, **not** today's state | runs `36628444279`, `36610394275`, `36628443911` |

**Why the lint/Detekt/ktlint per-rule censuses are BLOCKED rather than done:** the complete
per-rule data lives in `ci-summary.json`, the Detekt XML/SARIF and the ktlint text reports, and all
of those exist only inside run artifacts. `GET /actions/artifacts/{id}/zip` redirects to
`productionresultssa*.blob.core.windows.net`, which this sandbox cannot connect to
(`curl: (35) SSL_ERROR_SYSCALL`, 4 attempts across 2 storage hosts) — the same storage limitation
that makes raw job logs unreadable, and the same one recorded in `docs/BASELINE_VERIFICATION.md`
line 99. The workflow's own annotation budget is what bounds the published groups: the app lint
report publishes 6 of 32 groups and says so (`omitted_groups: 26`).

One consequence worth naming: **`ci_summary.py` prints `new_tests`, but that field is not "tests
added since the last run"** — it is a hard-coded focused selection of 13 class names plus 3 methods
(`tools/quality/ci_summary.py:66`). It is identical (35 entries) in the baseline run and in
`37564054844`, so it cannot be used to attribute count differences. Stated here because I made that
wrong assumption once already.

## 1 · Android Lint — 1,063 reported instances, 482–584 distinct (bounded, not pinned)

Totals from the current run:

| report | errors | hints | groups | notes |
|---|---:|---:|---|---|
| `app/build/reports/lint-results-debug.xml` | 465 | 14 | 32 (6 published) | `warnings: 0` |
| `app/build/reports/lint-results-release.xml` | 465 | 14 | 32 (6 published) | identical group counts to debug |
| `ad-filter/build/reports/lint-results-debug.xml` | 53 | 0 | 6 (all published) | |
| `adblock-client/build/reports/lint-results-debug.xml` | 52 | 0 | 6 (all published) | |

Two facts about those four rows, both verified against the annotations: the app report is published in
**both** lanes with byte-identical group counts (so it is one issue set, measured twice), while the
two library reports appear **only** in the debug lane — the release job's annotation lists just
`app/build/reports/lint-results-release.xml`, so there is no release-variant library count in evidence
at all. Do not read the table as 4 × per-variant coverage.

Published groups, with the sample file the annotation names:

| rule id | severity | pri | app debug | ad-filter | adblock-client | sample file cited |
|---|---|---:|---:|---:|---:|---|
| `MissingTranslation` | Error | 8 | 99 | — | — | `app/src/main/res/values/strings.xml:11` |
| `Typos` | Error | 7 | 3 | — | — | `app/src/main/res/values-es/strings.xml:71` |
| `UseKtx` | Error | 6 | 85 | 2 | 1 | `adblock-client/src/main/java/io/github/edsuns/adblockclient/AdBlockClient.kt:164` |
| `NewApi` | Error | 6 | 3 | — | — | `app/src/main/res/values-night/styles.xml:6` |
| `SetJavaScriptEnabled` | Error | 6 | 3 | — | — | `app/src/main/java/info/plateaukao/einkbro/unit/BookmarkRenderer.kt:52` |
| `TrimLambda` | Hint | 6 | 5 | — | — | `app/src/main/java/info/plateaukao/einkbro/view/EBWebView.kt:661` |
| `GradleDependency` | Error | 4 | in omitted tail | 23 | 23 | `adblock-client/build.gradle:71` |
| `NewerVersionAvailable` | Error | 4 | in omitted tail | 23 | 23 | `gradle/libs.versions.toml:3` |
| `AndroidGradlePluginVersion` | Error | 4 | in omitted tail | 2 | 2 | `gradle/libs.versions.toml:5` |
| `UseTomlInstead` | Error | 4 | in omitted tail | 2 | 2 | `adblock-client/build.gradle:71` |
| `OldTargetApi` | Error | 6 | in omitted tail | 1 | 1 | `adblock-client/build.gradle:12` |

**The arithmetic of double counting** (why 465 is not "466 bugs"):

- Debug and release app reports are the same 479 findings for two variants → 958 rows, 479 issues.
- `ad-filter`'s report cites `adblock-client/build.gradle` and `gradle/libs.versions.toml` — files
  outside its own module — because the root build sets `checkDependencies = true`. Its 53 and
  `adblock-client`'s 52 share the same 23 `GradleDependency` + 23 `NewerVersionAvailable` + 2 + 2
  advisory rows → 105 rows, ≈59 issues.
- Sum of reported rows 1,063 = 958 app rows (479 issues × 2 variants) + 105 library rows. The
  **distinct** count is therefore bounded, and only the bounds are defensible from this evidence:
  floor **482** (if all but the 3 module-unique `UseKtx` rows are shared advisories already inside
  app's 479) and ceiling **584** (479 + 105, if nothing is shared). The middle of that range is not
  a measurement — `UseKtx 2` vs `UseKtx 1` shows the module reports are *not* byte-identical, while
  the 50-per-module advisory rows cite the same shared files. Pinning it needs the XML reports,
  which this sandbox cannot reach (BLOCKED, §0), so §6 plans against the **reported** 1,063 — which
  is what the gate counts and what a baseline or suppression would have to cover.
  Historical record agrees in kind:
  `docs/BASELINE_VERIFICATION.md` line 111 notes the raw export's 17,890 diagnostic rows "preserve
  duplicate-report/tool overlaps and are not unique bugs".
- The unpublished app tail is 479 − 198 = 281 findings in 26 groups. It is bounded, not unknown:
  the annotation sorts by `(priority desc, count desc)`, so every one of those groups is either
  priority ≤ 4 or priority 6 with count ≤ 3 — i.e. the tail is dominated by the same dependency
  advisories visible in the library reports, not by a hidden mass of correctness defects. That is an
  inference from the published ordering, and I have not verified it per rule (BLOCKED).
- Three previously-named clusters are now **clean**: `draw_allocation_count: 0`, `recycle_count: 0`,
  `exif_interface_count: 0` (history: `Recycle` 1, `ExifInterface` 8). So this area has been
  shrinking, which matters when deciding where to start.

Classification of lint groups:

| group | disposition | why |
|---|---|---|
| `GradleDependency` 23, `NewerVersionAvailable` 23, `AndroidGradlePluginVersion` 2, `UseTomlInstead` 2 | **needs human decision** | These are upgrade advisories made *blocking* by `warningsAsErrors = true` + `checkDependencies = true`. Fixing = bumping dependencies (release risk, and `tools/quality/release_apk.py` pins versions), not editing code. Deciding to stop running them is a config change → owner. **Not** exception material: an "upgrade later" note has no expiry semantics per rule 4 unless an owner sets one. |
| `MissingTranslation` 99 | **mechanical, but not automatic** | Either supply translations or mark intentionally-static strings `translatable="false"`. Requires a decision per string group, so it is bulk work with judgment, not a sed. |
| `UseKtx` 85 (app 85 / libs 3) | **mechanical** | Swap to the androidx-ktx extension calls. Large but low-risk diff; must not be batched with behavior changes. |
| `Typos` 3 | **needs human decision** | Translator-facing text in `values-es` etc.; changing copy is a product call. |
| `NewApi` 3 (`windowLayoutInDisplayCutoutMode` needs API 27, min 24) | **needs human decision** | Needs an SDK-int guard or a min-SDK policy change. Guarding changes e-ink device behaviour. |
| `SetJavaScriptEnabled` 3 | **needs human decision — security owner** | `AGENTS.md` rule 8 governs WebView script bridges; a `@SuppressLint` here would be exactly the suppression rule 4 forbids. |
| `TrimLambda` 5 (Hint) | **mechanical** | Lambda-to-method-reference trims in `EBWebView.kt`. |
| `OldTargetApi` 1 | **needs human decision** | compileSdk vs targetSdk is a release-policy knob. |

## 2 · Detekt — 2,602 weighted issues, per-rule census BLOCKED

| fact | value | source |
|---|---|---|
| task outcome | `> Task :detekt FAILED` | annotation, job `112607785974` |
| total | "Analysis failed with **2602 weighted issues**" | same annotation |
| gate config | `maxIssues: 0`, `ignoreFailures = false`, `buildUponDefaultConfig = true`, config `quality/detekt.yml` | `build.gradle.kts`, `quality/detekt.yml` — **untouched by me** (unconfirmed default #2) |
| historical entry count | 2,788 XML entries, "independently match SARIF" | `docs/quality/verification-6abf29b.json` (`detekt_issues: 2788`), `BASELINE_VERIFICATION.md:111` |
| rules visible in the bounded sample | `MagicNumber` 25, `LongMethod` 1, `CyclomaticComplexMethod` 1, `ComplexCondition` 1, `Incubating` 1 — **30 finding lines total**, all from one file each except `ThemedEdgeBorderView.kt` | annotations on job `112607785974` (3 notice annotations, ~9 KB) |

"Weighted" is not "entries": 2,602 today vs 2,788 historical entries are not directly comparable,
and I am not claiming a 6% improvement — Detekt weights by rule/severity. Per-rule counts need
`app/build/reports/detekt/*.xml` (artifact) → **BLOCKED** as in §0.

**The sample is a head, not a distribution.** 30 visible findings against 2,602 weighted issues means
25 of 30 are a single rule in a single file; GitHub truncated the failing-task annotation at exactly
4,096 characters (verified: `len == 4096`). Any per-rule share inferred from this would be an
invention, which is why the census is marked BLOCKED instead of estimated.

Disposition by what is visible: `MagicNumber` → **mechanical** (named constants), but it is dense in
view/measure code where the "magic" numbers are e-ink layout values, so each one wants a real name
→ mechanical-with-review. `LongMethod` / `CyclomaticComplexMethod` / `ComplexCondition` → **needs
human decision** (extracting state out of `BrowserActivity`/`ThemedEdgeBorderView` is a refactor with
test consequences; there is no safe sweep). `Incubating` → **mechanical** (`@OptIn`, which is not a
suppression). No Detekt baseline exists and none should be added (§6).

## 3 · ktlint — 7 failing tasks, per-rule census BLOCKED

Failing tasks in the current run (this is exactly what Lane A step A4 made possible — before, these
same violations were auto-"repaired" and pushed with `[skip ci]`):

```
:ktlintKotlinScriptCheck                FAILED   (root *.kts)
:app:ktlintMainSourceSetCheck           FAILED
:app:ktlintTestSourceSetCheck           FAILED
:app:ktlintAndroidTestSourceSetCheck    FAILED
:ad-filter:ktlintMainSourceSetCheck     FAILED
:adblock-client:ktlintMainSourceSetCheck FAILED
:detekt                                 FAILED   (separate tool, §2)
```

| fact | value |
|---|---|
| historical volume | "eleven ktlint text reports contain **13,875** occurrences with exact per-rule footer reconciliation" — `docs/BASELINE_VERIFICATION.md:111` |
| today's per-rule volume | **BLOCKED** — the reports are `*.txt` (one per source set), uploaded only inside `android-detekt-<run_id>`; and the findings-packetizer globs `*/build/reports/ktlint/*/*.xml`, so it reports **0** ktlint reports even when 11 exist (verified earlier with a fixture of this repo's real layout; `gradle_quality_queue.py` exits 1 with `ParseError` on text input) |
| rules visible in the sample | 2 finding lines, **both** explicitly flagged *"cannot be auto-corrected"*: `WebViewJsBridge.kt:637:141` "Exceeded max line length (140)" and `TranslationViewModel.kt:550:12` "Class or object name should start with an uppercase letter and use camel case" |

Disposition: ktlint is **the most mechanical** of the four tools — most rules are auto-correctable
by `ktlintFormat`, and the correct route is a human-reviewed formatting commit (never CI pushing its
own repairs, which is the failure mode Lane A deleted). The two findings that *are* visible are both
non-correctable and both in our own code: the long line in `WebViewJsBridge.kt` (a JS-bridge string,
so re-wrapping it is a human decision, not a formatter job) and `TranslationViewModel.kt:550`'s
mis-cased declaration (mechanical to rename, but it is a public-ish symbol so the rename wants a
grep for callers). Whether the remaining ~13,844 historical occurrences are mostly correctable is
**unknown**, not assumed. `.editorconfig` toggles or
`ktlint_disabled_rules` would suppress, and are therefore **not** available under rule 4.

## 4 · unfinished-code — complete census, 116 findings *(as of `f1e07d0`; §10 records what has
since been fixed for real, and the census below is deliberately left at its measured values)*

`Scanned 590 files; 116 findings; 0 checker errors`, exit 1 (fails closed, unchanged by any work
in this session). `quality/unfinished-exceptions.json` is **`[]`** — the fingerprinted-exception
mechanism exists and is currently carrying nothing.

| rule | findings | distinct files | what it flags |
|---|---:|---:|---|
| `silent-catch` | 63 | 32 | empty or contentless `catch`/`except` body — an error that vanishes |
| `empty-function-review` | 21 | 10 | function body is empty, i.e. an operation that silently does nothing |
| `constant-return-review` | 13 | 8 | function always returns a constant (`Unit`, `null`, `false`, `0`) |
| `empty-action` | 11 | 8 | lambda/callback wired to nothing |
| `unfinished-marker` | 8 | 5 | `TODO`/`FIXME`/`not implemented` marker left in scanned source |

### `silent-catch` — 63 findings over 32 files

| file | n | cited line | disposition |
|---|---:|---|---|
| `app/src/main/assets/gm_shim.js`:11 | 10 | `} catch (e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/java/info/plateaukao/einkbro/unit/ShareUtil.kt`:83 | 6 | `} catch (_: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `ad-filter/scriptlets_src/scriptlets.js`:630 | 3 | `} catch (e) {// try catch for Edge 15` | needs human decision (exception candidate only with owner + expiry + regression test) |
| `app/src/main/assets/MozReadability.js`:472 | 3 | `} catch (ex) {` | needs human decision (exception candidate only with owner + expiry + regression test) |
| `app/src/main/assets/fix_scrolling.js`:52 | 3 | `} catch (ex) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/assets/speech_synthesis_polyfill.js`:154 | 3 | `try { androidApp.ttsCancel(); } catch (e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/java/info/plateaukao/einkbro/browser/NinjaWebViewClient.kt`:439 | 3 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/unit/BookmarkRenderer.kt`:83 | 3 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/androidTest/java/info/plateaukao/einkbro/searchhh/RelayProbeTest.kt`:47 | 2 | `} catch (` | mechanical (assert or log in the test) |
| `app/src/main/assets/blob_download_hook.js`:9 | 2 | `try { window.__einkbroBlobRegistry.set(url, blob); } catch (e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/assets/translate_by_paragraph.js`:125 | 2 | `try { parentDisplay = window.getComputedStyle(parent).display; } catch (e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/java/info/plateaukao/einkbro/task/BrowserToolsImpl.kt`:378 | 2 | `} catch (_: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `backend/searchhh/domain.py`:26 | 2 | `except ValueError:` | needs human decision per site |
| `ad-filter/src/main/java/io/github/edsuns/adfilter/impl/FilterUpdater.kt`:138 | 1 | `} catch (_: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `ad-filter/src/main/java/io/github/edsuns/adfilter/impl/FilterViewModelImpl.kt`:109 | 1 | `} catch (_: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/assets/audio_only_mode.js`:44 | 1 | `} catch(e2) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/assets/dns_prefetch.js`:16 | 1 | `} catch(e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/assets/force_viewport_width.js`:30 | 1 | `} catch (e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/assets/get_remaining_images.js`:16 | 1 | `} catch(e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/assets/zoom-text-wrap-reflow.js`:325 | 1 | `} catch (e) {}` | mechanical (name the binding, route to the existing debug logger) |
| `app/src/main/java/info/plateaukao/einkbro/EinkBroApplication.kt`:263 | 1 | `} catch (_: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/activity/delegates/TaskMenuDelegate.kt`:91 | 1 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/browser/ChatWebInterface.kt`:426 | 1 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/caption/YouTubeCaptionFetcher.kt`:353 | 1 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/data/remote/OpenAiRepository.kt`:182 | 1 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/database/BookmarkDao.kt`:445 | 1 | `} catch (_: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/searchhh/local/PublicSearch.kt`:123 | 1 | `} catch (_: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/unit/BackupUnit.kt`:443 | 1 | `} catch (_: IllegalArgumentException) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/unit/HelperUnit.kt`:263 | 1 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `app/src/main/java/info/plateaukao/einkbro/view/WebViewConfigApplier.kt`:218 | 1 | `} catch (e: Exception) {` | needs human decision per site (log/narrow/propagate) |
| `backend/searchhh/extraction.py`:27 | 1 | `except (ValueError, TypeError, RecursionError):` | needs human decision per site |
| `backend/tests/stack_smoke.py`:13 | 1 | `except httpx.HTTPError: pass` | mechanical (assert or log in the test) |

Fingerprints are emitted by the checker itself (64-hex, one per finding); first entries for the three largest files of this rule: `f07c5e37e5a1`, `8dddec36c95f`, `aff26895a478`. The full set lives in the tool report; it is not duplicated into this file.

### `empty-function-review` — 21 findings over 10 files

| file | n | cited line | disposition |
|---|---:|---|---|
| `app/src/main/java/info/plateaukao/einkbro/view/MultitouchListener.kt`:133 | 6 | `open fun onSwipeRight() {}` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/view/SwipeTouchListener.kt`:63 | 4 | `open fun onSwipeRight() {}` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/activity/EpubReaderActivity.kt`:102 | 2 | `override fun addHistory(` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/browser/BrowserController.kt`:256 | 2 | `fun dismissActionMode() {}` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/browser/LazyAlbumController.kt`:40 | 2 | `override fun pauseWebView() {}` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/search/SplitSearchListType.kt`:56 | 1 | `override suspend fun addDomain(domain: String) { // use addSplitSearchItem ...` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/tts/ByteArrayMediaSource.kt`:9 | 1 | `override fun close() {` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/view/dialog/compose/BookmarkContextMenuDlgFragment.kt`:44 | 1 | `override fun adjustHorizontalPosition() {` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/view/dialog/compose/ComposeDialogFragment.kt`:130 | 1 | `protected open fun beforeComposing() {}` | needs human decision (abstract vs default) — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/view/dialog/compose/DraggableComposeDialogFragment.kt`:12 | 1 | `override fun adjustHorizontalPosition() {` | needs human decision (abstract vs default) — exception candidate |

Fingerprints are emitted by the checker itself (64-hex, one per finding); first entries for the three largest files of this rule: `66e27670c0a8`, `66e27670c0a8`, `9ad2aaea5564`. The full set lives in the tool report; it is not duplicated into this file.

### `constant-return-review` — 13 findings over 8 files

| file | n | cited line | disposition |
|---|---:|---|---|
| `app/src/main/java/info/plateaukao/einkbro/browser/BrowserController.kt`:248 | 3 | `fun showTocDialog() = Unit` | needs human decision — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/searchhh/local/RelayBridge.kt`:46 | 3 | `override fun add(` | needs human decision — exception candidate |
| `app/src/test/java/info/plateaukao/einkbro/preference/FakeSharedPreferences.kt`:72 | 2 | `override fun registerOnSharedPreferenceChangeListener(listener: SharedPrefe...` | mechanical (assert or log in the test) |
| `app/src/main/java/info/plateaukao/einkbro/activity/delegates/FullscreenDelegate.kt`:38 | 1 | `override fun onError(` | needs human decision — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/searchhh/local/InternalBackendService.kt`:21 | 1 | `override fun onBind(intent: Intent?): IBinder? = null` | fingerprinted-exception candidate (API requires null) |
| `app/src/main/java/info/plateaukao/einkbro/service/ClearService.kt`:24 | 1 | `override fun onBind(intent: Intent): IBinder? = null` | fingerprinted-exception candidate (API requires null) |
| `app/src/main/java/info/plateaukao/einkbro/view/SwipeTouchListener.kt`:28 | 1 | `override fun onDown(e: MotionEvent): Boolean = false` | needs human decision — exception candidate |
| `app/src/main/java/info/plateaukao/einkbro/view/ThemedBorders.kt`:701 | 1 | `override fun onViewDetachedFromWindow(v: View) = Unit` | needs human decision — exception candidate |

Fingerprints are emitted by the checker itself (64-hex, one per finding); first entries for the three largest files of this rule: `5bcf25b061ad`, `4549fcc661c9`, `2641a83c5046`. The full set lives in the tool report; it is not duplicated into this file.

### `empty-action` — 11 findings over 8 files

| file | n | cited line | disposition |
|---|---:|---|---|
| `app/src/main/java/info/plateaukao/einkbro/view/dialog/compose/BookmarksDialogFragment.kt`:682 | 4 | `onItemMoved = { _, _ -> },` | needs human decision (product intent) |
| `app/src/main/java/info/plateaukao/einkbro/unit/BackupUnit.kt`:512 | 1 | `zipEntry.name == MANIFEST_FILE -> { /* skip */ }` | needs human decision |
| `app/src/main/java/info/plateaukao/einkbro/view/compose/AutoCompleteTextField.kt`:100 | 1 | `onLongClick = { _, _ -> },` | needs human decision (product intent) |
| `app/src/main/java/info/plateaukao/einkbro/view/compose/BrowseHistoryList.kt`:350 | 1 | `onLongClick = { _, _ -> },` | needs human decision (product intent) |
| `app/src/main/java/info/plateaukao/einkbro/view/compose/HistoryAndTabs.kt`:458 | 1 | `onHistoryItemLongClick = { _, _ -> },` | needs human decision |
| `app/src/main/java/info/plateaukao/einkbro/view/compose/Toolbar.kt`:363 | 1 | `Spacer1, Spacer2 -> { /* handled in ComposedIconBar */ }` | needs human decision |
| `app/src/main/java/info/plateaukao/einkbro/view/dialog/compose/ThemeColorDialogFragment.kt`:779 | 1 | `}, onGradientPicked = { _, _ -> }, onCustomColorPreview = { Unit }, onCusto...` | needs human decision (product intent) |
| `app/src/main/java/info/plateaukao/einkbro/view/viewControllers/TouchAreaViewController.kt`:165 | 1 | `TouchAreaType.Ebook -> {` | needs human decision |

Fingerprints are emitted by the checker itself (64-hex, one per finding); first entries for the three largest files of this rule: `8a45856de3b7`, `323cdcc39264`, `10649b90a331`. The full set lives in the tool report; it is not duplicated into this file.

### `unfinished-marker` — 8 findings over 5 files

| file | n | cited line | disposition |
|---|---:|---|---|
| `app/src/main/assets/MozReadability.js`:2161 | 3 | `* TODO: Test if getElementsByTagName(*) is faster.` | needs human decision (exception candidate only with owner + expiry + regression test) |
| `ad-filter/scriptlets_src/scriptlets.js`:259 | 2 | `* TODO think about nested dependencies, but be careful with dependency loops` | needs human decision (exception candidate only with owner + expiry + regression test) |
| `ad-filter/src/main/java/io/github/edsuns/adfilter/impl/Detector.kt`:133 | 1 | `// TODO: validate custom rules because rules containing any wrong will break` | needs human decision (resolve or plan; cannot be deleted) |
| `app/src/main/java/info/plateaukao/einkbro/epub/EpubXMLFileParser.kt`:26 | 1 | `// TODO` | needs human decision (resolve or plan; cannot be deleted) |
| `app/src/main/java/info/plateaukao/einkbro/viewmodel/TtsViewModel.kt`:230 | 1 | `// TODO` | needs human decision (resolve or plan; cannot be deleted) |

Fingerprints are emitted by the checker itself (64-hex, one per finding); first entries for the three largest files of this rule: `05716dce7a99`, `1bf91037b2ac`, `23e0f1dbac95`. The full set lives in the tool report; it is not duplicated into this file.

Roll-up of every finding, computed from the same per-line classification used in the tables above —
each finding is counted exactly once, so this sums to the 116 total:

| cluster | findings | disposition | sample sites |
|---|---:|---|---|
| production swallow | 26 | needs human decision per site (log/narrow/propagate) | `ShareUtil.kt` x6, `NinjaWebViewClient.kt` x3, `BookmarkRenderer.kt` x3 |
| our injected script | 25 | mechanical (name the binding, route to the existing debug logger) | `gm_shim.js` x10, `fix_scrolling.js` x3, `speech_synthesis_polyfill.js` x3 |
| listener/view default no-op | 21 | needs human decision (abstract vs default) — exception candidate | `MultitouchListener.kt` x6, `SwipeTouchListener.kt` x4, `EpubReaderActivity.kt` x2 |
| vendored upstream code | 11 | needs human decision (exception candidate only with owner + expiry + regression test) | `MozReadability.js` x6, `scriptlets.js` x5 |
| interface default returning a constant | 9 | needs human decision — exception candidate | `BrowserController.kt` x3, `RelayBridge.kt` x3, `FullscreenDelegate.kt` x1 |
| other | 6 | needs human decision | `BookmarksDialogFragment.kt` x2, `BackupUnit.kt` x1, `HistoryAndTabs.kt` x1 |
| Compose callback deliberately not wired | 5 | needs human decision (product intent) | `BookmarksDialogFragment.kt` x2, `AutoCompleteTextField.kt` x1, `BrowseHistoryList.kt` x1 |
| test code | 5 | mechanical (assert or log in the test) | `RelayProbeTest.kt` x2, `FakeSharedPreferences.kt` x2, `stack_smoke.py` x1 |
| backend best-effort path | 3 | needs human decision per site | `domain.py` x2, `extraction.py` x1 |
| bare or upstream TODO in our code | 3 | needs human decision (resolve or plan; cannot be deleted) | `Detector.kt` x1, `EpubXMLFileParser.kt` x1, `TtsViewModel.kt` x1 |
| Android unbound-service contract | 2 | fingerprinted-exception candidate (API requires null) | `InternalBackendService.kt` x1, `ClearService.kt` x1 |
| **total** | **116** | (must equal 116) | |

How to read the buckets: **mechanical** means a reviewer can verify the change from the diff alone;
**fingerprinted-exception candidate** means the code is correct as written and only the checker
cannot know that, so rule 4's channel (fingerprint + reason + owner + expiry + regression test) is
the sole policy-compliant route; **needs human decision** means the right behaviour is genuinely
undetermined — best-effort cleanup vs propagate, product intent, or upstream provenance.

Two things this roll-up makes visible that a raw count does not. The vendored-upstream group (11
findings) cannot be cleared by editing our code without forking upstream copies, and the three
API-shape groups (listener/view defaults 21, constant-returning interface defaults 9,
`onBind … = null` 2 = **32**) are largely code that is correct by contract. Together that is **43 of
116 — 37% of the unfinished-code debt that a blanket "just fix it" pass would get wrong**, which is
the strongest argument for the per-finding route in §5 rather than a sweep.

The `other` bucket (6) is not a tool defect: those are `empty-action` sites whose intent is stated in
a nearby comment (`/* skip */`, `/* handled in ComposedIconBar */`) or in a `when` branch, so a human
has to confirm the intent and either document it as an exception or wire the behaviour.

## 5 · `app/lint-baseline.xml` — not active, and rule 4 does not allow it

**Active? No.** Four independent proofs:

1. No build script references it: `grep -rn "lint-baseline" --include='*.kts' --include='*.gradle'
   --include='*.properties' --include='*.yml' --include='*.yaml' .` → **empty** (excluding
   `**/build/`). The only `lint { }` block in `app/build.gradle.kts` (lines 222-227) sets
   `abortOnError`, `warningsAsErrors`, `checkReleaseBuilds`, `checkDependencies` — no `baseline`.
2. Empirically decisive: the file contains `Typos` 3, `TrimLambda` 5 and `NewApi` 3 — exactly the
   counts lint reports today. If the baseline were applied those findings would be suppressed; they
   are reported as errors/hints and fail the lane.
3. Its header is `by="lint 8.13.2" variant="all" dependencies="false"`, while the build now runs
   with `checkDependencies = true` — the entry shapes could not match current reports even if wired
   up.
4. Its largest group, `LogNotTimber` (112 of 255 entries), refers to a rule that does not appear in
   the current published groups at all.
5. And it would not even work as a shortcut: its `UseKtx` coverage is **73 entries against the 85**
   `UseKtx` errors reported today, so wiring it back up would leave 12 of that one group firing. A
   baseline is a snapshot of one moment, which is precisely why rule 4 forbids leaning on one.

File facts (from the parsed XML, not `grep -c`, which also counts a nested occurrence): 2,805 lines,
255 `<issue>` elements, 31 distinct ids — top groups `LogNotTimber` 112, `UseKtx` 73,
`AutoboxingStateValueProperty` 8, `TrimLambda` 5.

**Does `AGENTS.md` rule 4 allow it? No.** Rule 4: *"No blanket baselines, disabled tests, ignored CI
failures, swallowed exceptions, removal of unfinished markers without resolving the underlying
issue, or suppression to make checks green. Legitimate exceptions need exact fingerprints, reasons,
owner, expiry and regression tests."* A lint baseline is a blanket, id+file-keyed suppression with no
reason/owner/expiry/regression-test fields, so it is the named prohibited category.
`docs/QUALITY_RULES.md` §6 says the same in the project's own words: *"Do not add blanket
suppressions or a new lint/Detekt debt baseline. The old lint baseline remains historical evidence
and is no longer applied."*

So the file's current status — present, unreferenced, retained as historical evidence — is exactly
what the policy prescribes. **Re-activating it, extending it, or adding a Detekt baseline would each
be a policy change and are not offered.** Deleting the file is *also* an owner decision, because
§6's sentence refers to it; my recommendation is to leave it untouched and let the inventory above
replace it as the shared picture.

The sanctioned route for the genuinely-legitimate cases is the per-finding fingerprint channel the
checker already writes: exact 64-hex fingerprint + reason + owner + expiry + regression test, into
`quality/unfinished-exceptions.json`. That file is empty today. **I added no entries.**

## 6 · Proposed remediation clusters (not started)

Ordering by (unique findings cleared) ÷ (release risk). Nothing here is done; this is the queue the
owner can pick from, one cluster per ticket under the WIP limit.

| # | cluster | ≈ findings | route | effect if cleared |
|---:|---|---:|---|---|
| C1 | `UseKtx` app+libs | 88 | mechanical | −88 lint errors |
| C2 | ktlint auto-correctable rules | **unknown** correctable share (both currently-visible findings are marked non-correctable) | `ktlintFormat` in one human-reviewed commit | clears a task only if *every* finding in that source set is correctable |
| C3 | non-auto-correctable ktlint (`MaxLineLength`, casing) | unknown tail | hand edits, high churn, touches call sites | remaining ktlint tasks |
| C4 | our injected-script swallows | 25 | mechanical + small behavior review | −25 unfinished findings |
| C5 | `MissingTranslation` | 99 | **9 keys need a product/translation decision + 90 need bulk translations**; `translatable="false"` is only correct for internal strings, and none of the 99 is internal (see §11) | −99 lint errors, but only via translations |
| C6 | dependency advisories (`GradleDependency` 46, `NewerVersionAvailable` 46, AGP 4, `UseTomlInstead` 4, `OldTargetApi` 2, plus 3 `UseKtx`) | 105 rows / ≈59 unique | **decision**: upgrade cadence, or the scope of `checkDependencies` / `warningsAsErrors` — the latter is a policy change, not a fix | −105 rows from both library reports |
| C7 | Android API-shape no-ops (listener defaults 21, constant interface defaults 9, `onBind` 2) | 32 | mostly fingerprinted exceptions (owner+expiry+regression test each); alternative is abstracting the listener bases | −32 unfinished findings without changing behaviour |
| C8 | Detekt `MagicNumber` etc. | subset of 2,602 weighted | mixed; needs the blocked census first | unblocks `:detekt` only if the whole 2,602 goes to 0 |
| C9 | production swallows (Kotlin/Java 26, backend 3) | 29 | decision per site: narrow, log, or propagate | −29 unfinished findings |
| C10 | `SetJavaScriptEnabled`, `NewApi`, `OldTargetApi`, `Typos` | 10 | **security/product owner decisions** (rule 8 applies to the WebView one) | −10 lint errors |
| C11 | vendored `MozReadability.js` (6) + `scriptlets.js` (5) | 11 | **policy question** (§4/§9) — editing forks upstream copies, and rule 4 has no "third-party" field | −11 unfinished findings, or a documented rule change |
| C12 | TODO markers 3 (incl. `Detector.kt`), Compose callbacks not wired 5, comment-stated intents 6 | 14 | decision | −14 unfinished findings |

**Reconciliation check on the clusters** (so the table above can be audited without the tool): the six
unfinished-code clusters are C4 25 + C7 32 + C9 29 + C11 11 + C12 14 + the 5 test-code findings
= **116**, which is exactly the census total — every finding is in one cluster and none is
double-counted. The lint clusters are deliberately *not* reconciled to 479, because 281 of them are
in the unpublished tail and cannot be assigned to a cluster yet.

Sequencing note that the numbers force: **no lint-only or ktlint-only effort can turn any lane
green.** `required-quality` needs *all* of `policy-and-source` (unfinished-code 116 → 0, and
`:verifyInteractionEvidence`), `detekt` (2,602 weighted → 0), `build-test-lint` (465+14 and
library lint → 0) and `release-checks` (release lint + R8) to clear, because `maxIssues: 0` and
`warningsAsErrors = true` are zero-tolerance. So the only end-to-end plans are "clear everything"
(C1-C12, large) or an owner-approved change to the zero-tolerance config. That is a policy decision
and the reason this task stops at inventory.

## 7 · Also-failing gates that are not "debt findings"

Listed so the totals are not misread as the whole path to green:

- `:verifyInteractionEvidence` → `evidence.py` reports missing/stale executed evidence for the
  inventoried controls (622 candidates, 9 contracts). It is an evidence-completeness gate, not a
  lint count; `python3 tools/quality/interactions.py --check` exits 0, so the *inventory* is current
  while the *executed* evidence is not.
- `:app:minifyReleaseWithR8` in the `release-checks` lane shows `Missing class` lines in the
  current run's truncated annotation; history names `CheckReturnValue`, `Immutable`,
  `RestrictedApi`, `ManagementFactory`, `RuntimeMXBean`. Current status **BLOCKED** — the
  annotation is cut at 4,096 chars and the log is unreachable, so I am not claiming it either way.
- `release-build` / `release-smoke` / `publish-release-apk` remain `skipped` because
  `required-quality` fails first: `framework` ← `quality`, `backend-integration` ← `framework`,
  `android` ← `backend-integration`, `device-smoke` ← `android`, `release-build` ← `device-smoke`,
  `release-smoke` ← `release-build`, `publish-release-apk` ← both. Verified from the parsed
  workflow, and observed in every measured run.

## 8 · What this task did not do

- No `lint-baseline.xml` (re-)activation, no new baseline of any kind, no `.editorconfig` or
  `ktlint_disabled_rules` change, no `@SuppressLint`, no `tools:ignore`, no `// ktlint-disable`,
  no `continue-on-error`, no rule-severity edits in `quality/detekt.yml`, no dependency bumps.
- No entry added to `quality/unfinished-exceptions.json` — it remains `[]`.
- No production or test source file changed. No marker removed.
- No gate or lane reconfigured; `required-quality` untouched.
- Only this file was written, and the ticket updated with a link to it.

## 9 · Open questions for the owner

1. Do `AGENTS.md` rule 4 / `QUALITY_RULES.md` §5 intend to hold **vendored upstream copies**
   (`MozReadability.js` 6, `scriptlets.js` 5) to the same silent-catch/TODO standard as our code?
   11 findings hang on this, and neither a code edit (forks upstream) nor an exception (no
   "it's third-party" field exists in rule 4) is safe to choose on my own.
2. Is `warningsAsErrors = true` with `checkDependencies = true` intended to make ~46 *advisory*
   dependency findings **blocking** on every PR? If not, the fix is a config change — yours.
3. Will the Android-API-shape group (32 findings: listener/view defaults, constant-returning
   interface defaults, `onBind … = null`) and the test-double/test-code group (5) get fingerprinted
   exceptions with an owner and an expiry, or do you want the listener bases refactored to abstract
   members? Those two clusters are 37 of 116 findings and neither route is mechanical.
4. `SetJavaScriptEnabled` (3) is WebView-script surface: rule 8 says security-relevant changes need
   the security owner. Who signs that off, and is the expected outcome a guard or a documented
   exception?
5. Can the blocked censuses be unblocked cheaply? Two options: keep `ci-summary.json` and the Detekt
   XML as a *checked-in-at-a-tag* summary artifact, or fix `report_transport`/`gradle_quality_queue`
   to read ktlint text (pre-existing gap, §3) — then this inventory becomes exact without Gradle
   access. Both are small; both touch tool code, so they need their own ticket under the WIP limit.
6. Should `app/lint-baseline.xml` stay as inert historical evidence (current policy text), or be
   deleted with §6 amended? Either way it is your edit, not mine.

## 10 · Remediation log — what was fixed after this inventory, and what was not

Recorded here so the census above is never mistaken for the current state. Every batch was a real
code change: no baseline, no `tools:ignore`, no `@SuppressLint`, no `ktlint-disable`, no severity
edit, and `quality/unfinished-exceptions.json` is still `[]`.

| batch | commit | what | census before → after | lint | how it was verified without a compiler |
|---|---|---|---:|---|---|
| A | `6adf92e` | 25 empty `catch` blocks in `app/src/main/assets/*.js` — our injected page scripts | 116 → 91 | unchanged | `node --check` on all 40 asset scripts (0 failures, 0 before as well); token-stream diff proving **0 tokens removed, +25 `console.*` calls only, brace counts identical**, every inserted identifier an existing catch binding |
| B | `60e3733` | 7 swallowed failures in Kotlin where silence = "the user's action did nothing" (external-link launch ×2, EPUB rollback delete, background WebView teardown, filter-data clear, provider error-body read ×2) | 91 → 84 | unchanged | per-file brace balance net-zero; one added statement per existing `catch`, each using that file's own logger idiom (`Timber.w`, `Log.w`, or `android.util.Log.e` where the file imports no logger), every added line < ktlint's 140 limit; compile + the 371 tests verified by CI only |
| C | `06b44f2` | the one genuine defect the audit found in this cluster: `FilterViewModelImpl`'s restart-resume loop wrapped **all** filters in one empty catch, so a throw on filter *n* left every later filter stuck as "running" and never resumed; now guarded per filter and logged. Plus the two `ShareUtil` sites that swallowed `SocketException` when creating/joining the multicast socket (share discovery silently never started). | 84 → 81 | unchanged | delimiter-profile delta per file (net braces, min depth, net parens, net brackets all 0→0 — the restructure cannot have shifted balance); `Timber` import inserted in ktlint's sorted position; the neighbouring `receive()`-loop `SocketException` deliberately left silent because it fires per iteration; CI ran the 371 tests green at `8130bda` (`device-and-evidence`: `{passed: 371, failure: 0, error: 0, skipped: 0}`) |
| — | not done | 28 remaining `silent-catch`: fallbacks inside per-item / per-request loops (`BookmarkRenderer` ×3, the ad-block path parse, `BookmarkDao` and `ShareUtil`'s teardown/receive paths, `PublicSearch`'s discovery loop, `BackupUnit`'s enum-restore loop, `HelperUnit`'s host parse, `EinkBroApplication`'s Chromium-detection probe, `ChatWebInterface` which already returns the error to its caller) | 81 | — | **left alone deliberately**: a log line inside those loops converts one real failure into thousands on a device with no log buffer to spare. Needs per-site judgment, i.e. §9 question 3. |

Nothing in the two batches above reduces a *lint* count, which is the point worth being blunt about:
`required-quality` still fails on 81 unfinished findings + 465 lint errors + 2,602 Detekt issues +
7 ktlint tasks, so there is still no APK. The code that ships is more debuggable, not greener.


## 11 · `MissingTranslation` decomposed — the 99 findings are 9 + 90, and none is fixable mechanically

Computed locally from `app/src/main/res/values*/` (no lint report needed): `654` keys in the default config, **30** locale folders qualify as translations (≥50% key coverage), 4 folders are configuration qualifiers rather than languages (`values-night-v31`, `values-night`, `values-v31`, `values-v34`). The rule fires **once per key**, not once per key×locale — which is why 2,528 missing (key, locale) pairs are reported as 99 findings, and it reproduces the `MissingTranslation 99` group of run 37564054844 exactly.

- **9 keys are absent from every translated locale**: added in English only. One decision each.
- **90 keys are present in most locales and missing from a few**: drift, mostly one new key per locale batch.

### A. The 9 never-localised keys — these are the ones worth deciding first

| key | English text | referenced from | suggested action |
|---|---|---|---|
| `error_download_save_failed` | “Downloaded, but saving the file failed” | unit/DownloadHelper.kt | translate (error text shown to the user) |
| `searchhh_webview_back` | “Back” | searchhh/web/SearchhhResultsWebViewActivity.kt | translate (content description: TalkBack reads it) |
| `searchhh_webview_reload` | “Reload” | searchhh/web/SearchhhResultsWebViewActivity.kt | translate (content description) |
| `searchhh_webview_results` | “Searchhh results” | searchhh/web/SearchhhResultsWebViewActivity.kt | translate (content description) |
| `searchhh_webview_title` | “Searchhh WebView” | searchhh/web/SearchhhResultsWebViewActivity.kt | translate or `translatable="false"` if it is only a debug-screen title |
| `setting_summary_http` | “Allow insecure HTTP top-level navigation. Keep disabled ” | setting/screens/StartSettings.kt | translate (settings screen summary) |
| `setting_title_http` | “Allow HTTP pages” | setting/screens/StartSettings.kt | translate (settings screen title) |
| `site_webview_dark_mode` | “WebView dark mode” | activity/SiteRuleListActivity.kt, view/dialog/compose/SiteSettingsDialogFragment.kt | translate (settings entry title) |
| `toast_http_blocked` | “HTTP navigation blocked. Enable Allow HTTP pages only fo” | browser/NinjaWebViewClient.kt | translate (it is a toast: user-visible by definition) |

All 9 are read at runtime by the app (nothing here is dead), so **`translatable="false"` is the wrong instrument for most of them** — the honest fix is translations, or `translatable="false"` only where the string is genuinely internal (which the reference column above is meant to show). Marking a user-visible string non-translatable silences the finding and leaves 30 languages showing English: that is suppression with extra steps, so no entry was changed by this inventory.

### B. The 90 drift keys — missing per locale, no decision needed to start

This is a mechanical backlog: each row needs the string added to the listed locale files. Keys are ordered by how many locales lack them.

| key | English text | missing in | n |
|---|---|---|---:|
| `backup_category_database_data` | “Database (Highlights, AI Queries, Site Setti” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `changelog_url` | “https://plateaukao.github.io/einkbro/downloa” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rCN | 29 |
| `default_value_hint` | “default” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `gemini_in_place` | “Gemini in-place” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `go_to` | “Click” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `menu_save_mht` | “Save as MHT” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `openai_in_place` | “OpenAI in-place” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `reset_to_global` | “Reset All to Global” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `search_settings_hint` | “Search settings…” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_section_advanced` | “Advanced” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_section_typography` | “Typography” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_summary_share_long_press` | “Action when long pressing the share icon” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_summary_userscripts` | “Manage Tampermonkey-style userscripts” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_summary_video_autoplay` | “Allow videos to play automatically without u” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_title_share_long_press` | “Share long press action” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_title_userscripts` | “Userscripts” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `setting_title_video_autoplay` | “Allow video autoplay” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `share_long_press_copy_link` | “Copy link” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `share_long_press_last_target` | “Share to last app” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `site_custom_css` | “Custom CSS” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `site_post_load_js` | “Post-Load JavaScript” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `site_settings` | “Site Settings” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `site_settings_overrides_count` | “%1$d override” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `site_settings_overrides_count_plural` | “%1$d overrides” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_add` | “Add userscript” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_browse` | “Find userscripts on Greasy Fork” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_code` | “Userscript code” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_empty` | “No userscripts installed yet. Tap + to add o” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_fetch` | “Fetch” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_install_from_url` | “Install from URL” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_no_update_source` | “No update URL for this script” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_up_to_date` | “Already up to date” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_update` | “Check for update” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_update_failed` | “Update check failed” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `userscript_updated` | “Updated to v%1$s” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi, zh-rTW | 29 |
| `action_category_ai` | “AI” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_bookmarks` | “Bookmarks & History” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_content` | “Content” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_dialog` | “Dialogs & UI” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_file` | “Files” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_navigation` | “Navigation” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_search` | “Search” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_share` | “Share” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_tab` | “Tabs” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_touch` | “Touch” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_translation` | “Translation” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_tts` | “Text-to-speech” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_category_view` | “View” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_fast_toggle` | “Fast toggle” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_save_web_archive` | “Save web archive” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_selected_label` | “Selected action” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_summarize_content` | “Summarize page” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `action_text_search` | “Text search” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `setting_section_statusbar` | “Info bar” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `setting_section_toolbar` | “Toolbar” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `setting_summary_statusbar_enabled` | “A slim bar with time, page info, battery, wi” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `setting_summary_statusbar_items` | “Choose which items appear and reorder them” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `setting_title_statusbar_enabled` | “Show info bar when toolbar is hidden” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `setting_title_statusbar_items` | “Info bar items” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `setting_title_statusbar_position` | “Info bar position” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `share_receiving` | “Receiving data…” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_config_available` | “Available items” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_config_available_hint` | “click icon to add it to the info bar” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_config_preview` | “Preview” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_config_preview_hint` | “click icon to remove; long click to drag and” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_item_battery` | “Battery” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_item_volume_pagination` | “Volume page turn” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_item_wifi` | “Wifi” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_position_bottom` | “Bottom” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `statusbar_position_top` | “Top” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_custom` | “Custom task…” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_custom_desc` | “Describe a multi-step task in natural langua” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_custom_hint` | “e.g. open the top 3 story links and give me ” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_menu_title` | “Tasks” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_read_article_list` | “News anchor” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_read_article_list_desc` | “Extract article links from this page, then o” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_requires_openai` | “Custom tasks require OpenAI (not Gemini).” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_run` | “Run” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `task_unknown` | “Unknown task.” | af, ar, ca, cs, da, de, el, es, fi, fr, hu, in, it, iw, ja, ko, nl, no, pl, pt, ro, ru, sat, sr, sv, tr, uk, vi | 28 |
| `backup_category_chat_sessions` | “AI Chat Sessions” | sat | 1 |
| `backup_category_transcripts` | “Video Transcripts” | sat | 1 |
| `backup_category_userscripts` | “Userscripts” | sat | 1 |
| `menu_save_archive` | “Save for later” | ru | 1 |
| `setting_summary_ui_theme` | “Accent color for buttons, borders, and dialo” | sat | 1 |
| `setting_title_border` | “Border” | sat | 1 |
| `setting_title_export_userscripts` | “Export userscripts” | sat | 1 |
| `setting_title_fill` | “Fill” | sat | 1 |
| `setting_title_import_userscripts` | “Import userscripts” | sat | 1 |
| `setting_title_ui_theme` | “Theme” | sat | 1 |
| `theme_section_color` | “Color” | sat | 1 |

### What this changes about the remediation plan

`docs/ci-plan/debt-inventory.md` §6 cluster C5 said “`MissingTranslation` 99, mixed”. Now it is: **9 keys needing a per-key product/translation decision, plus 90 keys needing bulk translation additions**. Neither is a lint-baseline-shaped problem, and both are translation work rather than code work — which is why the count is still 99 and not 0 after the code batches that did land.


## 12 · Why no APK exists at this head, stated exactly

`release-build` is unreachable from `required-quality`, and `required-quality` needs all four source
lanes at zero. The remaining honest path is ordered by what CI can actually verify in this
environment (no JVM/SDK here, and the Maven/Gradle/Google endpoints refuse connections, so this
sandbox can never build):

1. `UseKtx` 85 and `TrimLambda` 5 — mechanical, but the **per-site list is unreadable** here (only
   counts and one sample file are published), so any sweep would be guesswork; needs `ci-summary.json`
   or the lint XML reachable (§9 question 5). This is why they were not attempted, not because they
   look hard.
2. `MissingTranslation` 99 — translation work, §11 gives the exact key list; not code work.
3. Detekt 2,602 weighted / ktlint 7 tasks — blocked on the same census gap.
4. `:verifyInteractionEvidence` — 42 required scenarios in `quality/required-scenarios.json`, of
   which **31 carry only `{"blocker": "Not yet linked to complete executed behavior evidence"}`**.
   Those need instrumented tests that *execute* on a device and then satisfy `evidence.py`'s
   contract check (an action followed by an assertion, plus fresh JUnit XML). Writing tests that
   pattern-match the checker without exercising behaviour is gate-gaming, not evidence, so it was
   not done. This is Lane B's territory, which I was told not to start.


# Searchhh project audit — pre-implementation

Date: 2026-09-29. Baseline: `dbd9366c6ab57cdd17d503fa70555a94ccb5a1d6`.
This document was saved before quality-system implementation. Automated source reading/indexing is not a claim that every behavior has been manually verified.

## Scope and architecture

- Root project `einkbro`; modules `app`, `ad-filter`, `adblock-client` (native ad-blocking).
- Launcher `info.plateaukao.einkbro.searchhh.SearchhhActivity`; application ID `app.searchhh.browser`; namespace `info.plateaukao.einkbro`. Preserve these identities.
- AGP 8.13.2; Gradle 8.14.5; Kotlin 2.1.20; KSP 2.1.20-1.0.32; Java 17; compile/target SDK 36; minimum SDK 24 in app.
- Compose 1.7.8 and **Material 2**, not Material 3. AndroidX WebKit 1.11.0, Navigation 2.8.9, Room 2.6.1, coroutines 1.10.2, OkHttp 4.12.0. Existing Compose instrumentation, MockK and MockWebServer can be reused. Retrofit/Room/WorkManager coexist with legacy MVVM/Koin and activity delegates.
- 327 main Java/Kotlin files; 59 Kotlin files reference `@Composable`. Automated inspection read 488 source files across Android modules, injected JavaScript, backend and JS tests. Config/docs/navigation/theme/ViewModel/repository/WebView/test index: `quality/baseline-file-index.json` (206 files, content hashes). Source findings: `quality/baseline-source-findings.json`.
- Searchhh has four integer-selected bottom tabs: Discover, Sources, Saved, Settings. Browser is a separate explicit Activity intent. No Searchhh drawer, dedicated history route, typed central route registry or general retry control. Legacy browser has its own history, menus and settings; they are not equivalent to missing Searchhh routes.
- Searchhh state mostly lives inside one composable; coroutine/UI events call Retrofit/local service, poll status, then update Compose state. Saved items use Room flow; persistent job/status use preferences. Lifecycle polling exists; stop confirmation can fail and reports that fact. Process/recreation, cancellation and UI error/retry coverage remain incomplete.
- Result URLs passed to BrowserActivity retain the original URI; entry allows both HTTP and HTTPS. Sources/codebase counts are catalogues, not independently integrated runtimes. 120 listed codebases must not be reported as 120 working crawlers.

## Screens and navigation entry points

- `.searchhh.SearchhhActivity`
- `.activity.DictActivity`
- `.activity.SettingActivity`
- `.activity.ExtraBrowserActivity`
- `.activity.BrowserActivity`
- `.activity.EpubReaderActivity`
- `.activity.DataListActivity`
- `.activity.GptActionsActivity`
- `.activity.GptQueryListActivity`
- `.activity.HighlightsActivity`
- `.activity.ToolbarConfigActivity`
- `.activity.MenuItemHideActivity`
- `.activity.StatusbarConfigActivity`
- `.activity.AdBlockSettingActivity`
- `.activity.SavedPagesActivity`
- `.activity.UserScriptListActivity`
- `.activity.SiteSettingsActivity`
- `.activity.SiteRuleListActivity`

## Source/state/test inventory

ViewModels:
- `app/src/main/java/info/plateaukao/einkbro/search/suggestion/SearchSuggestionViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/ActionModeMenuViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/AlbumViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/BookmarkViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/ExternalSearchViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/GptQueryViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/HighlightViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/InstapaperViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/RemoteConnViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/SavedPageViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/SplitSearchViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/TranslationViewModel.kt`
- `app/src/main/java/info/plateaukao/einkbro/viewmodel/TtsViewModel.kt`

Repositories:
- `app/src/main/java/info/plateaukao/einkbro/data/remote/GoogleDriveRepository.kt`
- `app/src/main/java/info/plateaukao/einkbro/data/remote/InstapaperRepository.kt`
- `app/src/main/java/info/plateaukao/einkbro/data/remote/OpenAiRepository.kt`
- `app/src/main/java/info/plateaukao/einkbro/data/remote/TranslateRepository.kt`
- `app/src/main/java/info/plateaukao/einkbro/database/RecordRepository.kt`
- `app/src/main/java/info/plateaukao/einkbro/search/suggestion/GoogleSuggestionsRepository.kt`
- `app/src/main/java/info/plateaukao/einkbro/search/suggestion/OpenSearchSuggestionsRepository.kt`
- `app/src/main/java/info/plateaukao/einkbro/search/suggestion/SearchSuggestionsRepository.kt`

Existing Kotlin tests:
- `ad-filter/src/test/java/io/github/edsuns/adfilter/BinaryDataStoreTest.kt`
- `ad-filter/src/test/java/io/github/edsuns/adfilter/script/NativeExtendedCssTest.kt`
- `adblock-client/src/test/java/io/github/edsuns/adblockclient/RegexFilterSetTest.kt`
- `app/src/androidTest/java/info/plateaukao/einkbro/searchhh/AiVaultTest.kt`
- `app/src/androidTest/java/info/plateaukao/einkbro/searchhh/CodebaseUiTest.kt`
- `app/src/androidTest/java/info/plateaukao/einkbro/searchhh/InternalBackendTest.kt`
- `app/src/androidTest/java/info/plateaukao/einkbro/searchhh/PersistenceTest.kt`
- `app/src/androidTest/java/info/plateaukao/einkbro/searchhh/RelayProbeTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/browser/BrowserActionCatalogTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/browser/ImageRequestClassifierTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/browser/UserScriptBridgeTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/caption/YouTubeCaptionFetcherTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/data/remote/ChatRequestSerializationTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/data/remote/ThinkTagFilterTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/epub/EpubCoverParserTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/epub/EpubParserTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/epub/EpubTestFixtures.kt`
- `app/src/test/java/info/plateaukao/einkbro/epub/EpubXMLFileParserTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/AiConfigTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/BrowserAndTranslationConfigTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/DisplayConfigTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/DomainConfigManagerTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/FakeSharedPreferences.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/PreferenceDelegatesTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/SerializableDataTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/TabConfigTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/TouchConfigTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/TtsConfigTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/preference/UiConfigTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/searchhh/ConnectionSettingsTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/searchhh/CrawlerExpansionTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/searchhh/LocalPolicyTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/searchhh/OpportunityPipelineTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/service/OpenAiRepositoryTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/task/ToolTextWindowTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/BackupUnitJsonTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/DownloadHelperTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/FaviconCandidatesTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/GithubUtilTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/HelperUnitTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/MarkdownBlocksTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/MarkdownParserTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/unit/pdf/PdfTocEditorTest.kt`
- `app/src/test/java/info/plateaukao/einkbro/view/GestureTypeTest.kt`

## Unfinished code and interaction risks

Initial lexical candidates (not all confirmed bugs): 6 unfinished markers, 9 empty `onClick` handlers, 50 empty/comment-only catches, 404 null/false/empty-return sites, 177 broad catches, 48 JS-interface references. Lexical matching can include comments and legitimate guards; context must be reviewed, never blindly removed.

Confirmed unfinished production symbols:
- `epub/EpubXMLFileParser.kt:parseAsDocument` retains an unfinished alternate structured-text implementation.
- `viewmodel/TtsViewModel.kt:pauseOrResume` returns without pausing/resuming system TTS. This is a real dead-action candidate, not proof that every calling control is reachable.
- `ad-filter/.../Detector.kt` has an outstanding detection implementation marker.
- Empty click candidates reviewed are in preview functions; they are not evidence of nine dead live buttons. They still need explicit contextual handling under the requested strict rule.
- Silent failures include `PublicSearch.kt:69`, `NinjaWebViewClient.kt:507,539`, `BookmarkDao.kt:391`, and injected assets. Broad catches require cancellation audit; returning empty/null is often legitimate, so flags alone cannot establish stubs.
- Searchhh has no central test-tag registry; catalogue test checks filtering after interaction, but existence assertions do not prove opening a source, persistence or full runtime behavior.
- Disabled Start depends on server activity, identity, query length, selected sources and busy/running state; visible explanation of each disabled cause is incomplete.
- No complete action/state/navigation/test mapping, coverage gate or visual-regression baseline exists.

## WebView/security audit

- `WebViewSslHandler.onReceivedSslError` calls `handler.proceed()` after confirmation **and automatically when the certificate dialog preference is off**. Must cancel invalid TLS; high priority.
- Manifest and `res/xml/network_security_config.xml` permit cleartext globally. No explicit per-host approval enforced at network-config level.
- `EBWebView.kt:440-443,683` attaches native interfaces; JS/UserScript/Chat/StartPage bridges expose many methods. Some implement privileged networking; origin and frame authorization need a dedicated review. This audit does not certify them safe.
- AndroidX WebKit is already a dependency; do not add Chromium sources or duplicate WebView stacks.
- Shared WebView helpers/delegates exist; reuse and restoration require lifecycle instrumentation. Browser controls/unsupported schemes/history/loading/error/SSL behavior lack comprehensive fixture-backed UI tests.
- Signing material is external to the repository; embedded Drive installed-app OAuth client ID is public, not a client secret. This is not a full secret-history audit. AI credentials and paired MCP credentials must never appear in artifacts or screenshots.

## Existing build/CI limitations

`.github/workflows/buid-app-workflow.yaml` builds/tests/publishes a prerelease through five jobs. Previous run 36518594817 passed those existing checks; that does not cover this task. No Detekt/ktlint/unfinished/master contract gate exists at baseline.

App lint uses a baseline, disables MissingTranslation and skips release lint. Baseline issue counts: {"AppBundleLocaleChanges": 1, "AppLinkUrlError": 3, "AutoboxingStateCreation": 5, "AutoboxingStateValueProperty": 8, "ChromeOsAbiSupport": 1, "ClickableViewAccessibility": 2, "ComposableNaming": 4, "DataExtractionRules": 1, "DiscouragedApi": 2, "InlinedApi": 4, "InsecureBaseConfiguration": 1, "IntentFilterUniqueDataAttributes": 2, "LocaleFolder": 1, "LogNotTimber": 112, "ModifierParameter": 2, "NewApi": 3, "OldTargetApi": 1, "PictureInPictureIssue": 1, "PluralsCandidate": 2, "Recycle": 1, "RtlHardcoded": 3, "SdCardPath": 2, "SetJavaScriptEnabled": 1, "TrimLambda": 5, "Typos": 3, "UnusedMaterialScaffoldPaddingParameter": 2, "UnusedResources": 3, "UseKtx": 73, "VectorPath": 3, "ViewConstructor": 2, "WrongThread": 1}. These must remain visible rather than being silently treated as clean. No global compiler warning-as-error configuration found.

Local Java and adb are unavailable. Exact attempted commands are recorded separately in `BASELINE_VERIFICATION.md`. Main branch protection API returned HTTP 403; repository administrator permissions are unavailable. Files/CI can demand acknowledgment and block configured workflows; they cannot force readers/forks to agree or make themselves required merge checks without administrator configuration.

## Performance/visual risk

Searchhh parses source assets synchronously in composition initialization, polls service state every 1.5 seconds and job status every 5 seconds, and retains a rolling result window. Stable lazy keys must be checked per collection. No measured startup/recomposition/memory/scroll baseline or Macrobenchmark exists. Do not invent performance gains or add dependencies simply to increase counts. Searchhh is dark-only; requested home-light/theme/drawer screenshots cannot be fabricated from nonexistent UI. Existing previews do not replace exercised-device screenshots.

## Implementation order

1. Persist this audit and execute/save baseline, before changing production behavior.
2. Add tested fail-closed source/contract tooling, contributor agreement, CI/static/build/master gates; leave debt failing rather than auto-excluding it.
3. Establish exhaustive lexical control inventory plus explicit missing-feature requirements; connect stable tags and typed routes in small tested increments.
4. Fix TLS bypass with focused handler tests. Review cleartext/native bridge changes separately to avoid silently breaking browser functions.
5. Execute new gates; distinguish infrastructure failures from legacy/code failures. Add full behavior/screenshot/accessibility/performance evidence incrementally.
6. Require administrator activation of protected required checks; do not claim enforcement before it is enabled.

## Release delivery correction — pre-change audit, 2026-09-29

User explicitly rejects distributing debug builds; repository tests must remain separate from the shipped Searchhh APK. Inspected `app/build.gradle.kts`, main manifest, dependency scopes, `EBWebView.initWebView`, `StartSettings`, BrowserConfig, both workflows and signing references. Findings:
- Publication currently renames a debug artifact; it never builds the ordinary release variant. Existing Play signing applies only to a different `.g` application ID, not Searchhh's main ID.
- Release already uses R8/resource shrinking but lacks an explicit production signing path. Keep identity `app.searchhh.browser`; do not use debug signing or invent an ephemeral release key.
- Tests and Compose runtime tooling are already test/debug scoped; preview annotations are not a test runner. Quality scripts/reports are repository files, not Android assets.
- `EBWebView.initWebView` permits `BuildConfig.DEBUG || savedPreference`, allowing release WebView inspection. Remove the preference override and its now-redundant settings control; preserve the stored key for backward data compatibility only.
- `gh secret list` returns HTTP 403 (integration scope), not proof that secrets are absent. An administrator must configure/confirm release signing outside chat. No private signing material will be requested in chat or committed.
- Baseline `./gradlew --continue searchhhVerification -PuniversalApk`: exit 1, Java/JAVA_HOME absent. Last verified application build passes tests but required static/evidence gates fail.

Plan: keep all quality gates; replace debug publication with separate signed-release build, binary manifest/certificate checks, release-device smoke, then release-only publication. Add negative validator tests. No assertion of an available release APK before those jobs succeed.

Release-lane follow-up: hosted run 36582486441 exposed an error in the new runtime guard before release tests: resolving all artifacts without selecting a type is ambiguous for Android library variants (resources/JAR/navigation/etc.). Correct the guard to traverse the selected **component dependency graph**, not download ambiguous artifact variants. Keep unresolved dependencies fatal, record graph identities as task inputs, and retain the complete forbidden dependency check.

## R8 missing-type continuation — pre-change audit, 2026-09-29

Baseline source `b709d86`; hosted run 36585651804, release check 109465110694: exactly five missing types (three Error Prone annotations used by Tink, two desktop Java management classes referenced by Ktor's IDE detector). 327 release unit tests passed; release lint is back to 470 errors after unused developer-label removal. Local master attempt again exits 1 before Gradle because Java/JAVA_HOME is absent; 33 Python checker tests pass. Recovered the session branch Git index/head from its fetched remote with a mixed reset, preserving working-tree files; restored tree was clean.

Inspected build/dependency definitions, existing R8 rules, Ktor/MCP device tests, encrypted-preference tests, quality rules and release workflows. Upstream source inspected through GitHub contents APIs:
- `ktorio/ktor`, ref `3.0.2`, `ktor-utils/jvm/src/io/ktor/util/debug/IntellijIdeaDebugDetectorJvm.kt`: ManagementFactory/RuntimeMXBean use is inside try/catch(Throwable); absence returns false. The `3.1.3` implementation is identical, so a dependency upgrade would not resolve this.
- `ktorio/ktor`, ref `3.0.2`, `ktor-utils/jvm/resources/META-INF/proguard/ktor.pro`: existing upstream rules preserve AtomicFU fields/client engine loading but do not describe Android's absent management types.
- `tink-crypto/tink-java`, ref `v1.8.0`, `maven/tink-java-android.pom.xml` and `tink_java_deps.bzl`: static-analysis annotation dependency is used to compile Tink but absent from its Android Maven dependency list. Error Prone v2.18.0 source includes RestrictedApi and the required annotation APIs.

Plan: supply Error Prone annotations as compileOnly (not implementation); keep encryption storage/library semantics unchanged; add only two exact Android compatibility R8 rules, with rationale, owner/review/removal conditions and an Android test exercising the real upstream fallback. Keep all other R8 errors fatal and keep shrinking enabled. The existing real Ktor/MCP device test remains required. These changes do not certify a signed/minified release on a device; production signing and release smoke still require their own successful execution.

## First release-lint remediation audit (before changes)

`ComposeDialogFragment.disableMoveAnimation` reflects `WindowManager.LayoutParams.privateFlags`, ORs a hidden flag, and silently catches Throwable. This is the first reported `DiscouragedPrivateApi` failure. Inspection found that the existing API-34 `windowNoMoveAnimation` theme attribute is applied to `EinkDialogTheme`, but actual Compose dialogs select **EinkPanelDialogTheme**, which does not inherit it. Therefore merely removing reflection would lose the intended public replacement even on newer devices.

Plan: factor the existing panel style into a base plus unchanged public style alias; give that same actual panel style an API-34 resource override with `android:windowNoMoveAnimation=true`; remove reflection/catch/hidden constant, retain public `windowAnimations=0`, positioning, colors and IDs. On API 24–33, platform window-movement behavior is accepted rather than accessing a hidden field; no exact old-device animation equivalence is claimed. Add an Android test opening the actual ThemeColorDialogFragment, checking the effective panel theme/window settings, clicking its existing localized OK action and observing dismissal. This is behavior/theme evidence, not a screenshot or e-ink performance measurement.

Diagnostic follow-up: existing annotations expose compiler/R8 failures and lint totals, but not the lint issue groups/locations needed for the next fix. Add bounded summaries from existing lint XML reports (full grouped summary retained in CI JSON); keep malformed report errors visible and all original checks failing. This changes reporting, not rule severity or publication conditions.

## Sequential remediation — DrawAllocation (pre-change audit)

User requires issues to be resolved sequentially before the next build. Work is limited to the two DrawAllocation findings in `ThemedEdgeBorderView` plus the verification needed to close them. Latest baseline: `5896283`, run 36596759905, lint reports 2 DrawAllocation errors at this view. The view is actually installed as `R.id.content_separator` in `MainActivityLayout`; it is not an unused preview.

Inspected the renderer, theme state/colors, border enum, creation route, CI/release gates, device harness and checker tests. STAMP allocates a RectF for each scallop plus a boxed center list every frame; DASHED allocates a DashPathEffect/interval array every frame. Geometry, mirroring, density scaling, colors and transparency must remain unchanged. Other color/theme helper allocations are outside this bounded issue; no whole-app allocation-free or frame-time claim is intended.

Plan: use the API-21+ scalar Path.arcTo overload (min SDK is 24), iterate center coordinates without a list, cache dash effects by the live density, and add Android bitmap comparisons against a frozen pre-fix renderer across border styles, sizes, directions, density and inversion. Separately test cache reuse/recreation. Reference bitmap allocations occur only in test fixture generation, not in a View.onDraw callback. Save representative reference/current images as test artifacts, explicitly not full-screen before/after app screenshots.

Process correction: verification will still compile private test APKs needed by connected tests, but the release lane will check R8 without assembling an unsigned release APK while other gates fail. Actual release assembly remains in the existing quality-gated, production-signed release job; publication gates are not weakened. Remove general debug APK downloads from the verification artifact list. Do not start the next issue until this issue's relevant lint and Android regressions pass.

Local baseline: master Gradle command exits 1 (Java/JAVA_HOME unavailable); 35 Python checker tests pass. Session branch Git metadata restored from its fetched remote with a mixed reset, preserving the clean working files.


## Sequential issue 2 — SAF stream ownership (pre-change audit)

Border allocation issue closed using retrieved 92484f6 device/lint/PNG evidence before this issue began. Baseline local checker suite: 41 passing; inventory 617/9; master remains blocked by missing Java/JAVA_HOME. Hosted app lint still includes one Recycle at SupernoteStorage.openOutputStream:98.

Inspected SupernoteStorage, DownloadHelper's only caller, SAF picker registration/result lifecycle in FileHandlingDelegate, existing test dependencies and CI reporting. The stream is returned in a Pair; its sole caller already uses `.use` before showing completion. Therefore this is an ownership-design/lint finding, not evidence that this caller currently leaks. Data URLs, blob downloads and direct Supernote downloads all funnel through writeBytesViaSupernote. No controls/routes need adding.

Plan: replace the stream-returning API with a scoped write callback, keep `.use` inside the method that opens the stream, and return only the destination Uri after write AND close complete. Preserve find/delete/create order and blank-MIME fallback. Null provider/tree/file outcomes remain explicit failure; propagate I/O failures to DownloadHelper's existing error toast/log rather than silently claiming success. Unit tests will execute real tracking OutputStreams, mocking only Android/SAF boundaries with existing MockK: success/bytes, missing tree/file/stream, open/write/close failures and suppressed close failures. Real Supernote/OEM picker integration is outside this bounded ownership proof. Keep Recycle fatal; no suppression or dependency addition.

## Sequential issue 3 — platform EXIF (pre-change audit)

Baseline c63e25a, run 36612274354, host check 109556416947: ExifInterface findings remain (8 in app lint). Local checker suite 41 passes, inventory 617/9; full master exits 1 before execution because Java/JAVA_HOME is absent. Border and SAF ownership issues are already closed; no next unrelated issue is being implemented.

Inspected BookmarkRenderer.saveStartPageBackground and BrowserActivity.onResume's picked-background save→success reload/error toast path, EinkImageProcessor.processBytes and EinkImageInterceptor's DEEP-mode decode/process/cache response path, preview caller, catalogue/build settings, test dependencies, device harness and diagnostics. Both EXIF readers are platform android.media.ExifInterface. No direct AndroidX EXIF dependency or existing EXIF tests found. Backgrounds currently apply only 90/180/270 rotations; mirrored EXIF values are ignored. Deep processing handles all eight orientation values. Preserve those semantics in this migration; mirror-background support, bitmap recycling, exact downsample caps and existing exception policy remain separate work. No new screen/control/route is needed.

Plan: use maintained AndroidX ExifInterface at both call sites with explicit pinned dependency 1.4.2. Official release notes https://developer.android.com/jetpack/androidx/releases/exifinterface list 1.4.2 (2025-12-03), including JPEG marker parsing fixes. No parser implementation from scratch, no lint suppression. Add Android tests invoking the actual save/process methods on generated asymmetric JPEG/PNG fixtures, verifying independent pixel-coordinate orientation references, persisted background bytes, transparency, missing metadata and invalid/pass-through behavior. Export representative expected/current bitmap pairs through the existing bounded diagnostic mechanism; these are image-pipeline regressions, not full-screen UI screenshots or performance evidence. Require real Android execution and ExifInterface lint count zero before closure.

## Next-agent work-order audit — 2026-09-29

User requested repository instructions and a GitHub push for sequential resolution of all 400+ findings. This increment is documentation/triage only, not a production fix. Reviewed AGENTS, QUALITY_RULES, latest implementation and sequential reports, source `6abf29b` structured evidence, build verification tasks, both CI workflows, interaction scenarios/evidence checker, scanner/Detekt settings, current EXIF background path, resource API use, TTS call sites and crawler/AI architecture documents.

Latest saved baseline: 458 app lint errors/14 hints, Detekt 2788; app lint annotations expose six groups and omit 28. Those summaries cannot supply 458 exact occurrence tickets; module/variant reports overlap. A new full host-artifact download from run 36628444279 failed with storage EOF. The work order must make exhaustive occurrence reconciliation the first prerequisite, distinguish verified seed groups from unknown remainder, and forbid closure by aggregate count reduction alone. Preserve existing successful border/SAF/EXIF regressions and release security gates. No dependency or runtime/UI change is planned in this increment.

Checkout metadata was at initial commit while work files already matched published `77fbeef`. Fetched the same session branch, verified ancestry, and aligned HEAD/index with a mixed reset; no working file was overwritten. Status was clean afterward. Future agents must inspect divergent/uncommitted work, not blindly repeat this recovery.

## S00 report recovery audit — 2026-09-29

Normal artifact download for delivery run 36628444279 again fails with storage EOF; an alternate authenticated client fails with URLError (credentials remained in memory, never saved). Local Java is absent. Current checkout was again initial-commit metadata with published work files; after fetching, ancestry-checked mixed alignment to 030984f left a clean tree and did not overwrite files.

Inspected current CI summary: it intentionally returns six lint groups and bounded diagnostics, not full occurrences. Increasing that preview cannot establish exhaustive inventory. Plan one bounded infrastructure ticket S00-transport: add a read-only recovery workflow for existing, pinned-source artifacts; transport only static reports/logs and source inventory snapshots using bounded gzip/base64 pages with source/run/artifact identity, per-file and full-payload hashes. Decoder must reject missing/duplicate/truncated/tampered pages, wrong source, unsafe paths and size overflow. Keep existing quality/release workflows, thresholds, exits and tests untouched. Recover original source 6abf29b; separate recovery-tool source from report source. Runtime application/build sources have not changed in documentation checkpoints; do not mislabel historical reports as a new full verification.

## S00 recovered-data normalization audit — after executed transport closure

Transport source 0778e68, recovery run 36635551405: all ten jobs succeed; all five groups reconstruct with complete page/source/file checksums. Original report source remains 6abf29b. Host/release/source/Detekt originate from delivery36628444279; formatting from standalone36628443911 at the identical SHA. The unsuccessful delivery formatter failed before execution because Detekt plugin resolution failed. No existing app/build sources differ between 6abf29b and this recovery source.

Actual formats inspected: Android lint issue XML (including secondary locations), Detekt checkstyle XML plus independent SARIF, eleven ANSI-colored ktlint text reports with per-rule summary footers, unfinished JSON and exact-source interaction/scenario snapshots. Next bounded S00 substep is a deterministic evidence export, not production remediation: parse every occurrence, reconcile ktlint footer counts, cross-check Detekt XML/SARIF counts, preserve raw report/variant provenance and secondary locations, and reject unrecognized nonempty report lines. Do not invent stable root-cause tickets or silently deduplicate overlapping reports. Save a compact recovered occurrence dataset and manifest; keep S00 open until semantic ownership/deduplication is complete.

## Development factory audit — 2026-09-30

User explicitly requests at least50 workload-reducing tools, incorporated rather than catalogued. This authorizes one bounded factory-infrastructure task ahead of remaining S00 semantic ownership; it does not close S00 or permit concurrent production fixes. Reviewed existing policy, Gradle catalogue/build tasks, recovered17,890raw observations, backend requirements/Docker Compose, JS/Jest project, scripts, workflows and artifact/scanner exclusions. Native runtime and release policy must stay unchanged.

Plan:58 named upstream tools with concrete scoped commands, official/project source links and exact new-tool package pins; reuse existing Gradle/Android/Git tooling. Implement only minimal orchestration: isolated installation/locks, manifest validation, prerequisite doctor, read-only bounded execution/evidence, and file/rule work-packet suggestions from the recovered inventory. No auto-commit, source rewrite, signature generation or publication. Source mutation, missing prerequisites, timeout/truncated diagnostics and actual failures cannot produce a clean result. Property/negative tests exercise the orchestration; existing checks remain enabled. New artifacts/caches/environments stay under ignored build output; no dev dependency goes into Android runtime.

Researched official uv tool-isolation docs, ast-grep scan/rule documentation and zizmor offline behavior. Retrieved PyPI/npm metadata for actual versions/engines and wrapper-source provenance; no unversioned curl-to-shell installers. Binary wrapper packages are explicitly distinguished from upstream engines. Several source-analysis tools overlap intentionally in scope (types, security, complexity, mutation); task routing avoids running all of them for every edit. No claim of 58 installed/passing tools until actual evidence supports it.

## Factory implementation audit closure — 2026-09-30

The user-requested factory is implemented as explicit development-only argv
jobs, not runtime dependencies or a catalogue-only deliverable.58 engines are
registered;50 executed at25f9401/run36662473189. Failures stay visible. Reviewed
real setup errors led to corrected package extras, argument ordering, archive
contracts and workspace resolution; none was disguised as a passing scan.

Source-change detection, process-group cleanup, bounded separate stdout/stderr,
credential-filtered environments and fresh artifact checks have real negative
regressions. They are not an OS sandbox. Dependency hashes/metadata are not a
full upstream security audit. No unsafe source auto-fix or release command was
added. npm lifecycle scripts are disabled; Python isolation/locks and explicit
installation remain mandatory. Tool/cache availability can disappear on restore.

Triage preserves17890 raw aliases in3509 suggested packets without asserting
root-cause ownership. Security/license/style findings remain unresolved; two
redacted Gitleaks candidates require review before confirming production order.
No UI/runtime/crawler/AI feature completion or new APK approval is claimed.
See docs/factory/VERIFICATION.md and FACTORY-001 for scope and exact evidence.

## FACTORY-002 pre-edit audit — 2026-09-30

Reviewed runner, installer, safety/schema, locked packages, workflow, browser
assets and FastAPI/store initialization. Existing checks lack real-browser
accessibility, OpenAPI schema validation, installed dependency conflict checks,
TOML validation and npm dependency vulnerability auditing. Manual selection and
reading raw reports remain inefficient. Preserve all58 engines and old evidence;
add six engines plus scoped orchestration/map/triage, not overlapping aliases.
See factory/EXPANSION_PLAN.md. Product/UI sources and canonical gates stay intact.

## FACTORY-002 audit outcome

Six additional engines validated in real hosted execution, not help probes or
aliases. All64 source repositories and responsibilities are visible; nine
candidates stay separately labeled unimplemented. Automatic report processing
uses paths, size limits and hashes; it omits snippets/secret values and retains
unparsed/malformed cases. Stylelint's actual stderr output needed a dedicated
adapter; failed initial parsing was corrected, not hidden. Browser checks own
their inputs and block external requests; they do not mock a working native
bridge. Hosted retry-state tests do not certify native navigation dispatch.

Read-only post-execution source guards are not OS sandboxing. Third-party trust,
CVE relevance, native fixtures, screenshot approval and security decisions remain
human/agent work.70–80% effort savings are not established; baseline must include
existing FACTORY-001 automation. No original app or release gate is waived.

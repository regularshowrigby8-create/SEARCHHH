# Precisely scoped compatibility annotations

Owner: Searchhh maintainers. Review by 2026-12-31 and on any min-SDK/storage migration. These are not exemptions from the unfinished scanner or permission to ship failing tests. No file-wide or global compiler suppression is added.

| Symbol | Annotation | Necessary compatibility reason | Removal condition | Regression evidence |
|---|---|---|---|---|
| `ConnectionSettings.prefs` | Existing `DEPRECATION` annotation, retained | Existing credentials/identity are in AndroidX EncryptedSharedPreferences with the existing Android Keystore alias. Replacing it with plaintext to remove a warning would weaken security and risk losing installations. Deprecated class references are confined to this adapter, not imports. | A separately audited, encrypted, transactional migration that preserves credentials and supports API 24+. | Existing `PersistenceTest` identity isolation plus new `encryptedPreferencesReopenWithoutPlaintextCredentials`; execution status in main report. |
| `ImageCacheTrimPolicy.action` | `DEPRECATION` | RUNNING_LOW/CRITICAL are still emitted on supported API 24–33. Modern background/UI-hidden behavior and legacy thresholds must both remain intact. The annotation covers only this decision helper, using named Android constants. | Drop pre-34 device support or replace with equivalently tested platform lifecycle policy. | `ImageCacheTrimPolicyTest`; actual Android cache eviction/disk-rehydration tests in `ImageCacheBehaviorTest`; execution status in main report. |

The window APIs, locale constructors, notification overload, robots parser overload and repeated serializers are migrated rather than suppressed. This table does not retroactively approve other inherited annotations in the repository.

## R8 Android/desktop compatibility — Ktor 3.0.2

Owner: Searchhh maintainers. Review by **2026-12-31**, whenever Ktor changes, or when the supported Android SDK range changes.

Only `java.lang.management.ManagementFactory` and `java.lang.management.RuntimeMXBean` receive exact `-dontwarn` rules. These are desktop JDK types absent from Android, not missing Android runtime dependencies that should be bundled. Upstream [IntellijIdeaDebugDetectorJvm.kt at 3.0.2](https://github.com/ktorio/ktor/blob/3.0.2/ktor-utils/jvm/src/io/ktor/util/debug/IntellijIdeaDebugDetectorJvm.kt) calls them inside a `try`/`catch (Throwable)` that returns **false** on Android. The 3.1.3 source is identical; upgrading only to hide the warning is not a fix. No wildcard rule, `-ignorewarnings`, disabled shrinker, fake platform class or assumed return value is added.

Regression: `KtorAndroidCompatibilityTest.absentDesktopManagementApisLeaveKtorDebuggerDetectionDisabled` asserts that both types are absent on the actual Android device and invokes the actual upstream singleton getter, asserting false. Existing `InternalBackendTest.initializesFromNameAndServesAuthenticatedMcp` must continue passing real authenticated HTTP/SSE/MCP requests. Execution status is recorded in the implementation report; adding a test is not a passing result. Signed/minified release smoke remains separately mandatory.

Remove/revisit these rules if upstream removes the desktop references, provides an Android implementation, Android supplies those types, or any new caller makes those APIs a runtime requirement. The rules are exact **types**, not caller-scoped; future uses outside this reviewed optional detector require fresh audit. The new Android test deliberately fails rather than skips when the platform/dependency assumption changes.

The three Tink Error Prone annotation types are **not suppressed**: `error_prone_annotations:2.18.0` is compile-only. The release runtime dependency guard rejects that group if it accidentally becomes a packaged dependency. Encryption format, keys and AndroidX Security version remain unchanged.

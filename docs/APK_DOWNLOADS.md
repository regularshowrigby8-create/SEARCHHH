# Searchhh APK downloads

**No approved signed Searchhh release APK is available yet.** Internal debug/test APKs are not user deliverables.

## Latest verified progress

Three bounded issues are now closed sequentially: border drawing allocations, SAF output-stream ownership, and AndroidX EXIF migration. Final EXIF source **`6abf29b`** passed all four new Android regressions, including 16 orientation comparisons. Debug/release **ExifInterface findings decreased 8 → 0**, and app lint decreased **466 → 458 errors**.

[Standalone verification 36628443911](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36628443911) reports **384 combined device-lane / 336 release** passing results. The standalone host failed before reporting any tests; independent [same-source delivery verification 36628444279](https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36628444279) reports **360 host** passing results. That delivery's device attempt also reported zero tests and failed. These attempts are not counted as passes; full results and limitations are in `quality/verification-6abf29b.json`. Counts overlap and are not additive.

**Both workflows still FAIL overall.** Required lint/format/Detekt/unfinished/interaction evidence remain unresolved. Release build, signed smoke and publication are skipped. Ordinary host/release checks do not assemble APKs; private emulator-test APKs remain internal. No debug or unsigned substitute is offered.

The intended release remains **Searchhh 0.4.1**, version code **5**, package **`app.searchhh.browser`**, minimum SDK **24**. Approved delivery must contain `Searchhh-0.4.1.apk` and `SHA256SUMS` after production signing, signature/metadata validation and signed-release device smoke.

## What must happen before a link is available

1. Repository quality, backend integration and device checks pass.
2. The ordinary `release` variant builds with R8/resource shrinking, production signing and `debuggable=false`. The original app ID stays `app.searchhh.browser`; next version is `0.4.1` / code `5`.
3. The resolved release runtime rejects JUnit/MockK/Mockito, AndroidX test runners and Compose runtime debugging/test tooling. Repository scripts, test APKs, reports and screenshots are not release assets. Compose preview annotations are not a test runner.
4. SDK tools verify the actual APK signature and manifest. The certificate must match the pinned production fingerprint; debug signing, instrumentation, shell profiling and test-only/debuggable manifests are rejected.
5. The exact release APK is installed and launched on Android 35, with process/UI/crash checks and a check that debug `run-as` is unavailable. Only then may CI publish `Searchhh-0.4.1.apk` and `SHA256SUMS`.

A multi-architecture APK can still be a normal release APK: architecture coverage and debug/release build type are independent. Tests remain in the repository and separate CI artifacts, not in the delivered application.

## Secure signing setup (repository administrator)

The current GitHub integration returned HTTP 403 when listing Actions secrets. This does **not** establish whether signing secrets already exist. Confirm/configure these through GitHub **Settings → Secrets and variables → Actions**, never in chat or tracked files:

Secrets:
- `SEARCHHH_RELEASE_KEYSTORE_BASE64`: base64 encoding of the privately backed-up production keystore.
- `SEARCHHH_RELEASE_STORE_PASSWORD`
- `SEARCHHH_RELEASE_KEY_ALIAS`
- `SEARCHHH_RELEASE_KEY_PASSWORD`

Repository variable:
- `SEARCHHH_RELEASE_CERT_SHA256`: public SHA-256 fingerprint of the production signing certificate, as 64 hexadecimal characters (colons accepted).

Use the same backed-up production signing key for subsequent updates. Do not generate a disposable key on every CI run, reuse an Android debug certificate, or use an unrelated upstream Play key. CI decodes the key into runner-temporary storage, disables signing-job Gradle caches and removes the temporary keystore. It never uploads the private key.

Moving from an older debug-signed installation to a different production certificate may require uninstalling the old app. Back up needed data first; no seamless debug-to-production upgrade is promised.

## Current blockers

Required lint/format/Detekt/unfinished/interaction-evidence checks remain failing. Release signing configuration cannot be confirmed with the current GitHub integration scope. The original R8 blocker was resolved, but production signature validation and on-device signed-release smoke have not run successfully, so no approved release download link exists yet.

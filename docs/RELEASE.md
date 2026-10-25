# Releasing an APK

## What CI produces

Every push to any branch runs `.github/workflows/ci.yml` and uploads
`searchhh-debug-apk` (installable, debug-signed). Pushing a tag `v1.0.0` runs
`.github/workflows/release.yml`, which:

1. builds the debug APK,
2. builds a **signed** release APK *if* signing secrets exist,
3. attaches both to a GitHub Release with generated notes.

Download the artifact from the Actions run, or from the Release page.

## One-time signing setup

```bash
keytool -genkeypair -v \
  -keystore searchhh-release.jks \
  -alias searchhh \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass '<password>' -keypass '<password>' \
  -dname "CN=SEARCHHH, OU=Engineering, O=SEARCHHH, L=Bloemfontein, C=ZA"

base64 -w0 searchhh-release.jks > keystore.b64
```

Add four repository secrets: `KEYSTORE_BASE64` (contents of `keystore.b64`),
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

For local signed builds:

```bash
cp android/keystore.properties.example android/keystore.properties
# edit paths + passwords
cd android && ./gradlew :app:assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

Without `keystore.properties`, `assembleRelease` produces an **unsigned** APK -
fine for inspection, not for installing.

## Versioning

Single source of truth: `android/app/build.gradle.kts`

```kotlin
versionCode = 1          // bump on every public build
versionName = "1.0.0"    // semantic version, shown in Settings
```

Tag format: `v1.0.0` (must match `versionName`). The tag triggers the release
workflow.

## Pre-release checklist

- [ ] `make test` and `make cov` pass locally
- [ ] `make lint` and `make stack` are clean
- [ ] `cd android && ./gradlew :app:testDebugUnitTest`
- [ ] Version bumped, tag pushed
- [ ] CI green on the release commit
- [ ] Release notes mention any schema or API changes
- [ ] `docs/HACKATHON.md` checklist updated

## Distribution notes

* Debug APKs use `applicationIdSuffix = ".debug"`, so they install alongside a
  release build - useful for side-by-side comparison on one device.
* Sideloading requires "Install unknown apps" permission on Android 8+.
* The app needs Android 8.0 (API 26) or newer.
* First launch with no backend reachable still works: the catalogue renders from
  the cached/seed path and Settings shows the exact failure reason.

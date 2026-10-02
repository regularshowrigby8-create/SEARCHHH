# Hosted verification provisioning

The local sandbox has no Java or Android SDK. The quality gates are not bypassed;
GitHub-hosted runners are used as the legitimate environment boundary and now
provision the required SDK packages before running checks.

## Reviewed GitHub repositories

- [android-actions/setup-android](https://github.com/android-actions/setup-android)
  installs command-line tools, accepts licenses, exposes `sdkmanager` and can
  install requested packages. The workflow pins commit
  `035682743ab223ec9c6169af09811dff8247e520` rather than using a floating tag.
- [ReactiveCircus/android-emulator-runner](https://github.com/ReactiveCircus/android-emulator-runner)
  runs the existing Android 35 device lane. It remains a device test runner,
  not evidence that local compilation passed.
- [gradle/actions](https://github.com/gradle/actions) provides Gradle setup and
  caching for the hosted build.
- [actions/setup-java](https://github.com/actions/setup-java) supplies the
  Java 17 runtime used by Gradle.

## Provisioned packages

The Android host lanes now request:

```text
platform-tools
platforms;android-36
build-tools;35.0.0
```

The workflow still verifies `android.jar`, `adb` and the resulting toolchain
before invoking Gradle. A failed provisioning step remains a failure. No
`continue-on-error`, skipped gate, debug-to-release substitution or fake SDK
artifact was added.

## Why this is not a gate bypass

A local missing prerequisite is different from a failing quality result. Hosted
CI supplies the documented build environment, while the required quality jobs,
device test and final aggregation remain mandatory. The release workflow still
requires successful upstream checks, device smoke tests, private signing
secrets and APK signature verification.

The local importer remains available at
`tools/quality/import_android_sdk.py` for a real SDK archive or directory. SDK
binaries are never committed to the repository.

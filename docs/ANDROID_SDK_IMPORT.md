# Android SDK environment import

`tools/quality/import_android_sdk.py` is the factory's fail-closed environment
preparation step. It does not download binaries, add an SDK to Git, accept
credentials, or mark a release approved.

It searches `ANDROID_SDK_ROOT`, `ANDROID_HOME`, standard `/opt` locations and
`~/android-sdk`. A supplied stripped SDK directory or archive can be imported:

```sh
python3 tools/quality/import_android_sdk.py --source /path/to/sdk.tar.gz
```

The importer validates all of the following:

- Java is available through `JAVA_HOME` or `PATH`.
- `platforms/android-36/android.jar` exists.
- At least one Build Tools `aapt2` exists.
- `platform-tools/adb` exists.
- A command-line-tools `sdkmanager` exists.

The report is written to `build/reports/android-sdk.json`, and a missing or
partial environment exits with code 2. The current sandbox has no Java, `adb`,
`sdkmanager`, Android SDK directory or cached SDK archive, so Android compilation
remains **BLOCKED**, rather than being reported as passed. Network download also
failed at the Google repository TLS connection, so no guessed or unverified SDK
was imported.

#!/usr/bin/env bash
# Run inside android-emulator-runner, before its emulator is torn down.
set -euo pipefail
mkdir -p build/reports/searchhh
capture_crashes() {
    local status=$?
    trap - EXIT
    # A completed pixel regression writes only synthetic border images, never user content.
    # Collect them even when another mandatory gate fails; preserve the failing exit.
    if adb shell run-as app.searchhh.browser test -f files/edge-border-evidence/complete.txt; then
        mkdir -p build/reports/searchhh/edge-border
        for file in complete.txt reference-stamp.png current-stamp.png reference-dashed.png current-dashed.png; do
            adb exec-out run-as app.searchhh.browser cat "files/edge-border-evidence/$file" \
                > "build/reports/searchhh/edge-border/$file" || status=1
            test -s "build/reports/searchhh/edge-border/$file" || status=1
        done
    fi
    if adb shell run-as app.searchhh.browser test -f files/exif-evidence/complete.txt; then
        mkdir -p build/reports/searchhh/exif
        for file in complete.txt reference-background.png current-background.png reference-web.png current-web.png; do
            adb exec-out run-as app.searchhh.browser cat "files/exif-evidence/$file" \
                > "build/reports/searchhh/exif/$file" || status=1
            test -s "build/reports/searchhh/exif/$file" || status=1
        done
    fi
    adb logcat -d -b crash > build/reports/searchhh/crash.log || status=1
    if test -s build/reports/searchhh/crash.log; then
        echo '::error::Android crash buffer is not empty'
        status=1
    fi
    printf 'verification_exit=%s\n' "$status" > build/reports/searchhh/device-exit.txt
    exit "$status"
}
trap capture_crashes EXIT
adb logcat -c
# Keep private test installs only on this disposable emulator until evidence capture/launch finishes.
./gradlew --continue searchhhVerification -PuniversalApk --max-workers=2 \
    -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true \
    -Dorg.gradle.jvmargs="-Xmx4g -XX:+UseParallelGC" 2>&1 | tee quality-device.log
adb shell am start -W -n app.searchhh.browser/info.plateaukao.einkbro.searchhh.SearchhhActivity \
    > build/reports/searchhh/launch.log
adb shell pidof app.searchhh.browser > build/reports/searchhh/process.txt
adb shell uiautomator dump /sdcard/searchhh-quality-ui.xml
adb pull /sdcard/searchhh-quality-ui.xml build/reports/searchhh/launch-ui.xml
grep -q 'Less noise.' build/reports/searchhh/launch-ui.xml
adb exec-out screencap -p > build/reports/searchhh/launch.png

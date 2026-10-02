# S00-HTTP-WEBVIEW-SECURITY — opt-in HTTP and mixed-content hardening

Status: IMPLEMENTED, HOST UNIT VERIFICATION PASSED; DEVICE VERIFICATION OPEN. WIP1 root-cause ticket.
Owner: current agent; follow-up review required.

## Finding and scope

The app permitted HTTP top-level navigation by default and configured both the
main and popup WebViews with `MIXED_CONTENT_ALWAYS_ALLOW`. The manifest and
network security configuration also retain broad cleartext permission for legacy
browser compatibility; this ticket does not claim those remaining transport
paths are closed.

## Minimal change

- Added `BrowserConfig.allowHttp`, default false and persisted in preferences.
- Added a visible Start Settings control explaining that HTTP is insecure and
  should be enabled only for a trusted site with no HTTPS alternative.
- Blocked HTTP navigation in `EBWebViewClient.handleUri` unless that setting is
  enabled, with an observable toast. HTTPS and non-HTTP schemes are unaffected.
- Changed main and popup WebViews to `MIXED_CONTENT_NEVER_ALLOW`, preventing an
  HTTPS document from silently loading HTTP subresources.
- Added pure policy regression tests for default blocking, explicit approval,
  HTTPS and null schemes.

No automatic HTTPS upgrade was added; that could change URL identity and app
behavior. No certificate bypass, cleartext manifest change or backend transport
change was hidden in this ticket.

## Verification

- `python3 -m unittest discover -s tools/quality/tests -q`: 105 PASS.
- Added `BrowserConfigTest` policy/default/persistence coverage, but the Android
  test could not execute locally: `./gradlew :app:testDebugUnitTest ...` exits
  before Gradle because Java/JAVA_HOME is absent.
- No APK/device/WebView runtime evidence is claimed. Hosted canonical Android
  verification must compile and run the test before closure.

## Remaining review

1. Run the targeted Android test in a JDK/SDK environment and inspect the
   setting/toast/blocked-navigation behavior on an emulator.
2. Decide separately whether legacy `usesCleartextTraffic` and
   `network_security_config.xml` can be tightened without breaking authorized
   HTTP browsing, local/share flows or non-WebView clients.
3. Add a device regression for HTTPS page plus HTTP subresource blocking and
   explicit HTTP toggle behavior before closing this ticket.

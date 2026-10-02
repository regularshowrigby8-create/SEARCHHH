# Searchhh UI dependency decisions

## Current foundation

- Kotlin and Jetpack Compose already exist in the project.
- Existing Compose Material APIs are used by SearchhhActivity and shared UI.
- AndroidX WebKit remains the browser foundation.
- AndroidX lifecycle, Room, WorkManager and coroutines remain in the current
  architecture.

## Supplied brief: assessed but not added

| Candidate | Decision | Reason |
|---|---|---|
| Material 3 | staged | Current launcher uses Material 2 APIs; migrate in a separately compiled/tested ticket |
| NavigationSuiteScaffold | staged | Current four-route launcher works; add when adaptive navigation has real destinations |
| Coil | not added | Existing image/browser handling must be audited first |
| Lottie/Rive | not added | No approved illustration requirement; native Compose motion is lighter |
| Shimmer | not added | Loading behavior needs a real visual baseline first |
| Paging | not added | Current bounded Room/session result model needs an explicit pagination contract |
| Paparazzi | not added | Screenshot environment and reference ownership are not yet available |
| Showkase | not added | Component inventory is not yet stable |
| LeakCanary | not added | Development-only memory work requires a device baseline |

No dependency is added solely because the brief lists it. Every future addition
must record repository, version, license, APK impact, permissions, maintenance
status and the exact behavior it enables.

# Searchhh UI architecture — link implementation checkpoint

This document translates the supplied design brief into the existing repository
without inventing screens or copying a new sample application.

## Implemented in this checkpoint

- Existing real destinations remain the source of truth: Discover, Sources,
  Saved and Settings.
- `SearchhhMenuRegistry.kt` owns the bottom-navigation route/icon mapping.
  The launcher no longer keeps a positional icon list separate from routes.
- `SearchhhDesignSystem.kt` owns Searchhh colors, surfaces, warning color,
  shapes and the light/dark theme boundary. Existing Material 2 APIs remain
  compatible with the current app while a tested Material 3 migration is staged.
- Existing stable semantic tags remain in use.
- Search state, Room persistence, backend status and WebView navigation remain
  in their existing state/data layers; this checkpoint does not replace them.

## Deliberately not added

The brief proposes Collections, Alerts, Downloads, Browser as a new native
route, filter sheets, NavigationSuiteScaffold, Material 3 migration, Lottie,
Rive, Shimmer, Paging and new result-detail destinations. They are not added as
empty routes or decorative controls. Each needs real state, persistence,
behavior tests, accessibility evidence and a bounded implementation ticket.
The existing BrowserActivity remains the real browser destination.

## Current route registry

| Route | Implemented screen | Bottom navigation |
|---|---|---:|
| Discover | Search/start/live results | yes |
| Sources | Source and codebase selection | yes |
| Saved | Room-backed saved results | yes |
| Settings | Backend, relay, AI and app settings | yes |

## Design rules adopted from the brief

- AndroidX and existing project components first.
- No API keys in the APK.
- No complete Chromium build.
- HTTPS-first WebView policy with explicit HTTP approval.
- Stable route IDs and semantic tags.
- No route without a real destination and behavior test.
- Light/dark themes through one boundary.
- Use motion only after behavior and reduced-motion requirements are verified.
- Keep the existing app architecture; no unrelated rewrite.

## Next UI tickets

1. Run host/device verification for the centralized theme and menu change.
2. Add a real filter-state model and bottom sheet only after its persistence and
   result filtering behavior are specified.
3. Audit accessibility semantics and font scaling on actual screens.
4. Evaluate Material 3 migration as a separate dependency/build ticket; do not
   add an unused dependency to inflate completion.

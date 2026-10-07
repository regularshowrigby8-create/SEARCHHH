# Searchhh 0.2.0 — included backend and MCP

Install the APK and enter your name. No repository deployment, hosting account,
backend URL or manual credential setup is required. A random installation UUID
and independent secure credential are generated on the phone.

## Included

- Kotlin/Room backend with in-process UI calls and foreground-service controls.
- 128-source catalog based on existing SearXNG adapters; direct keyless GitHub and
  Hacker News connectors, plus automatic public metasearch discovery.
- Official MCP SDK SSE server: `/sse` and `/message`, seven authenticated tools.
- JSch outbound bridge to localhost.run; consent-based credential sharing and rotation.
- Reused EinkBro browser, local saves, deduplication, known-date sorting and Stop.

## Verified

All sequential stages passed in run **36505625533**: framework/JS tests, loading all
128 adapters in real SearXNG, Android compile/unit/lint/assembly, Android 35 device
checks, and publication. Device checks include identity isolation, Room migration,
MCP authentication/negotiation/tools, credential rotation and Stop.

The live Android relay diagnostic successfully performed **public HTTPS authentication
and MCP SSE initialization/tool calls** against the same phone installation.
The live search diagnostic returned **40 results (20 GitHub + 20 Hacker News)**.

## Important limits

The selected public SearXNG node returned **HTTP 429**; the app reported this without
bypass or provider rotation. 128 catalog adapters do not mean 128 always-available
search providers. Free relay URLs can change, and the relay terminates HTTPS and
can see forwarded traffic. Disable remote access if unwanted. No AI model is bundled.

Debug-signed development APK; not a production security or opportunity-accuracy
certification. Physical-device and long-duration testing remain outstanding.
If an older test APK refuses an update, export saved results before uninstalling it.

Source: `22d6835343e933c6d0d82ecc6be5a661b441a88d`.
APK: 28,254,276 bytes.
SHA-256: `2b584955200619a2a227c0e3021395fe89bc21590174414be03fa7862a6436de`.
